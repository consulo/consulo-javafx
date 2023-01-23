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

import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.PsiReferenceProvider;
import consulo.language.util.ProcessingContext;
import consulo.util.collection.ArrayUtil;
import consulo.xml.psi.xml.XmlProcessingInstruction;
import consulo.xml.psi.xml.XmlTokenType;
import org.jetbrains.plugins.javaFX.fxml.JavaFxPsiUtil;

import javax.annotation.Nonnull;

/**
 * User: anna
 */
class ImportReferenceProvider extends PsiReferenceProvider {

  @Nonnull
  @Override
  public PsiReference[] getReferencesByElement(@Nonnull PsiElement element, @Nonnull ProcessingContext context) {
    if (element instanceof XmlProcessingInstruction) {
      final ASTNode importNode = element.getNode().findChildByType(XmlTokenType.XML_TAG_CHARACTERS);
      if (importNode != null) {
        final PsiElement importInstr = importNode.getPsi();
        final String instructionTarget = JavaFxPsiUtil.getInstructionTarget("import", (XmlProcessingInstruction)element);
        if (instructionTarget != null && instructionTarget.equals(importInstr.getText())) {
          final PsiReference[] references =
            FxmlReferencesContributor.CLASS_REFERENCE_PROVIDER.getReferencesByString(instructionTarget,
                                                                                     element,
                                                                                     importInstr.getStartOffsetInParent());
          if (instructionTarget.endsWith(".*")) {
            return ArrayUtil.remove(references, references.length - 1);
          }
          else {
            return references;
          }
        }
      }
    }
    return PsiReference.EMPTY_ARRAY;
  }
}
