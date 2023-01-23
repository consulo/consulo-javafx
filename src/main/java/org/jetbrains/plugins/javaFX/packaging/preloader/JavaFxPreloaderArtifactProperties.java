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

import consulo.compiler.CompileContext;
import consulo.compiler.artifact.Artifact;
import consulo.compiler.artifact.ArtifactProperties;
import consulo.compiler.artifact.ui.ArtifactEditorContext;
import consulo.compiler.artifact.ui.ArtifactPropertiesEditor;
import consulo.util.xml.serializer.XmlSerializerUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: anna
 * Date: 3/12/13
 */
public class JavaFxPreloaderArtifactProperties extends ArtifactProperties<JavaFxPreloaderArtifactProperties> {

  private String myPreloaderClass;

  @Override
  public void onBuildFinished(@Nonnull final Artifact artifact, @Nonnull final CompileContext compileContext) {
  }

  @Override
  public ArtifactPropertiesEditor createEditor(@Nonnull ArtifactEditorContext context) {
    return new JavaFxPreloaderArtifactPropertiesEditor(this, context.getProject(), context.getArtifact());
  }

  @Nullable
  @Override
  public JavaFxPreloaderArtifactProperties getState() {
    return this;
  }

  @Override
  public void loadState(JavaFxPreloaderArtifactProperties state) {
    XmlSerializerUtil.copyBean(state, this);
  }

  public String getPreloaderClass() {
    return myPreloaderClass;
  }

  public void setPreloaderClass(String preloaderClass) {
    myPreloaderClass = preloaderClass;
  }
}
