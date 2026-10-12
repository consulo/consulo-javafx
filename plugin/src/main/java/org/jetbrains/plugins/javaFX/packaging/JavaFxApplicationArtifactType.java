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
import consulo.compiler.artifact.element.CompositePackagingElement;
import consulo.compiler.artifact.element.PackagingElement;
import consulo.compiler.artifact.element.PackagingElementFactory;
import consulo.compiler.artifact.element.PackagingElementOutputKind;
import consulo.compiler.artifact.element.PackagingElementResolvingContext;
import consulo.compiler.artifact.element.ProductionModuleOutputElementType;
import consulo.java.language.module.extension.JavaModuleExtension;
import consulo.language.util.ModuleUtilCore;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.module.content.layer.ModulesProvider;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.dialog.ChooseItemsDialogDescriptor;
import consulo.ui.image.Image;
import consulo.util.io.FileUtil;
import org.jetbrains.plugins.javaFX.localize.JavaFXLocalize;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;

/**
 * User: anna
 * Date: 3/12/13
 */
@ExtensionImpl
public class JavaFxApplicationArtifactType extends ArtifactType {
    public JavaFxApplicationArtifactType() {
        super("javafx", JavaFXLocalize.artifactTypeApplication());
    }

    @Override
    public boolean isAvailableForAdd(ModulesProvider modulesProvider) {
        return ModuleUtilCore.hasModuleExtension(modulesProvider, JavaModuleExtension.class);
    }

    @Override
    public Image getIcon() {
        return AllIcons.Nodes.Artifact;
    }

    @Override
    public @Nullable String getDefaultPathFor(PackagingElementOutputKind kind) {
        return "/";
    }

    @Override
    public CompositePackagingElement<?> createRootElement(PackagingElementFactory factory, String artifactName) {
        return factory.createArtifactRootElement();
    }

    @Override
    public List<? extends ArtifactTemplate> getNewArtifactTemplates(PackagingElementResolvingContext context) {
        List<Module> modules = List.of(context.getModulesProvider().getModules());
        if (modules.isEmpty()) {
            return List.of();
        }
        return List.of(new JavaFxArtifactTemplate(modules));
    }

    private class JavaFxArtifactTemplate extends ArtifactTemplate {
        private final List<Module> myModules;

        public JavaFxArtifactTemplate(List<Module> modules) {
            myModules = modules;
        }

        @Override
        public LocalizeValue getPresentableName() {
            if (myModules.size() == 1) {
                return JavaFXLocalize.artifactTemplateFromModule(myModules.get(0).getName());
            }
            return JavaFXLocalize.artifactTemplateFromModules();
        }

        @Override
        @RequiredUIAccess
        public CompletableFuture<NewArtifactConfiguration> createArtifact() {
            if (myModules.size() == 1) {
                return CompletableFuture.completedFuture(createConfiguration(myModules.get(0)));
            }

            return ChooseItemsDialogDescriptor.showAsync(
                null,
                JavaFXLocalize.artifactChooseModuleTitle(),
                JavaFXLocalize.artifactChooseModuleDescription(),
                myModules,
                (module, presentation) -> {
                    presentation.withIcon(PlatformIconGroup.nodesModule());
                    presentation.append(module.getName());
                },
                false,
                List.of()
            ).thenApply(modules -> {
                if (modules.isEmpty()) {
                    throw new CancellationException();
                }
                return createConfiguration(modules.get(0));
            });
        }

        private NewArtifactConfiguration createConfiguration(Module module) {
            PackagingElementFactory factory = PackagingElementFactory.getInstance(module.getProject());
            CompositePackagingElement<?> rootElement = JavaFxApplicationArtifactType.this.createRootElement(factory, module.getName());
            CompositePackagingElement<?> subElement =
                JarArtifactType.getInstance().createRootElement(factory, FileUtil.sanitizeFileName(module.getName()));
            PackagingElement<?> moduleOutputElement =
                ProductionModuleOutputElementType.getInstance().createElement(module.getProject(), ModuleUtilCore.createPointer(module));
            subElement.addFirstChild(moduleOutputElement);
            rootElement.addFirstChild(subElement);
            return new NewArtifactConfiguration(rootElement, module.getName(), JavaFxApplicationArtifactType.this);
        }
    }
}
