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

import com.intellij.java.language.psi.*;
import com.intellij.java.language.psi.util.PropertyUtil;
import consulo.annotation.component.ExtensionImpl;
import consulo.content.scope.SearchScope;
import consulo.javaFX.fxml.FXMLFileType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.UseScopeEnlarger;
import consulo.language.psi.scope.DelegatingGlobalSearchScope;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import org.jetbrains.plugins.javaFX.JavaFxControllerClassIndex;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: anna
 */
@ExtensionImpl
public class JavaFxScopeEnlarger implements UseScopeEnlarger {
  @Nullable
  @Override
  public SearchScope getAdditionalUseScope(@Nonnull PsiElement element) {
    PsiClass containingClass = null;
    if (element instanceof PsiField) {
      containingClass = ((PsiField)element).getContainingClass();
    }
    else if (element instanceof PsiParameter) {
      final PsiElement declarationScope = ((PsiParameter)element).getDeclarationScope();
      if (declarationScope instanceof PsiMethod && PropertyUtil.isSimplePropertySetter((PsiMethod)declarationScope)) {
        containingClass = ((PsiMethod)declarationScope).getContainingClass();
      }
    }

    if (containingClass != null) {
      if (element instanceof PsiField && ((PsiField)element).hasModifierProperty(PsiModifier.PRIVATE) || element instanceof PsiParameter) {
        final Project project = element.getProject();
        final String qualifiedName = containingClass.getQualifiedName();
        if (qualifiedName != null && !JavaFxControllerClassIndex.findFxmlWithController(project, qualifiedName).isEmpty()) {
          final GlobalSearchScope projectScope = GlobalSearchScope.projectScope(project);
          return new DelegatingGlobalSearchScope(projectScope){
            @Override
            public boolean contains(@Nonnull VirtualFile file) {
              return super.contains(file) && FXMLFileType.isFxml(file);
            }
          };
        }
      }
    } 

    return null;
  }
}
