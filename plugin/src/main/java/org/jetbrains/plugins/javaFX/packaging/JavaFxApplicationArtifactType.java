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

import com.intellij.java.compiler.artifact.impl.artifacts.JarArtifactType;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.AllIcons;
import consulo.compiler.artifact.ArtifactTemplate;
import consulo.compiler.artifact.ArtifactType;
import consulo.compiler.artifact.element.*;
import consulo.ide.impl.idea.openapi.roots.ui.configuration.ChooseModulesDialog;
import consulo.java.language.module.extension.JavaModuleExtension;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.module.content.layer.ModulesProvider;
import consulo.ui.image.Image;
import consulo.util.io.FileUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: anna
 * Date: 3/12/13
 */
@ExtensionImpl
public class JavaFxApplicationArtifactType extends ArtifactType {
  public static JavaFxApplicationArtifactType getInstance() {
    return EP_NAME.findExtension(JavaFxApplicationArtifactType.class);
  }

  public JavaFxApplicationArtifactType() {
    super("javafx", "JavaFx Application");
  }

  @Override
  public boolean isAvailableForAdd(@Nonnull ModulesProvider modulesProvider) {
    return ModuleUtilCore.hasModuleExtension(modulesProvider, JavaModuleExtension.class);
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
    return factory.createArtifactRootElement();
  }

  @Nonnull
  @Override
  public List<? extends ArtifactTemplate> getNewArtifactTemplates(@Nonnull PackagingElementResolvingContext context) {
    final List<Module> modules = new ArrayList<>();
    Collections.addAll(modules, context.getModulesProvider().getModules());
    if (modules.isEmpty()) {
      return Collections.emptyList();
    }
    return Collections.singletonList(new JavaFxArtifactTemplate(modules));
  }

  private class JavaFxArtifactTemplate extends ArtifactTemplate {
    private final List<Module> myModules;

    public JavaFxArtifactTemplate(List<Module> modules) {
      myModules = modules;
    }

    @Override
    public String getPresentableName() {
      if (myModules.size() == 1) {
        return "From module '" + myModules.get(0).getName() + "'";
      }
      return "From module...";
    }

    @Override
    public NewArtifactConfiguration createArtifact() {
      Module module = null;
      if (myModules.size() == 1) {
        module = myModules.get(0);
      }
      else {
        final ChooseModulesDialog dialog =
          new ChooseModulesDialog(myModules.get(0).getProject(),
                                  myModules,
                                  "Select Module",
                                  "Selected module output would to be included in the artifact");
        dialog.setSingleSelectionMode();
        dialog.show();
        if (dialog.isOK()) {
          final List<Module> elements = dialog.getChosenElements();
          if (elements.isEmpty()) {
            return null;
          }
          module = elements.get(0);
        }
      }
      if (module == null) {
        return null;
      }
      PackagingElementFactory factory = PackagingElementFactory.getInstance(module.getProject());
      final CompositePackagingElement<?> rootElement = JavaFxApplicationArtifactType.this.createRootElement(factory, module.getName());
      final CompositePackagingElement<?> subElement =
        JarArtifactType.getInstance().createRootElement(factory, FileUtil.sanitizeFileName(module.getName()));
      final PackagingElement<?> moduleOutputElement = ProductionModuleOutputElementType.getInstance()
                                                                                       .createElement(module.getProject(),
                                                                                                      ModuleUtilCore.createPointer(module));
      subElement.addFirstChild(moduleOutputElement);
      rootElement.addFirstChild(subElement);
      return new NewArtifactConfiguration(rootElement, module.getName(), JavaFxApplicationArtifactType.this);
    }
  }
}
