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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.packaging.artifacts.ArtifactType;
import com.intellij.packaging.elements.CompositePackagingElement;
import com.intellij.packaging.elements.PackagingElementFactory;
import com.intellij.packaging.elements.PackagingElementOutputKind;
import com.intellij.packaging.impl.artifacts.JarArtifactType;
import consulo.java.module.extension.JavaModuleExtension;
import consulo.ui.image.Image;

/**
 * User: anna
 * Date: 3/12/13
 */
public class JavaFxPreloaderArtifactType extends ArtifactType
{
	public static JavaFxPreloaderArtifactType getInstance()
	{
		return EP_NAME.findExtension(JavaFxPreloaderArtifactType.class);
	}

	@Override
	public boolean isAvailableForAdd(@Nonnull ModulesProvider modulesProvider)
	{
		return ModuleUtil.hasModuleExtension(modulesProvider, JavaModuleExtension.class);
	}

	protected JavaFxPreloaderArtifactType()
	{
		super("javafx-preloader", "JavaFx Preloader");
	}

	@Nonnull
	@Override
	public Image getIcon()
	{
		return AllIcons.Nodes.Artifact;
	}

	@Nullable
	@Override
	public String getDefaultPathFor(@Nonnull PackagingElementOutputKind kind)
	{
		return "/";
	}

	@Nonnull
	@Override
	public CompositePackagingElement<?> createRootElement(@Nonnull PackagingElementFactory factory, @Nonnull String artifactName)
	{
		return JarArtifactType.getInstance().createRootElement(factory, artifactName);
	}
}
