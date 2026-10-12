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
import consulo.application.ReadAction;
import consulo.compiler.artifact.Artifact;
import consulo.compiler.artifact.ArtifactUtil;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.project.Project;
import consulo.ui.TextBox;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.ActionGroup;
import consulo.ui.ex.action.ActionToolbar;
import consulo.ui.ex.action.ActionToolbarFactory;
import consulo.ui.ex.action.DumbAwareAction;
import org.jetbrains.plugins.javaFX.localize.JavaFXLocalize;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * User: anna
 * Date: 3/19/13
 */
public class JavaFxApplicationClassBrowser extends ClassBrowser {
    private final Artifact myArtifact;
    private final String myApplicationClass;

    public JavaFxApplicationClassBrowser(Project project, Artifact artifact) {
        this(project, artifact, JavaFXLocalize.artifactChooseApplicationClassTitle(), "javafx.application.Application");
    }

    public JavaFxApplicationClassBrowser(Project project, Artifact artifact, LocalizeValue title, String applicationClass) {
        super(project, title.get());
        myArtifact = artifact;
        myApplicationClass = applicationClass;
    }

    @RequiredUIAccess
    public static void installOn(TextBox field, Project project, Artifact artifact, LocalizeValue title, String applicationClass) {
        if (project.getApplication().isUnifiedApplication()) {
            return;
        }

        JavaFxApplicationClassBrowser browser = new JavaFxApplicationClassBrowser(project, artifact, title, applicationClass);
        ActionGroup actions = ActionGroup.newImmutableBuilder()
            .add(DumbAwareAction.create(title, LocalizeValue.empty(), PlatformIconGroup.nodesClass(), e -> {
                String className = browser.chooseClass(field.getValue());
                if (className != null) {
                    field.setValue(className);
                }
            }))
            .build();

        ActionToolbar toolbar = ActionToolbarFactory.getInstance()
            .createActionToolbar("JavaFxApplicationClass", actions, ActionToolbar.Style.INPLACE);
        toolbar.setTargetUIComponent(field);
        toolbar.updateActionsAsync();
        field.setSuffixComponent(toolbar.getUIComponent());
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
                return InheritanceUtil.isInheritor(aClass, myApplicationClass);
            }
        };
    }

    @Override
    protected @Nullable PsiClass findClass(String className) {
        Set<Module> modules = ReadAction.compute(() -> ArtifactUtil.getModulesIncludedInArtifacts(List.of(myArtifact), getProject()));
        for (Module module : modules) {
            PsiClass aClass = JavaExecutionUtil.findMainClass(getProject(), className, GlobalSearchScope.moduleScope(module));
            if (aClass != null) {
                return aClass;
            }
        }
        return null;
    }
}
