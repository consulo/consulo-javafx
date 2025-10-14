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

import consulo.annotation.component.ExtensionImpl;
import consulo.java.analysis.impl.localize.JavaQuickFixLocalize;
import consulo.javaFX.editor.inspection.JavaFXInspectionBase;
import consulo.language.editor.FileModificationService;
import consulo.language.editor.WriteCommandAction;
import consulo.language.editor.inspection.LocalQuickFix;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.scheme.InspectionManager;
import consulo.language.editor.refactoring.ImportOptimizer;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.util.lang.StringUtil;
import consulo.xml.psi.xml.XmlDocument;
import consulo.xml.psi.xml.XmlFile;
import consulo.xml.psi.xml.XmlProcessingInstruction;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.javaFX.fxml.JavaFxFileTypeFactory;
import org.jetbrains.plugins.javaFX.fxml.JavaFxPsiUtil;
import org.jetbrains.plugins.javaFX.fxml.codeInsight.JavaFxImportsOptimizer;

import java.util.*;

/**
 * @author anna
 * @since 2013-04-18
 */
@ExtensionImpl
public class JavaFxUnusedImportsInspection extends JavaFXInspectionBase {
    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return LocalizeValue.localizeTODO("JavaFX unused imports");
    }

    @Nullable
    @Override
    public ProblemDescriptor[] checkFile(@Nonnull PsiFile file, @Nonnull InspectionManager manager, final boolean isOnTheFly) {
        if (!JavaFxFileTypeFactory.isFxml(file)) {
            return null;
        }
        final XmlDocument document = ((XmlFile) file).getDocument();
        if (document == null) {
            return null;
        }
        final Set<String> usedNames = new HashSet<String>();
        file.accept(new JavaFxImportsOptimizer.JavaFxUsedClassesVisitor() {
            @Override
            protected void appendClassName(String fqn) {
                usedNames.add(fqn);
                final String packageName = StringUtil.getPackageName(fqn);
                if (!StringUtil.isEmpty(packageName)) {
                    usedNames.add(packageName);
                }
            }
        });

        final InspectionManager inspectionManager = InspectionManager.getInstance(file.getProject());

        final List<ProblemDescriptor> problems = new ArrayList<ProblemDescriptor>();
        final Collection<XmlProcessingInstruction> instructions =
            PsiTreeUtil.findChildrenOfType(document.getProlog(), XmlProcessingInstruction.class);
        final Map<String, XmlProcessingInstruction> targetProcessingInstructions = new LinkedHashMap<String, XmlProcessingInstruction>();
        for (XmlProcessingInstruction instruction : instructions) {
            final String target = JavaFxPsiUtil.getInstructionTarget("import", instruction);
            if (target != null) {
                targetProcessingInstructions.put(target, instruction);
            }
        }
        for (String target : targetProcessingInstructions.keySet()) {
            final XmlProcessingInstruction instruction = targetProcessingInstructions.get(target);
            if (target.endsWith(".*")) {
                if (!usedNames.contains(StringUtil.trimEnd(target, ".*"))) {
                    problems.add(inspectionManager.createProblemDescriptor(
                        instruction,
                        "Unused import",
                        new JavaFxOptimizeImportsFix(),
                        ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                        isOnTheFly
                    ));
                }
            }
            else if (!usedNames.contains(target) || targetProcessingInstructions.containsKey(StringUtil.getPackageName(target) + ".*")) {
                problems.add(inspectionManager.createProblemDescriptor(
                    instruction,
                    "Unused import",
                    new JavaFxOptimizeImportsFix(),
                    ProblemHighlightType.LIKE_UNUSED_SYMBOL,
                    isOnTheFly
                ));
            }
        }
        return problems.isEmpty() ? null : problems.toArray(new ProblemDescriptor[problems.size()]);
    }

    private static class JavaFxOptimizeImportsFix implements LocalQuickFix {
        @Nonnull
        @Override
        public LocalizeValue getName() {
            return JavaQuickFixLocalize.optimizeImportsFix();
        }

        @Override
        public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
            final PsiElement psiElement = descriptor.getPsiElement();
            if (psiElement == null) {
                return;
            }
            final PsiFile file = psiElement.getContainingFile();
            if (file == null || !JavaFxFileTypeFactory.isFxml(file)) {
                return;
            }
            if (!FileModificationService.getInstance().prepareFileForWrite(file)) {
                return;
            }
            ImportOptimizer optimizer = new JavaFxImportsOptimizer();
            final Runnable runnable = optimizer.processFile(file);
            new WriteCommandAction.Simple(project, getName().get(), file) {
                @Override
                protected void run() throws Throwable {
                    runnable.run();
                }
            }.execute();
        }
    }
}
