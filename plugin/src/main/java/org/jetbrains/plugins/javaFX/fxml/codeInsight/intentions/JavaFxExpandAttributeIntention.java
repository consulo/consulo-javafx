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
package org.jetbrains.plugins.javaFX.fxml.codeInsight.intentions;

import com.intellij.java.language.codeInsight.daemon.impl.analysis.JavaGenericsUtil;
import com.intellij.java.language.psi.*;
import com.intellij.java.language.psi.util.PsiUtil;
import com.intellij.xml.XmlAttributeDescriptor;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.language.editor.FileModificationService;
import consulo.language.editor.intention.IntentionMetaData;
import consulo.language.editor.intention.PsiElementBaseIntentionAction;
import consulo.language.psi.PsiElement;
import consulo.language.util.IncorrectOperationException;
import consulo.logging.Logger;
import consulo.project.Project;
import consulo.util.lang.StringUtil;
import consulo.xml.psi.XmlElementFactory;
import consulo.xml.psi.xml.*;
import org.jetbrains.plugins.javaFX.fxml.FxmlConstants;
import org.jetbrains.plugins.javaFX.fxml.JavaFxPsiUtil;
import org.jetbrains.plugins.javaFX.fxml.descriptors.JavaFxDefaultAttributeDescriptor;
import org.jetbrains.plugins.javaFX.fxml.descriptors.JavaFxPropertyAttributeDescriptor;
import org.jetbrains.plugins.javaFX.fxml.descriptors.JavaFxStaticPropertyAttributeDescriptor;

import jakarta.annotation.Nonnull;

/**
 * User: anna
 * Date: 2/22/13
 */
@ExtensionImpl
@IntentionMetaData(ignoreId = "javafx.expand.attribute.to.tag", categories = {"XML", "JavaFX"}, fileExtensions = "fxml")
public class JavaFxExpandAttributeIntention extends PsiElementBaseIntentionAction{
  private static final Logger LOG = Logger.getInstance(JavaFxExpandAttributeIntention.class);

  public JavaFxExpandAttributeIntention() {
    setText("Expand attribute to tag");
  }

  @Override
  public void invoke(@Nonnull Project project, Editor editor, @Nonnull PsiElement element) throws IncorrectOperationException {
    if (!FileModificationService.getInstance().preparePsiElementsForWrite(element)) return;
    final XmlAttribute attr = (XmlAttribute)element.getParent();
    final String name = attr.getName();
    final XmlAttributeDescriptor descriptor = attr.getDescriptor();
    LOG.assertTrue(descriptor != null);
    String value = attr.getValue();
    final PsiElement declaration = descriptor.getDeclaration();
    if (declaration instanceof PsiField) {
      final PsiType fieldType = ((PsiField)declaration).getType();
      final PsiType itemType = JavaGenericsUtil.getCollectionItemType(fieldType, declaration.getResolveScope());
      if (itemType != null) {
        final String typeNode = itemType.getPresentableText();
        JavaFxPsiUtil.insertImportWhenNeeded((XmlFile)attr.getContainingFile(), typeNode, itemType.getCanonicalText());
        final String[] vals = value.split(",");
        value = StringUtil.join(vals, s -> "<" + typeNode + " " + FxmlConstants.FX_VALUE + "=\"" + s.trim() + "\"/>", "\n");
      }
    }
    final XmlTag childTag = XmlElementFactory.getInstance(project).createTagFromText("<" + name + ">" + value + "</" + name + ">");
    attr.getParent().add(childTag);
    attr.delete();
  }

  @Override
  public boolean isAvailable(@Nonnull Project project, Editor editor, @Nonnull PsiElement element) {
    if (element instanceof XmlToken && ((XmlToken)element).getTokenType() == XmlTokenType.XML_NAME) {
      final PsiElement parent = element.getParent();
      if (parent instanceof XmlAttribute) {
        final XmlAttributeDescriptor descriptor = ((XmlAttribute)parent).getDescriptor();
        if (descriptor instanceof JavaFxPropertyAttributeDescriptor && !(descriptor instanceof JavaFxDefaultAttributeDescriptor)) {

          PsiType tagType = null;
          final PsiElement declaration = descriptor.getDeclaration();
          if (declaration instanceof PsiField) {
            tagType = ((PsiField)declaration).getType();
          } else if (declaration instanceof PsiMethod) {
            final PsiParameter[] parameters = ((PsiMethod)declaration).getParameterList().getParameters();
            if (parameters.length == 1) {
              tagType = parameters[0].getType();
            }
          }
          PsiClass
            tagClass = PsiUtil.resolveClassInType(tagType instanceof PsiPrimitiveType ? ((PsiPrimitiveType)tagType).getBoxedType(parent) : tagType);
          if ((tagClass != null && JavaFxPsiUtil.isAbleToInstantiate(tagClass) == null) || descriptor instanceof JavaFxStaticPropertyAttributeDescriptor) {
            setText("Expand '" + ((XmlAttribute)parent).getName() + "' to tag");
            return true;
          }
        }
      }
    }
    return false;
  }
}
