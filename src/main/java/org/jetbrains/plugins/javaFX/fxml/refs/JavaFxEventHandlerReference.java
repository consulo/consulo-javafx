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

import com.intellij.java.language.psi.*;
import com.intellij.java.language.psi.util.InheritanceUtil;
import consulo.document.util.TextRange;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReferenceBase;
import consulo.util.collection.ArrayUtil;
import consulo.xml.psi.xml.XmlAttributeValue;
import org.jetbrains.plugins.javaFX.fxml.JavaFxCommonClassNames;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * User: anna
 * Date: 1/16/13
 */
public class JavaFxEventHandlerReference extends PsiReferenceBase<XmlAttributeValue> {
  protected final PsiClass myCurrentTagClass;
  protected final PsiMethod myEventHandler;
  protected final PsiClass myController;

  public JavaFxEventHandlerReference(XmlAttributeValue element, PsiClass currentTagClass, final PsiMethod method, PsiClass controller) {
    super(element);
    myCurrentTagClass = currentTagClass;
    myEventHandler = method;
    myController = controller;
  }

  @Nullable
  @Override
  public PsiElement resolve() {
    return myEventHandler;
  }

  @Nonnull
  @Override
  public Object[] getVariants() {
    if (myController == null) return EMPTY_ARRAY;
    final List<PsiMethod> availableHandlers = new ArrayList<PsiMethod>();
    for (PsiMethod psiMethod : myController.getMethods()) {
      if (isHandlerMethod(psiMethod)) {
        availableHandlers.add(psiMethod);
      }
    }
    return availableHandlers.isEmpty() ? EMPTY_ARRAY : ArrayUtil.toObjectArray(availableHandlers);
  }

  public static boolean isHandlerMethod(PsiMethod psiMethod) {
    if (!psiMethod.hasModifierProperty(PsiModifier.STATIC) && PsiType.VOID.equals(psiMethod.getReturnType())) {
      final PsiParameter[] parameters = psiMethod.getParameterList().getParameters();
      if (parameters.length == 1) {
        final PsiType parameterType = parameters[0].getType();
        if (InheritanceUtil.isInheritor(parameterType, JavaFxCommonClassNames.JAVAFX_EVENT)) {
          return true;
        }
      }
      return parameters.length == 0;
    }
    return false;
  }

  @Override
  public TextRange getRangeInElement() {
    final TextRange range = super.getRangeInElement();
    return new TextRange(range.getStartOffset() + 1, range.getEndOffset());
  }

}
