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

import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.PsiReferenceProvider;
import consulo.language.psi.path.FileReferenceSet;
import consulo.language.util.ProcessingContext;
import consulo.xml.psi.xml.XmlAttributeValue;

import javax.annotation.Nonnull;

/**
 * User: anna
 */
class JavaFxSourceReferenceProvider extends PsiReferenceProvider
{
  @Nonnull
  @Override
  public PsiReference[] getReferencesByElement(@Nonnull final PsiElement element,
                                               @Nonnull ProcessingContext context) {
    final FileReferenceSet set = new FileReferenceSet(((XmlAttributeValue)element).getValue(), element, 1, null, true);
    return set.getAllReferences();
  }
}
