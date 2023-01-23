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
package org.jetbrains.plugins.javaFX.packaging;

import com.intellij.java.execution.JavaExecutionUtil;
import com.intellij.java.execution.impl.ui.ClassBrowser;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.util.InheritanceUtil;
import com.intellij.java.language.util.ClassFilter;
import consulo.application.ApplicationManager;
import consulo.application.util.function.Computable;
import consulo.compiler.artifact.Artifact;
import consulo.compiler.artifact.ArtifactUtil;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.module.Module;
import consulo.project.Project;

import java.util.Collections;
import java.util.Set;

/**
 * User: anna
 * Date: 3/19/13
 */
public class JavaFxApplicationClassBrowser extends ClassBrowser {

  private final Artifact myArtifact;

  public JavaFxApplicationClassBrowser(Project project, Artifact artifact) {
    this(project, artifact, "Choose Application Class");
  }

  public JavaFxApplicationClassBrowser(final Project project,
                                       final Artifact artifact,
                                       final String title) {
    super(project, title);
    myArtifact = artifact;
  }

  @Override
  protected ClassFilter.ClassFilterWithScope getFilter() throws NoFilterException {
    return new ClassFilter.ClassFilterWithScope() {
      @Override
      public GlobalSearchScope getScope() {
        return GlobalSearchScope.projectScope(getProject());
      }

      @Override
      public boolean isAccepted(PsiClass aClass) {
        return InheritanceUtil.isInheritor(aClass, getApplicationClass());
      }
    };
  }

  protected String getApplicationClass() {
    return "javafx.application.Application";
  }

  @Override
  protected PsiClass findClass(String className) {
    final Set<Module> modules = ApplicationManager.getApplication().runReadAction(new Computable<Set<Module>>() {
      @Override
      public Set<Module> compute() {
        return ArtifactUtil.getModulesIncludedInArtifacts(Collections.singletonList(
          myArtifact), getProject());
      }
    });
    for (Module module : modules) {
      final PsiClass aClass = JavaExecutionUtil.findMainClass(getProject(), className, GlobalSearchScope.moduleScope(module));
      if (aClass != null) {
        return aClass;
      }
    }
    return null;
  }
}
