package com.github.joekerouac.beancopy;

import com.github.joekerouac.beancopy.common.Constant;
import com.github.joekerouac.beancopy.common.MessageTool;
import com.github.joekerouac.beancopy.common.PsiUtils;
import com.github.joekerouac.beancopy.model.Position;
import com.github.joekerouac.beancopy.model.PsiClassMeta;
import com.intellij.codeInsight.generation.PsiFieldMember;
import com.intellij.ide.util.MemberChooser;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.TransactionGuard;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author JoeKerouac
 * @date 2024-08-22 10:29:52
 * @since 1.0.0
 */
public class GenerateSetterAction extends AnAction {

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);

        assert psiFile != null;

        // 获取光标处标识符
        PsiIdentifier identifierAtCaret = PsiUtils.getIdentifierAtCaret(e);
        if (identifierAtCaret == null) {
            return;
        }

        // 获取光标处变量名
        String targetVariableName = identifierAtCaret.getText().trim();
        Project project = identifierAtCaret.getProject();

        // 获取光标处变量类型，该变量作为目标注入变量
        PsiClass targetClass = PsiUtils.resolveType(identifierAtCaret.getParent());

        if (targetClass == null) {
            // 理论上不可能
            MessageTool.warning(project, String.format("Cannot get target class type: %s", targetVariableName));
            return;
        }

        // 获取稍后要生成的代码插入位置
        Position position = PsiUtils.getPosition(identifierAtCaret, identifierAtCaret);
        if (position == null || position.getRoot() == null) {
            MessageTool.warning(project, String.format("Cannot locate where to insert code: %s", identifierAtCaret.getClass().getName()));
            return;
        }
        // 确认anchor
        if (position.getAnchor() == null && position.getRoot() instanceof PsiCodeBlock) {
            position.setAnchor(((PsiCodeBlock) position.getRoot()).getFirstBodyElement());
        }

        // 弹窗让用户输入数据来源变量名
        String sourceVariableName = Messages.showInputDialog(
                "Enter the variable name of source object.\n(If nothing is entered, nothing to do)",
                Constant.PLUGIN_NAME, Messages.getQuestionIcon());

        if (sourceVariableName == null || sourceVariableName.trim().isEmpty()) {
            return;
        }

        // 找到用户输入的变量名对应的变量
        JavaPsiFacade facade = JavaPsiFacade.getInstance(project);
        PsiVariable sourceVariable = PsiUtils.findVariables(facade.getResolveHelper(), identifierAtCaret, sourceVariableName);
        if (sourceVariable == null) {
            MessageTool.warning(project, String.format("Invalid source object name: %s", sourceVariableName));
            return;
        }

        PsiClass sourceClass = PsiUtils.resolveType(sourceVariable);
        if (sourceClass == null) {
            // 理论上不可能
            MessageTool.warning(project, String.format("Cannot get variable class type: %s", sourceVariable.getName()));
            return;
        }

        // 解析源对象、目标对象的所有getter、setter方法
        PsiClassMeta sourceClassMeta = new PsiClassMeta(sourceClass);
        Set<PsiField> sourceFieldSet = sourceClassMeta.getAllFieldWithGetter();
        PsiClassMeta targetClassMeta = new PsiClassMeta(targetClass);
        Set<PsiField> targetFieldSet = targetClassMeta.getAllFieldWithSetter();

        if (sourceFieldSet.isEmpty()) {
            MessageTool.warning(project, "No Field in " + sourceClass.getName());
            return;
        }

        if (targetFieldSet.isEmpty()) {
            MessageTool.warning(project, "No Field in " + targetClass.getName());
            return;
        }

        Map<String, PsiField> targetFieldMap = targetFieldSet.stream().collect(Collectors.toMap(PsiField::getName, Function.identity()));

        // 只有源对象和目标对象都有、并且类型一致的字段才允许复制
        // todo 后续可以允许用户自定义类型转换器
        List<PsiFieldMember> commonFields = new ArrayList<>();
        for (PsiField sourceField : sourceFieldSet) {
            PsiField targetField = targetFieldMap.get(sourceField.getName());
            if (targetField == null) {
                continue;
            }

            if (!targetField.getType().equals(sourceField.getType()) && !targetField.getType().isAssignableFrom(sourceField.getType())) {
                continue;
            }

            commonFields.add(new PsiFieldMember(sourceField));
        }

        if (commonFields.isEmpty()) {
            MessageTool.warning(project, String.format("No Common Field in %s:%s", sourceClass.getName(), targetClass.getName()));
            return;
        }

        // 弹窗让用户自己选择要复制的变量
        MemberChooser<PsiFieldMember> chooser = new MemberChooser<>(commonFields.toArray(new PsiFieldMember[0]), false, true, project);
        chooser.setTitle("Choose field(s) for copy value");
        chooser.setCopyJavadocVisible(true);

        TransactionGuard.getInstance().submitTransactionAndWait(chooser::show);
        if (chooser.getExitCode() != DialogWrapper.OK_EXIT_CODE) {
            // 没有选择OK
            return;
        }

        List<PsiFieldMember> selectedElements = chooser.getSelectedElements();
        if (selectedElements == null || selectedElements.isEmpty()) {
            // 未选择任何字段
            return;
        }

        try {
            PsiElementFactory elementFactory = facade.getElementFactory();
            List<PsiElement> addStatements = new ArrayList<>();
            for (PsiFieldMember selectedElement : selectedElements) {
                PsiField selectField = selectedElement.getElement();
                PsiMethod getterMethod = sourceClassMeta.getGetterMethod(selectField);
                PsiMethod setterMethod = targetClassMeta.getSetterMethod(targetFieldMap.get(selectField.getName()));

                String statementStr = String.format("%s.%s(%s.%s());\n", targetVariableName, setterMethod.getName(), sourceVariableName, getterMethod.getName());
                PsiStatement statement = elementFactory.createStatementFromText(statementStr, null);
                addStatements.add(statement);

                // 如果选中了要把文档复制过来，则复制文档
                if (chooser.isCopyJavadoc() && selectField.getDocComment() != null) {
                    String comment = PsiUtils.transformMultilineToSingleLine(selectField.getDocComment());
                    if (comment != null && !comment.trim().isEmpty()) {
                        addStatements.add(elementFactory.createCommentFromText(comment, null));
                    }
                }
            }

            WriteCommandAction.runWriteCommandAction(project, () -> {
                for (PsiElement statement : addStatements) {
                    if (position.getAnchor() == null) {
                        position.setAnchor(position.getRoot().add(statement));
                    } else {
                        position.setAnchor(position.getRoot().addAfter(statement, position.getAnchor()));
                    }
                }
            });
        } catch (Throwable throwable) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            String stackTrace = sw.toString();

            // 显示异常栈信息在消息框中
            Messages.showErrorDialog(stackTrace, "An Error Occurred");
        }
    }

    @Override
    public void update(AnActionEvent e) {
        PsiIdentifier identifierAtCaret = PsiUtils.getIdentifierAtCaret(e);
        if (identifierAtCaret == null) {
            // 隐藏选项
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }

        PsiClass clazz = PsiUtils.resolveType(identifierAtCaret.getParent());
        if (clazz == null) {
            // 隐藏选项
            e.getPresentation().setEnabledAndVisible(false);
        }
    }

}
