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
package org.jetbrains.plugins.javaFX.packaging.preloader;

import consulo.compiler.artifact.Artifact;
import consulo.compiler.artifact.ui.ArtifactPropertiesEditor;
import consulo.disposer.Disposable;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.ui.Component;
import consulo.ui.TextBox;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.util.FormBuilder;
import consulo.util.lang.StringUtil;
import org.jetbrains.plugins.javaFX.localize.JavaFXLocalize;
import org.jetbrains.plugins.javaFX.packaging.JavaFxApplicationClassBrowser;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * User: anna
 * Date: 3/19/13
 */
public class JavaFxPreloaderArtifactPropertiesEditor extends ArtifactPropertiesEditor {
    private final JavaFxPreloaderArtifactProperties myProperties;
    private final Project myProject;
    private final Artifact myArtifact;

    private @Nullable TextBox myPreloaderField;

    public JavaFxPreloaderArtifactPropertiesEditor(JavaFxPreloaderArtifactProperties properties, Project project, Artifact artifact) {
        myProperties = properties;
        myProject = project;
        myArtifact = artifact;
    }

    @Override
    public LocalizeValue getTabName() {
        return JavaFXLocalize.artifactPreloaderTab();
    }

    @Override
    @RequiredUIAccess
    public Component createUIComponent(Disposable uiDisposable) {
        TextBox preloaderField = TextBox.create();
        JavaFxApplicationClassBrowser.installOn(
            preloaderField,
            myProject,
            myArtifact,
            JavaFXLocalize.artifactChoosePreloaderClassTitle(),
            "javafx.application.Preloader"
        );
        myPreloaderField = preloaderField;

        FormBuilder formBuilder = FormBuilder.create();
        formBuilder.addLabeled(JavaFXLocalize.artifactPreloaderClassLabel(), preloaderField);
        return formBuilder.build();
    }

    @Override
    @RequiredUIAccess
    public boolean isModified() {
        TextBox preloaderField = myPreloaderField;
        return preloaderField != null
            && !Objects.equals(StringUtil.notNullize(preloaderField.getValue()), StringUtil.notNullize(myProperties.getPreloaderClass()));
    }

    @Override
    @RequiredUIAccess
    public void apply() {
        TextBox preloaderField = myPreloaderField;
        if (preloaderField != null) {
            myProperties.setPreloaderClass(StringUtil.notNullize(preloaderField.getValue()));
        }
    }

    @Override
    @RequiredUIAccess
    public void reset() {
        TextBox preloaderField = myPreloaderField;
        if (preloaderField != null) {
            preloaderField.setValue(StringUtil.notNullize(myProperties.getPreloaderClass()));
        }
    }
}
