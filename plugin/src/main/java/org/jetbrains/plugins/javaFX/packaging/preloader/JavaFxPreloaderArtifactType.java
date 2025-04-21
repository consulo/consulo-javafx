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

import com.intellij.java.compiler.artifact.impl.artifacts.JarArtifactType;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.AllIcons;
import consulo.compiler.artifact.ArtifactType;
import consulo.compiler.artifact.element.CompositePackagingElement;
import consulo.compiler.artifact.element.PackagingElementFactory;
import consulo.compiler.artifact.element.PackagingElementOutputKind;
import consulo.java.language.module.extension.JavaModuleExtension;
import consulo.language.util.ModuleUtilCore;
import consulo.localize.LocalizeValue;
import consulo.module.content.layer.ModulesProvider;
import consulo.ui.image.Image;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * User: anna
 * Date: 3/12/13
 */
@ExtensionImpl
public class JavaFxPreloaderArtifactType extends ArtifactType {
  @Override
  public boolean isAvailableForAdd(@Nonnull ModulesProvider modulesProvider) {
    return ModuleUtilCore.hasModuleExtension(modulesProvider, JavaModuleExtension.class);
  }

  public JavaFxPreloaderArtifactType() {
    super("javafx-preloader", LocalizeValue.localizeTODO("JavaFx Preloader"));
  }

  @Nonnull
  @Override
  public Image getIcon() {
    return AllIcons.Nodes.Artifact;
  }

  @Nullable
  @Override
  public String getDefaultPathFor(@Nonnull PackagingElementOutputKind kind) {
    return "/";
  }

  @Nonnull
  @Override
  public CompositePackagingElement<?> createRootElement(@Nonnull PackagingElementFactory factory, @Nonnull String artifactName) {
    return JarArtifactType.getInstance().createRootElement(factory, artifactName);
  }
}
