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
import com.intellij.xml.XmlElementDescriptor;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.util.ProcessingContext;
import consulo.logging.Logger;
import consulo.xml.psi.xml.XmlAttribute;
import consulo.xml.psi.xml.XmlAttributeValue;
import consulo.xml.psi.xml.XmlFile;
import consulo.xml.psi.xml.XmlTag;
import org.jetbrains.plugins.javaFX.fxml.FxmlConstants;
import org.jetbrains.plugins.javaFX.fxml.JavaFxPsiUtil;

import jakarta.annotation.Nonnull;

/**
 * User: anna
 * Date: 1/16/13
 */
class JavaFxEventHandlerReferenceProvider extends JavaFxControllerBasedReferenceProvider {
  private static final Logger LOG = Logger.getInstance(JavaFxEventHandlerReferenceProvider.class);

  @Override
  protected PsiReference[] getReferencesByElement(@Nonnull PsiClass controllerClass,
                                                  XmlAttributeValue xmlAttributeValue,
                                                  ProcessingContext context) {
    final String attValueString = xmlAttributeValue.getValue();
    LOG.assertTrue(attValueString.startsWith("#"));

    final XmlAttribute attribute = (XmlAttribute)xmlAttributeValue.getContext();
    if (attribute == null) return PsiReference.EMPTY_ARRAY;
    if (!JavaFxPsiUtil.checkIfAttributeHandler(attribute)) return PsiReference.EMPTY_ARRAY;
    final XmlElementDescriptor descriptor = attribute.getParent().getDescriptor();
    if (descriptor == null) return PsiReference.EMPTY_ARRAY;
    final PsiElement currentTagClass = descriptor.getDeclaration();
    final String eventHandlerName = attValueString.substring(1);
    final PsiMethod[] methods = controllerClass.findMethodsByName(eventHandlerName, true);

    PsiMethod handlerMethod = null;
    for (PsiMethod psiMethod : methods) {
      if (JavaFxEventHandlerReference.isHandlerMethod(psiMethod)) {
        handlerMethod = psiMethod;
        break;
      }
    }
    if (handlerMethod == null) {
      final XmlTag rootTag = ((XmlFile)xmlAttributeValue.getContainingFile()).getRootTag();
      if (rootTag == null || FxmlConstants.FX_ROOT.equals(rootTag.getName())) {
        return PsiReference.EMPTY_ARRAY;
      }
    }
    return new PsiReference[]{new JavaFxEventHandlerReference(xmlAttributeValue,
                                                              (PsiClass)currentTagClass,
                                                              handlerMethod,
                                                              controllerClass)};
  }
}
