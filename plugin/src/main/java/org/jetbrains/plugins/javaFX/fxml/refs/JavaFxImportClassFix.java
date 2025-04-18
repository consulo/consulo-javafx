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

import com.intellij.java.impl.codeInsight.daemon.impl.quickfix.ImportClassFixBase;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiMember;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiReference;
import consulo.util.lang.StringUtil;
import consulo.xml.psi.xml.XmlFile;
import consulo.xml.psi.xml.XmlTag;
import org.jetbrains.plugins.javaFX.fxml.JavaFxPsiUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * User: anna
 */
abstract class JavaFxImportClassFix extends ImportClassFixBase<XmlTag, JavaFxTagNameReference> {

  public JavaFxImportClassFix(@Nonnull JavaFxTagNameReference ref, @Nonnull XmlTag element) {
    super(element, ref);
  }

  protected abstract XmlTag getTagElement(JavaFxTagNameReference ref);

  @Nullable
  @Override
  protected String getReferenceName(@Nonnull JavaFxTagNameReference reference) {
    final XmlTag tagElement = getTagElement(reference);
    return tagElement != null ? tagElement.getName() : null;
  }

  @Override
  protected PsiElement getReferenceNameElement(@Nonnull JavaFxTagNameReference reference) {
    final XmlTag tagElement = getTagElement(reference);
    return tagElement != null ? tagElement.getNavigationElement() : null;
  }

  @Override
  protected void bindReference(PsiReference reference, PsiClass targetClass) {
    final PsiFile file = reference.getElement().getContainingFile();
    super.bindReference(reference, targetClass);
    final String qualifiedName = targetClass.getQualifiedName();
    if (qualifiedName != null) {
      final String shortName = StringUtil.getShortName(qualifiedName);
      JavaFxPsiUtil.insertImportWhenNeeded((XmlFile)file, shortName, qualifiedName);
    }
  }

  @Override
  protected boolean hasTypeParameters(@Nonnull JavaFxTagNameReference reference) {
    return false;
  }

  @Override
  protected boolean isAccessible(PsiMember member, XmlTag reference) {
    return member instanceof PsiClass && JavaFxPsiUtil.isClassAcceptable(reference.getParentTag(), (PsiClass)member) == null;
  }

  @Override
  protected String getQualifiedName(XmlTag tag) {
    return tag.getDescriptor().getQualifiedName();
  }

  @Override
  protected boolean isQualified(JavaFxTagNameReference reference) {
    return false;
  }

  @Override
  protected boolean hasUnresolvedImportWhichCanImport(PsiFile psiFile, String name) {
    return false;   //todo
  }

  @Override
  protected int getStartOffset(XmlTag element, JavaFxTagNameReference ref) {
    return element.getTextOffset() + ref.getRangeInElement().getStartOffset();
  }

  @Override
  protected int getEndOffset(XmlTag element, JavaFxTagNameReference ref) {
    return element.getTextOffset() + ref.getRangeInElement().getEndOffset();
  }
}
