package com.github.joekerouac.beancopy.common;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * @author JoeKerouac
 * @date 2024-09-01 10:29:52
 * @since 1.0.0
 */
public class ActionUtils {

    /**
     * 获取当前类
     * can both from editor and projectViewer
     */
    @Nullable
    public static PsiClass getClass(@NotNull AnActionEvent event) {
        PsiFile psiFile = event.getData(LangDataKeys.PSI_FILE);
        return psiFile == null ? null : PsiTreeUtil.findChildOfAnyType(psiFile.getOriginalElement(),
                PsiClass.class);
    }
}
