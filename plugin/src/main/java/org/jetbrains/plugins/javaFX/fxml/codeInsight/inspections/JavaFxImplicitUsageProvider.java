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

import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiField;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.ImplicitUsageProvider;
import consulo.language.psi.PsiElement;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import org.jetbrains.plugins.javaFX.indexing.JavaFxIdsIndex;
import org.jetbrains.plugins.javaFX.indexing.JavaFxControllerClassIndex;

import java.util.Collection;
import java.util.List;

/**
 * User: anna
 * Date: 3/22/13
 */
@ExtensionImpl
public class JavaFxImplicitUsageProvider implements ImplicitUsageProvider {

  @Override
  public boolean isImplicitUsage(PsiElement element) {
    return isImplicitWrite(element);
  }

  @Override
  public boolean isImplicitRead(PsiElement element) {
    return false;
  }

  @Override
  public boolean isImplicitWrite(PsiElement element) {
    if (element instanceof PsiField) {
      final String name = ((PsiField)element).getName();
      final PsiClass containingClass = ((PsiField)element).getContainingClass();
      if (containingClass != null) {
        final String qualifiedName = containingClass.getQualifiedName();
        if (qualifiedName != null) {
          final Project project = element.getProject();
          final List<VirtualFile> fxmls = JavaFxControllerClassIndex.findFxmlsWithController(project, qualifiedName);
          if (!fxmls.isEmpty()) {
            final Collection<String> filePaths = JavaFxIdsIndex.getFilePaths(project, name);
            for (VirtualFile fxml : fxmls) {
              if (filePaths.contains(fxml.getPath())) return true;
            }
          }
        }
      }
    }
    return false;
  }
}
