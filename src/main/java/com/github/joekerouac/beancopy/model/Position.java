package com.github.joekerouac.beancopy.model;

import com.intellij.psi.PsiElement;

/**
 * @author JoeKerouac
 * @date 2024-09-01 10:29:52
 * @since 1.0.0
 */
public class Position {

    /**
     * 插入root
     */
    private PsiElement root;

    /**
     * 插入定位
     */
    private PsiElement anchor;

    public PsiElement getRoot() {
        return root;
    }

    public void setRoot(PsiElement root) {
        this.root = root;
    }

    public PsiElement getAnchor() {
        return anchor;
    }

    public void setAnchor(PsiElement anchor) {
        this.anchor = anchor;
    }
}
