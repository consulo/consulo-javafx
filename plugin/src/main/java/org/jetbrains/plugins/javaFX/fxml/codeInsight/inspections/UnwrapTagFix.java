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
package org.jetbrains.plugins.javaFX.fxml.codeInsight.inspections;

import consulo.language.codeStyle.CodeStyleManager;
import consulo.language.editor.FileModificationService;
import consulo.language.editor.inspection.LocalQuickFix;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.logging.Logger;
import consulo.project.Project;
import consulo.xml.psi.xml.XmlTag;
import consulo.xml.psi.xml.XmlTagChild;
import org.jetbrains.plugins.javaFX.fxml.JavaFxFileTypeFactory;

import jakarta.annotation.Nonnull;

/**
* User: anna
*/
public class UnwrapTagFix implements LocalQuickFix {
  private static final Logger LOG = Logger.getInstance(UnwrapTagFix.class);
  private final String myTagName;

  public UnwrapTagFix(String tagName) {
    myTagName = tagName;
  }

  @Nonnull
  @Override
  public String getName() {
    return "Unwrap '" + myTagName + "'";
  }

  @Nonnull
  @Override
  public String getFamilyName() {
    return getName();
  }

  @Override
  public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
    final PsiElement element = descriptor.getPsiElement();
    if (element != null) {
      final PsiFile containingFile = element.getContainingFile();
      LOG.assertTrue(containingFile != null && JavaFxFileTypeFactory.isFxml(containingFile), containingFile == null ? "no containing file found" : "containing file: " + containingFile.getName());
      final XmlTag xmlTag = PsiTreeUtil.getParentOfType(element, XmlTag.class);
      if (xmlTag != null) {
        final XmlTag parentTag = xmlTag.getParentTag();
        final PsiElement[] children = PsiTreeUtil.getChildrenOfType(xmlTag, XmlTagChild.class);
        if (children != null) {
          if (!FileModificationService.getInstance().preparePsiElementsForWrite(element)) return;
          if (children.length > 0) {
            parentTag.addRange(children[0], children[children.length - 1]);
          }
          xmlTag.delete();
          CodeStyleManager.getInstance(project).reformat(parentTag);
        }
      }
    }
  }
}
