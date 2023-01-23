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

import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiModifier;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.PsiReferenceBase;
import consulo.language.psi.PsiReferenceProvider;
import consulo.language.util.ProcessingContext;
import consulo.util.collection.ArrayUtil;
import consulo.xml.psi.xml.XmlAttributeValue;
import org.jetbrains.plugins.javaFX.fxml.JavaFxPsiUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * User: anna
 */
class JavaFxFactoryReferenceProvider extends PsiReferenceProvider {
  @Nonnull
  @Override
  public PsiReference[] getReferencesByElement(@Nonnull PsiElement element,
                                               @Nonnull ProcessingContext context) {
    final XmlAttributeValue attributeValue = (XmlAttributeValue)element;
    return new PsiReference[]{new JavaFXFactoryReference(attributeValue)};
  }

  private static class JavaFXFactoryReference extends PsiReferenceBase<XmlAttributeValue> {
    public JavaFXFactoryReference(XmlAttributeValue attributeValue) {
      super(attributeValue);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
      final PsiClass psiClass = JavaFxPsiUtil.getTagClass(getElement());
      if (psiClass != null) {
        final PsiMethod[] psiMethods = psiClass.findMethodsByName(getElement().getValue(), false);
        for (PsiMethod method : psiMethods) {
          if (isFactoryMethod(method)) {
            return method;
          }
        }
      }
      return null;
    }

    private static boolean isFactoryMethod(PsiMethod method) {
      return method.hasModifierProperty(PsiModifier.STATIC) && method.getParameterList().getParametersCount() == 0;
    }

    @Nonnull
    @Override
    public Object[] getVariants() {
      final PsiClass psiClass = JavaFxPsiUtil.getTagClass(getElement());
      if (psiClass != null) {
        final List<PsiMethod> methods = new ArrayList<PsiMethod>();
        for (PsiMethod method : psiClass.getMethods()) {
          if (isFactoryMethod(method)) {
            methods.add(method);
          }
        }
        return ArrayUtil.toObjectArray(methods);
      }
      return ArrayUtil.EMPTY_OBJECT_ARRAY;
    }
  }
}
