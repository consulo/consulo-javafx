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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.plugins.javaFX.fxml.JavaFxFileTypeFactory;
import org.jetbrains.plugins.javaFX.fxml.JavaFxPsiUtil;
import org.jetbrains.plugins.javaFX.fxml.codeInsight.JavaFxImportsOptimizer;
import com.intellij.codeInsight.FileModificationService;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.XmlSuppressableInspectionTool;
import com.intellij.lang.ImportOptimizer;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlProcessingInstruction;
import consulo.java.JavaQuickFixBundle;

/**
 * User: anna
 * Date: 4/18/13
 */
public class JavaFxUnusedImportsInspection extends XmlSuppressableInspectionTool
{
	@Nullable
	@Override
	public ProblemDescriptor[] checkFile(@Nonnull PsiFile file, @Nonnull InspectionManager manager, final boolean isOnTheFly)
	{
		if(!JavaFxFileTypeFactory.isFxml(file))
		{
			return null;
		}
		final XmlDocument document = ((XmlFile) file).getDocument();
		if(document == null)
		{
			return null;
		}
		final Set<String> usedNames = new HashSet<String>();
		file.accept(new JavaFxImportsOptimizer.JavaFxUsedClassesVisitor()
		{
			@Override
			protected void appendClassName(String fqn)
			{
				usedNames.add(fqn);
				final String packageName = StringUtil.getPackageName(fqn);
				if(!StringUtil.isEmpty(packageName))
				{
					usedNames.add(packageName);
				}
			}
		});

		final InspectionManager inspectionManager = InspectionManager.getInstance(file.getProject());

		final List<ProblemDescriptor> problems = new ArrayList<ProblemDescriptor>();
		final Collection<XmlProcessingInstruction> instructions = PsiTreeUtil.findChildrenOfType(document.getProlog(), XmlProcessingInstruction.class);
		final Map<String, XmlProcessingInstruction> targetProcessingInstructions = new LinkedHashMap<String, XmlProcessingInstruction>();
		for(XmlProcessingInstruction instruction : instructions)
		{
			final String target = JavaFxPsiUtil.getInstructionTarget("import", instruction);
			if(target != null)
			{
				targetProcessingInstructions.put(target, instruction);
			}
		}
		for(String target : targetProcessingInstructions.keySet())
		{
			final XmlProcessingInstruction instruction = targetProcessingInstructions.get(target);
			if(target.endsWith(".*"))
			{
				if(!usedNames.contains(StringUtil.trimEnd(target, ".*")))
				{
					problems.add(inspectionManager.createProblemDescriptor(instruction, "Unused import", new JavaFxOptimizeImportsFix(), ProblemHighlightType.LIKE_UNUSED_SYMBOL, isOnTheFly));
				}
			}
			else if(!usedNames.contains(target) || targetProcessingInstructions.containsKey(StringUtil.getPackageName(target) + ".*"))
			{
				problems.add(inspectionManager.createProblemDescriptor(instruction, "Unused import", new JavaFxOptimizeImportsFix(), ProblemHighlightType.LIKE_UNUSED_SYMBOL, isOnTheFly));
			}
		}
		return problems.isEmpty() ? null : problems.toArray(new ProblemDescriptor[problems.size()]);
	}

	private static class JavaFxOptimizeImportsFix implements LocalQuickFix
	{
		@Nonnull
		@Override
		public String getName()
		{
			return JavaQuickFixBundle.message("optimize.imports.fix");
		}

		@Override
		@Nonnull
		public String getFamilyName()
		{
			return JavaQuickFixBundle.message("optimize.imports.fix");
		}

		@Override
		public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor)
		{
			final PsiElement psiElement = descriptor.getPsiElement();
			if(psiElement == null)
			{
				return;
			}
			final PsiFile file = psiElement.getContainingFile();
			if(file == null || !JavaFxFileTypeFactory.isFxml(file))
			{
				return;
			}
			if(!FileModificationService.getInstance().prepareFileForWrite(file))
			{
				return;
			}
			ImportOptimizer optimizer = new JavaFxImportsOptimizer();
			final Runnable runnable = optimizer.processFile(file);
			new WriteCommandAction.Simple(project, getFamilyName(), file)
			{
				@Override
				protected void run() throws Throwable
				{
					runnable.run();
				}
			}.execute();
		}
	}
}
