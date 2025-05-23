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

import consulo.codeEditor.Editor;
import consulo.language.editor.FileModificationService;
import consulo.language.editor.intention.PsiElementBaseIntentionAction;
import consulo.language.editor.intention.SyntheticIntentionAction;
import consulo.language.psi.PsiElement;
import consulo.language.util.IncorrectOperationException;
import consulo.project.Project;
import consulo.xml.psi.XmlElementFactory;
import consulo.xml.psi.xml.XmlTag;
import org.jetbrains.plugins.javaFX.fxml.FxmlConstants;

import jakarta.annotation.Nonnull;

/**
 * User: anna
 * Date: 4/1/13
 */
public class JavaFxWrapWithDefineIntention extends PsiElementBaseIntentionAction implements SyntheticIntentionAction {
  private final XmlTag myTag;
  private final String myId;

  public JavaFxWrapWithDefineIntention(@Nonnull XmlTag tag, @Nonnull String id) {
    myTag = tag;
    myId = id;
    setText("Wrap \"" + myId + "\" with fx:define");
  }

  @Override
  public boolean isAvailable(@Nonnull Project project, Editor editor, @Nonnull PsiElement element) {
    return myTag.isValid();
  }

  @Override
  public void invoke(@Nonnull Project project, Editor editor, @Nonnull PsiElement element) throws IncorrectOperationException {
    if (!FileModificationService.getInstance().preparePsiElementsForWrite(element)) return;
    final XmlTag tagFromText = XmlElementFactory.getInstance(project).createTagFromText("<" + FxmlConstants.FX_DEFINE + "/>");
    tagFromText.addSubTag(myTag, true);
    myTag.replace(tagFromText);
  }

  @Override
  public boolean startInWriteAction() {
    return true;
  }
}
