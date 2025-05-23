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
import consulo.project.Project;
import consulo.ui.ex.awt.TextFieldWithBrowseButton;
import consulo.util.lang.Comparing;
import org.jetbrains.plugins.javaFX.packaging.JavaFxApplicationClassBrowser;

import jakarta.annotation.Nullable;
import javax.swing.*;

/**
 * User: anna
 * Date: 3/19/13
 */
public class JavaFxPreloaderArtifactPropertiesEditor extends ArtifactPropertiesEditor
{
  private final JavaFxPreloaderArtifactProperties myProperties;
  private JPanel myPanel;
  private TextFieldWithBrowseButton myPreloaderTf;
  
  public JavaFxPreloaderArtifactPropertiesEditor(JavaFxPreloaderArtifactProperties properties, Project project, Artifact artifact) {
    super();
    myProperties = properties;
    new JavaFxApplicationClassBrowser(project, artifact, "Choose Preloader Class") {
      @Override
      protected String getApplicationClass() {
        return "javafx.application.Preloader";
      }
    }.setField(myPreloaderTf);
  }

  @Override
  public String getTabName() {
    return "JavaFX Preloader";
  }

  @Nullable
  @Override
  public JComponent createComponent() {
    return myPanel;
  }

  @Override
  public boolean isModified() {
    return !Comparing.strEqual(myPreloaderTf.getText(), myProperties.getPreloaderClass());
  }

  @Override
  public void apply() {
    myProperties.setPreloaderClass(myPreloaderTf.getText());
  }

  /*@Nullable
  @Override
  public String getHelpId() {
    return "Project_Structure_Artifacts_Java_FX_tab";
  }       */

  @Override
  public void reset() {
    myPreloaderTf.setText(myProperties.getPreloaderClass());
  }

  @Override
  public void disposeUIResources() {
  }
}
