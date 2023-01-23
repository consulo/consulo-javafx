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

import com.intellij.java.language.psi.PsiClassType;
import com.intellij.java.language.psi.PsiField;
import com.intellij.java.language.psi.util.InheritanceUtil;
import com.intellij.xml.XmlAttributeDescriptor;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.PsiReferenceProvider;
import consulo.language.util.ProcessingContext;
import consulo.xml.psi.xml.XmlAttribute;
import consulo.xml.psi.xml.XmlAttributeValue;
import org.jetbrains.plugins.javaFX.fxml.JavaFxCommonClassNames;
import org.jetbrains.plugins.javaFX.fxml.JavaFxPsiUtil;
import org.jetbrains.plugins.javaFX.fxml.descriptors.JavaFxPropertyAttributeDescriptor;

import javax.annotation.Nonnull;

/**
 * User: anna
 * Date: 3/7/13
 */
class JavaFxColorReferenceProvider extends PsiReferenceProvider {
  @Nonnull
  @Override
  public PsiReference[] getReferencesByElement(@Nonnull PsiElement element,
                                               @Nonnull ProcessingContext context) {
    final XmlAttributeValue attributeValue = (XmlAttributeValue)element;
    final PsiElement parent = attributeValue.getParent();
    if (parent instanceof XmlAttribute) {
      final XmlAttributeDescriptor descriptor = ((XmlAttribute)parent).getDescriptor();
      if (descriptor instanceof JavaFxPropertyAttributeDescriptor) {
        final PsiElement declaration = descriptor.getDeclaration();
        if (declaration instanceof PsiField) {
          final PsiField field = (PsiField)declaration;
          final PsiClassType propertyClassType = JavaFxPsiUtil.getPropertyClassType(field);
          if (propertyClassType != null && InheritanceUtil.isInheritor(propertyClassType, JavaFxCommonClassNames.JAVAFX_SCENE_PAINT)) {
            return new PsiReference[]{new JavaFxColorReference(attributeValue)};
          }
        }
      }
    }
    return PsiReference.EMPTY_ARRAY;
  }
}
