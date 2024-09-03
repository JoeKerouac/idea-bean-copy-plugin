package com.github.joekerouac.beancopy.model;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author JoeKerouac
 * @date 2024-09-01 10:29:52
 * @since 1.0.0
 */
public class PsiClassMeta {

    private final PsiClass psiClass;

    private final Map<PsiField, PsiMethod> getterMap;

    private final Map<PsiField, PsiMethod> setterMap;

    public PsiClassMeta(PsiClass psiClass) {
        this.psiClass = psiClass;
        this.getterMap = new LinkedHashMap<>();
        this.setterMap = new LinkedHashMap<>();

        for (PsiField field : psiClass.getAllFields()) {
            PsiMethod getterMethod = findGetterMethod(field);
            if (getterMethod != null) {
                getterMap.put(field, getterMethod);
            }

            PsiMethod setterMethod = findSetterMethod(field);
            if (setterMethod != null) {
                setterMap.put(field, setterMethod);
            }
        }
    }

    /**
     * 获取指定字段的get方法
     *
     * @param field 字段
     * @return get方法
     */
    public @Nullable PsiMethod getGetterMethod(PsiField field) {
        return getterMap.get(field);
    }

    /**
     * 获取指定字段的set方法
     *
     * @param field 字段
     * @return set方法
     */
    public @Nullable PsiMethod getSetterMethod(PsiField field) {
        return setterMap.get(field);
    }

    /**
     * 获取所有包含getter的字段
     *
     * @return 所有包含getter的字段
     */
    public Set<PsiField> getAllFieldWithGetter() {
        return new LinkedHashSet<>(getterMap.keySet());
    }

    /**
     * 获取所有包含setter的字段
     *
     * @return 所有包含setter的字段
     */
    public Set<PsiField> getAllFieldWithSetter() {
        return new LinkedHashSet<>(setterMap.keySet());
    }

    private PsiMethod findSetterMethod(PsiField field) {
        String fieldName = field.getName();
        fieldName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);

        String getterName = "set" + fieldName;

        PsiMethod[] methods = psiClass.findMethodsByName(getterName, true);
        for (PsiMethod method : methods) {
            if (isValidSetterForField(method, field)) {
                return method;
            }
        }

        return null;
    }

    private PsiMethod findGetterMethod(PsiField field) {
        String fieldName = field.getName();
        fieldName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);

        String getterName = "get" + fieldName;

        PsiMethod[] methods = psiClass.findMethodsByName(getterName, true);
        for (PsiMethod method : methods) {
            if (isValidGetterForField(method, field)) {
                return method;
            }
        }

        if (!PsiTypes.booleanType().equals(field.getType())) {
            return null;
        }

        getterName = "is" + fieldName;
        methods = psiClass.findMethodsByName(getterName, true);
        for (PsiMethod method : methods) {
            if (isValidGetterForField(method, field)) {
                return method;
            }
        }
        return null;
    }

    private boolean isValidGetterForField(PsiMethod method, PsiField field) {
        if (!method.hasModifierProperty("public") || method.getParameterList().getParametersCount() > 0) {
            return false;
        }

        // 方法的返回类型必须和字段类型相同
        return PsiUtil.resolveClassInClassTypeOnly(method.getReturnType()) == PsiUtil.resolveClassInClassTypeOnly(field.getType());
    }

    private boolean isValidSetterForField(PsiMethod method, PsiField field) {
        if (!method.hasModifierProperty("public") || method.getParameterList().getParametersCount() != 1) {
            return false;
        }

        // 方法的参数类型必须和字段类型相同
        return PsiUtil.resolveClassInClassTypeOnly(method.getParameterList().getParameter(0).getType()) == PsiUtil.resolveClassInClassTypeOnly(field.getType());
    }

}
