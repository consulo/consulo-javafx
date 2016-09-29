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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.roots.ui.configuration.ChooseModulesDialog;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.packaging.artifacts.ArtifactTemplate;
import com.intellij.packaging.artifacts.ArtifactType;
import com.intellij.packaging.elements.CompositePackagingElement;
import com.intellij.packaging.elements.PackagingElement;
import com.intellij.packaging.elements.PackagingElementFactory;
import com.intellij.packaging.elements.PackagingElementOutputKind;
import com.intellij.packaging.elements.PackagingElementResolvingContext;
import com.intellij.packaging.impl.artifacts.JarArtifactType;
import consulo.java.module.extension.JavaModuleExtension;

/**
 * User: anna
 * Date: 3/12/13
 */
public class JavaFxApplicationArtifactType extends ArtifactType
{
	public static JavaFxApplicationArtifactType getInstance()
	{
		return EP_NAME.findExtension(JavaFxApplicationArtifactType.class);
	}

	protected JavaFxApplicationArtifactType()
	{
		super("javafx", "JavaFx Application");
	}

	@Override
	public boolean isAvailableForAdd(@NotNull ModulesProvider modulesProvider)
	{
		return ModuleUtil.hasModuleExtension(modulesProvider, JavaModuleExtension.class);
	}

	@NotNull
	@Override
	public Icon getIcon()
	{
		return AllIcons.Nodes.Artifact;
	}

	@Nullable
	@Override
	public String getDefaultPathFor(@NotNull PackagingElementOutputKind kind)
	{
		return "/";
	}

	@NotNull
	@Override
	public CompositePackagingElement<?> createRootElement(@NotNull String artifactName)
	{
		return PackagingElementFactory.getInstance().createArtifactRootElement();
	}

	@NotNull
	@Override
	public List<? extends ArtifactTemplate> getNewArtifactTemplates(@NotNull PackagingElementResolvingContext context)
	{
		final List<Module> modules = new ArrayList<Module>();
		Collections.addAll(modules, context.getModulesProvider().getModules());
		if(modules.isEmpty())
		{
			return Collections.emptyList();
		}
		return Collections.singletonList(new JavaFxArtifactTemplate(modules));
	}

	private class JavaFxArtifactTemplate extends ArtifactTemplate
	{
		private final List<Module> myModules;

		public JavaFxArtifactTemplate(List<Module> modules)
		{
			myModules = modules;
		}

		@Override
		public String getPresentableName()
		{
			if(myModules.size() == 1)
			{
				return "From module '" + myModules.get(0).getName() + "'";
			}
			return "From module...";
		}

		@Override
		public NewArtifactConfiguration createArtifact()
		{
			Module module = null;
			if(myModules.size() == 1)
			{
				module = myModules.get(0);
			}
			else
			{
				final ChooseModulesDialog dialog = new ChooseModulesDialog(myModules.get(0).getProject(), myModules, "Select Module", "Selected module output would to be included in the artifact");
				dialog.setSingleSelectionMode();
				dialog.show();
				if(dialog.isOK())
				{
					final List<Module> elements = dialog.getChosenElements();
					if(elements.isEmpty())
					{
						return null;
					}
					module = elements.get(0);
				}
			}
			if(module == null)
			{
				return null;
			}
			final CompositePackagingElement<?> rootElement = JavaFxApplicationArtifactType.this.createRootElement(module.getName());
			final CompositePackagingElement<?> subElement = JarArtifactType.getInstance().createRootElement(FileUtil.sanitizeFileName(module.getName()));
			final PackagingElement<?> moduleOutputElement = PackagingElementFactory.getInstance().createModuleOutput(module);
			subElement.addFirstChild(moduleOutputElement);
			rootElement.addFirstChild(subElement);
			return new NewArtifactConfiguration(rootElement, module.getName(), JavaFxApplicationArtifactType.this);
		}
	}
}
