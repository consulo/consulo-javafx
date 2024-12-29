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
package org.jetbrains.plugins.javaFX.fxml.codeInsight.inspections;

import com.intellij.xml.XmlElementDescriptor;
import consulo.annotation.component.ExtensionImpl;
import consulo.javaFX.editor.inspection.JavaFXInspectionBase;
import consulo.language.editor.inspection.LocalInspectionToolSession;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.psi.PsiElementVisitor;
import consulo.util.lang.Comparing;
import consulo.xml.psi.XmlElementVisitor;
import consulo.xml.psi.xml.XmlTag;
import org.jetbrains.plugins.javaFX.fxml.JavaFxPsiUtil;
import org.jetbrains.plugins.javaFX.fxml.descriptors.JavaFxPropertyElementDescriptor;

import jakarta.annotation.Nonnull;

/**
 * User: anna
 */
@ExtensionImpl
public class JavaFxDefaultTagInspection extends JavaFXInspectionBase
{
	@Nonnull
	@Override
	public String getDisplayName()
	{
		return "Unnecessary default tag";
	}

	@Nonnull
	@Override
	public PsiElementVisitor buildVisitor(final @Nonnull ProblemsHolder holder,
										  boolean isOnTheFly,
										  @Nonnull LocalInspectionToolSession session,
										  @Nonnull Object state)
	{
		return new XmlElementVisitor()
		{
			@Override
			public void visitXmlTag(XmlTag tag)
			{
				super.visitXmlTag(tag);
				final XmlElementDescriptor descriptor = tag.getDescriptor();
				if(descriptor instanceof JavaFxPropertyElementDescriptor)
				{
					final XmlTag parentTag = tag.getParentTag();
					if(parentTag != null)
					{
						final String propertyName = JavaFxPsiUtil.getDefaultPropertyName(JavaFxPsiUtil.getTagClass(parentTag));
						final String tagName = tag.getName();
						if(Comparing.strEqual(tagName, propertyName))
						{
							holder.registerProblem(tag.getFirstChild(),
									"Default property tag could be removed",
									ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
									new UnwrapTagFix(tagName));
						}
					}
				}
			}
		};
	}
}
