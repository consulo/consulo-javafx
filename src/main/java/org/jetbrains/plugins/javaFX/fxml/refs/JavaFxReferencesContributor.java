/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.plugins.javaFX.fxml.refs;

import com.intellij.java.language.JavaLanguage;
import com.intellij.java.language.patterns.PsiJavaElementPattern;
import com.intellij.java.language.psi.*;
import com.intellij.java.language.psi.util.PsiUtil;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.pattern.FilterPattern;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReferenceContributor;
import consulo.language.psi.PsiReferenceRegistrar;
import consulo.language.psi.filter.ElementFilter;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.util.lang.StringUtil;
import org.jetbrains.plugins.javaFX.JavaFxFileReferenceProvider;
import org.jetbrains.plugins.javaFX.fxml.JavaFxCommonClassNames;
import org.jetbrains.plugins.javaFX.fxml.JavaFxFileTypeFactory;

import javax.annotation.Nonnull;

import static com.intellij.java.language.patterns.PsiJavaPatterns.literalExpression;

/**
 * User: anna
 * Date: 2/22/13
 */
@ExtensionImpl
public class JavaFxReferencesContributor extends PsiReferenceContributor {
  public static final PsiJavaElementPattern.Capture<PsiLiteralExpression> STYLESHEET_PATTERN =
    literalExpression().and(new FilterPattern(new ElementFilter() {
      public boolean isAcceptable(Object element, PsiElement context) {
        final PsiExpression psiExpression = getParentElement((PsiLiteralExpression)context);
        if (psiExpression != null) {
          final PsiType psiType = psiExpression.getType();
          return psiType != null && psiType.equalsToText(JavaFxCommonClassNames.JAVA_FX_PARENT);
        }
        return false;
      }

      public boolean isClassAcceptable(Class hintClass) {
        return true;
      }
    }));

  public static PsiExpression getParentElement(PsiLiteralExpression context) {
    PsiMethodCallExpression methodCallExpression = PsiTreeUtil.getParentOfType(context, PsiMethodCallExpression.class);
    final Object value = context.getValue();
    if (value instanceof String && ((String)value).endsWith(".bss")) {
      final PsiExpressionList addArgumentsList = PsiTreeUtil.getParentOfType(methodCallExpression, PsiExpressionList.class);
      methodCallExpression = PsiTreeUtil.getParentOfType(addArgumentsList, PsiMethodCallExpression.class);
    }
    if (methodCallExpression != null) {
      PsiMethod psiMethod = methodCallExpression.resolveMethod();
      if (psiMethod != null) {
        if ("getResource".equals(psiMethod.getName())) {
          final PsiExpressionList addArgumentsList = PsiTreeUtil.getParentOfType(methodCallExpression, PsiExpressionList.class);
          methodCallExpression = PsiTreeUtil.getParentOfType(addArgumentsList, PsiMethodCallExpression.class);
          psiMethod = methodCallExpression != null ? methodCallExpression.resolveMethod() : null;
          if (psiMethod == null) return null;
        }
        if ("add".equals(psiMethod.getName())) {
          final PsiClass containingClass = psiMethod.getContainingClass();
          if (containingClass != null) {
            final PsiExpression qualifierExpression = methodCallExpression.getMethodExpression().getQualifierExpression();
            if (qualifierExpression instanceof PsiMethodCallExpression) {
              final PsiReferenceExpression getStylesheetsMethodExpression =
                ((PsiMethodCallExpression)qualifierExpression).getMethodExpression();
              if ("getStylesheets".equals(getStylesheetsMethodExpression.getReferenceName())) {
                return getStylesheetsMethodExpression.getQualifierExpression();
              }
            }
          }
        }
      }
    }
    return null;
  }

  public static final PsiJavaElementPattern.Capture<PsiLiteralExpression> FXML_PATTERN =
    literalExpression().and(new FilterPattern(new ElementFilter() {
      public boolean isAcceptable(Object element, PsiElement context) {
        final PsiLiteralExpression literalExpression = (PsiLiteralExpression)context;
        PsiMethodCallExpression callExpression = PsiTreeUtil.getParentOfType(literalExpression, PsiMethodCallExpression.class);
        if (callExpression != null && "getResource".equals(callExpression.getMethodExpression().getReferenceName())) {
          final PsiCallExpression superCall = PsiTreeUtil.getParentOfType(callExpression, PsiCallExpression.class, true);
          if (superCall instanceof PsiMethodCallExpression) {
            final PsiReferenceExpression methodExpression = ((PsiMethodCallExpression)superCall).getMethodExpression();
            if ("load".equals(methodExpression.getReferenceName())) {
              final PsiExpression qualifierExpression = methodExpression.getQualifierExpression();
              PsiClass psiClass = null;
              if (qualifierExpression instanceof PsiReferenceExpression) {
                final PsiElement resolve = ((PsiReferenceExpression)qualifierExpression).resolve();
                if (resolve instanceof PsiClass) {
                  psiClass = (PsiClass)resolve;
                }
              }
              else if (qualifierExpression != null) {
                psiClass = PsiUtil.resolveClassInType(qualifierExpression.getType());
              }
              if (psiClass != null && JavaFxCommonClassNames.JAVAFX_FXML_FXMLLOADER.equals(psiClass.getQualifiedName())) {
                return true;
              }
            }
          }
          else if (superCall instanceof PsiNewExpression) {
            final PsiJavaCodeReferenceElement reference = ((PsiNewExpression)superCall).getClassOrAnonymousClassReference();
            if (reference != null) {
              final PsiElement resolve = reference.resolve();
              if (resolve instanceof PsiClass && JavaFxCommonClassNames.JAVAFX_FXML_FXMLLOADER.equals(((PsiClass)resolve).getQualifiedName())) {
                return true;
              }
            }
          }
        }
        return false;
      }

      public boolean isClassAcceptable(Class hintClass) {
        return true;
      }
    }));

  @Override
  public void registerReferenceProviders(PsiReferenceRegistrar registrar) {
    registrar.registerReferenceProvider(FXML_PATTERN, new JavaFxFileReferenceProvider(JavaFxFileTypeFactory.FXML_EXTENSION));
    registrar.registerReferenceProvider(STYLESHEET_PATTERN, new JavaFxFileReferenceProvider("css") {
      @Override
      protected String preprocessValue(String value) {
        if (value.endsWith(".bss")) {
          return StringUtil.trimEnd(value, ".bss") + ".css";
        }
        return value;
      }
    });
  }

  @Nonnull
  @Override
  public Language getLanguage() {
    return JavaLanguage.INSTANCE;
  }
}
