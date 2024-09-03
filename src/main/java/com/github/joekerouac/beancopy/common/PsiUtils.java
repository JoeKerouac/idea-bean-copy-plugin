package com.github.joekerouac.beancopy.common;

import com.github.joekerouac.beancopy.model.Position;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author JoeKerouac
 * @date 2024-09-01 10:29:52
 * @since 1.0.0
 */
public class PsiUtils {


    /**
     * 获取代码插入位置
     *
     * @param root    根节点
     * @param current 当前搜索节点
     * @return 生成代码的插入位置
     */
    @Nullable
    public static Position getPosition(@NotNull PsiElement root, @Nullable PsiElement current) {
        if (current == null) {
            return null;
        }

        if (current instanceof PsiLoopStatement) {
            return getPosition(root, ((PsiLoopStatement) current).getBody());
        } else if (current instanceof PsiIfStatement) {
            return getPosition(root, ((PsiIfStatement) current).getThenBranch());
        } else if (current instanceof PsiParameterListOwner) {
            Position position = new Position();
            position.setRoot((((PsiParameterListOwner) current).getBody()));
            if (!(root.getParent() instanceof PsiParameter) && !(root.getParent() instanceof PsiParameterList)) {
                position.setAnchor(root);
            }
            return position;
        } else if (current instanceof PsiCodeBlock) {
            Position position = new Position();
            position.setRoot(current);
            position.setAnchor(root);
            return position;
        } else if (current instanceof PsiBlockStatement) {
            Position position = new Position();
            position.setRoot(((PsiBlockStatement) current).getCodeBlock());
            position.setAnchor(root);
            return position;
        } else if (current instanceof PsiDeclarationStatement) {
            return getPosition(current, current.getParent());
        }

        return getPosition(root, current.getParent());
    }


    /**
     * 尝试解析元素类型，如果element是变量，则返回对应的类型
     *
     * @param element 元素
     * @return 元素类型，无法解析时返回null；
     */
    @Nullable
    public static PsiClass resolveType(@Nullable PsiElement element) {
        if (element == null) {
            return null;
        }

        if (element instanceof PsiVariable) {
            // 变量、方法参数等
            return PsiUtils.getPsiClass(((PsiVariable) element).getType());
        } else {
            return null;
        }
    }


    /**
     * 获取光标处的Identifier
     *
     * @param e 事件
     * @return 光标处的Identifier，如果光标处不是Identifier，则返回null
     */
    @Nullable
    public static PsiIdentifier getIdentifierAtCaret(@NotNull AnActionEvent e) {
        PsiClass currentClass = ActionUtils.getClass(e);
        if (currentClass == null) {
            return null;
        }

        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);

        if (psiFile == null) {
            return null;
        }

        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (editor == null) {
            return null;
        }

        int offset = editor.getCaretModel().getOffset();

        PsiElement ele1 = psiFile.findElementAt(offset - 1);
        PsiElement ele2 = psiFile.findElementAt(offset);
        PsiElement ele3 = psiFile.findElementAt(offset + 1);

        PsiIdentifier identifier = null;

        if (ele2 instanceof PsiIdentifier) {
            identifier = (PsiIdentifier) ele2;
        } else if (ele1 instanceof PsiIdentifier) {
            identifier = (PsiIdentifier) ele1;
        } else if (ele3 instanceof PsiIdentifier) {
            identifier = (PsiIdentifier) ele3;
        }

        // 不允许使用字段作为目标对象
        if (identifier == null || identifier instanceof PsiField) {
            return null;
        }

        return identifier;
    }


    /**
     * 尝试从指定element处开始搜索变量
     *
     * @param searchRoot element
     * @param name       变量名
     * @return 搜索到的变量，如果未搜索到，则返回null
     */
    @Nullable
    public static PsiVariable findVariables(PsiResolveHelper resolveHelper, PsiElement searchRoot, String name) {
        if (searchRoot == null) {
            return null;
        }

        if (searchRoot.getParent() instanceof PsiParameter) {
            return findVariables(resolveHelper, searchRoot.getParent(), name);
        }

        if (searchRoot.getParent() instanceof PsiParameterList) {
            for (PsiParameter parameter : ((PsiParameterList) searchRoot.getParent()).getParameters()) {
                if (parameter.getName().equals(name)) {
                    return parameter;
                }
            }
        }

        PsiVariable psiVariable = resolveHelper.resolveAccessibleReferencedVariable(name, searchRoot);
        if (psiVariable != null) {
            return psiVariable;
        }

        return null;
    }

    /**
     * 文档注释转单行文本 多行之间以空格分开
     *
     * @param docComment 多行注释
     */
    public static String transformMultilineToSingleLine(@NotNull PsiDocComment docComment) {
        if (docComment.getText() == null) {
            return null;
        }

        return "// " + Arrays.stream(docComment.getText().split("\n")).map(String::trim).filter(str -> !str.isEmpty()).map(str -> {
            String result = str;
            if (result.startsWith("/*")) {
                result = result.substring(2);
            }

            if (result.endsWith("*/")) {
                result = result.substring(0, result.length() - 2);
            }

            while (result.startsWith(" ") || result.startsWith("*")) {
                result = result.substring(1);
            }
            return result.trim();
        }).collect(Collectors.joining(" "));
    }


    /**
     * 根据PsiType解析PsiClass
     *
     * @param type PsiType
     * @return PsiClass
     */
    @Nullable
    public static PsiClass getPsiClass(@Nullable PsiType type) {
        if (type instanceof PsiClassType) {
            return ((PsiClassType) type).resolve();
        }
        return null;
    }
}
