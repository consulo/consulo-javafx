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

import consulo.language.file.FileTypeManager;
import consulo.language.psi.*;
import consulo.language.psi.path.FileReferenceSet;
import consulo.language.util.ProcessingContext;
import consulo.util.collection.ArrayUtil;
import consulo.util.lang.function.Condition;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.fileType.FileType;
import consulo.xml.psi.xml.XmlAttributeValue;

import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: anna
 */
class JavaFxLocationReferenceProvider extends PsiReferenceProvider {
  private boolean mySupportCommaInValue = false;
  private final FileType[] myAcceptedFileTypes;

  JavaFxLocationReferenceProvider() {
    this(false);
  }

  JavaFxLocationReferenceProvider(boolean supportCommaInValue, String... acceptedFileTypes) {
    mySupportCommaInValue = supportCommaInValue;
    myAcceptedFileTypes = new FileType[acceptedFileTypes.length];
    final FileTypeManager fileTypeManager = FileTypeManager.getInstance();
    for (int i = 0; i < acceptedFileTypes.length; i++) {
      myAcceptedFileTypes[i] = fileTypeManager.getFileTypeByExtension(acceptedFileTypes[i]);
    }
  }

  @Nonnull
  @Override
  public PsiReference[] getReferencesByElement(@Nonnull final PsiElement element,
                                               @Nonnull ProcessingContext context) {
    final String value = ((XmlAttributeValue)element).getValue();
    if (value.startsWith("@")) {
      return new FileReferenceSet(value.substring(1), element, 2, null, true).getAllReferences();
    }
    else {
      if (mySupportCommaInValue && value.contains(",")) {
        int startIdx = 0;
        int endIdx = 0;
        List<PsiReference> refs = new ArrayList<PsiReference>();
        while (true) {
          endIdx = value.indexOf(",", startIdx);
          Collections.addAll(refs,
                             collectRefs(element,
                                         endIdx >= 0 ? value.substring(startIdx, endIdx) : value.substring(startIdx),
                                         startIdx + 1,
                                         myAcceptedFileTypes));
          startIdx = endIdx + 1;
          if (endIdx < 0) {
            break;
          }
        }
        return refs.toArray(new PsiReference[refs.size()]);
      }
      else {
        return collectRefs(element, value, 1, myAcceptedFileTypes);
      }
    }
  }

  private static PsiReference[] collectRefs(PsiElement element,
                                            String value,
                                            final int startInElement,
                                            final FileType... acceptedFileTypes) {
    final FileReferenceSet set = new FileReferenceSet(value, element, startInElement, null, true) {
      @Override
      public consulo.util.lang.function.Condition<PsiFileSystemItem> getReferenceCompletionFilter() {
        return new Condition<PsiFileSystemItem>() {
          @Override
          public boolean value(PsiFileSystemItem item) {
            if (item instanceof PsiDirectory) return true;
            final VirtualFile virtualFile = item.getVirtualFile();
            if (virtualFile == null) return false;
            final FileType fileType = virtualFile.getFileType();
            return ArrayUtil.find(acceptedFileTypes, fileType) >= 0;
          }
        };
      }
    };
    if (value.startsWith("/")) {
      set.addCustomization(FileReferenceSet.DEFAULT_PATH_EVALUATOR_OPTION, FileReferenceSet.ABSOLUTE_TOP_LEVEL);
    }
    return set.getAllReferences();
  }
}
