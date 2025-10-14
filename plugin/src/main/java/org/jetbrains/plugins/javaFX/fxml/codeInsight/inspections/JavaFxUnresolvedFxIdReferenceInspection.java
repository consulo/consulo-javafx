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

import com.intellij.java.impl.codeInsight.ExpectedTypeInfo;
import com.intellij.java.impl.codeInsight.ExpectedTypeInfoImpl;
import com.intellij.java.impl.codeInsight.daemon.impl.quickfix.CreateFieldFromUsageFix;
import com.intellij.java.impl.codeInsight.daemon.impl.quickfix.CreateFieldFromUsageHelper;
import com.intellij.java.language.psi.*;
import com.intellij.java.language.util.VisibilityUtil;
import com.intellij.xml.XmlElementDescriptor;
import consulo.annotation.component.ExtensionImpl;
import consulo.java.analysis.impl.localize.JavaQuickFixLocalize;
import consulo.javaFX.editor.inspection.JavaFXInspectionBase;
import consulo.language.editor.FileModificationService;
import consulo.language.editor.completion.lookup.TailType;
import consulo.language.editor.inspection.LocalInspectionToolSession;
import consulo.language.editor.inspection.LocalQuickFix;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.refactoring.NamesValidator;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.PsiReference;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.xml.psi.XmlElementVisitor;
import consulo.xml.psi.xml.XmlAttribute;
import consulo.xml.psi.xml.XmlAttributeValue;
import consulo.xml.psi.xml.XmlFile;
import consulo.xml.psi.xml.XmlTag;
import jakarta.annotation.Nonnull;
import org.jetbrains.plugins.javaFX.fxml.FxmlConstants;
import org.jetbrains.plugins.javaFX.fxml.JavaFxFileTypeFactory;
import org.jetbrains.plugins.javaFX.fxml.JavaFxPsiUtil;
import org.jetbrains.plugins.javaFX.fxml.descriptors.JavaFxClassBackedElementDescriptor;
import org.jetbrains.plugins.javaFX.fxml.refs.JavaFxFieldIdReferenceProvider;

/**
 * @author anna
 */
@ExtensionImpl
public class JavaFxUnresolvedFxIdReferenceInspection extends JavaFXInspectionBase {
    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return LocalizeValue.localizeTODO("Unresolved fx:id attribute reference");
    }

    @Nonnull
    @Override
    public PsiElementVisitor buildVisitor(
        @Nonnull ProblemsHolder holder,
        boolean isOnTheFly,
        @Nonnull LocalInspectionToolSession session,
        @Nonnull Object state
    ) {
        return new XmlElementVisitor() {
            @Override
            public void visitXmlFile(XmlFile file) {
                if (!JavaFxFileTypeFactory.isFxml(file)) {
                    return;
                }
                super.visitXmlFile(file);
            }

            @Override
            public void visitXmlAttribute(XmlAttribute attribute) {
                if (FxmlConstants.FX_ID.equals(attribute.getName())) {
                    final XmlAttributeValue valueElement = attribute.getValueElement();
                    if (valueElement != null && valueElement.getTextLength() > 0) {
                        final PsiClass controllerClass = JavaFxPsiUtil.getControllerClass(attribute.getContainingFile());
                        if (controllerClass != null) {
                            final PsiReference reference = valueElement.getReference();
                            if (reference instanceof JavaFxFieldIdReferenceProvider.JavaFxControllerFieldRef
                                && ((JavaFxFieldIdReferenceProvider.JavaFxControllerFieldRef) reference).isUnresolved()) {
                                final PsiClass fieldClass =
                                    checkContext(((JavaFxFieldIdReferenceProvider.JavaFxControllerFieldRef) reference).getXmlAttributeValue());
                                if (fieldClass != null) {
                                    final String text = reference.getCanonicalText();
                                    final NamesValidator namesValidator = NamesValidator.forLanguage(fieldClass.getLanguage());
                                    boolean validName =
                                        namesValidator != null && namesValidator.isIdentifier(text, fieldClass.getProject());
                                    holder.registerProblem(
                                        reference.getElement(),
                                        reference.getRangeInElement(),
                                        "Unresolved fx:id reference",
                                        isOnTheFly && validName
											? new LocalQuickFix[]{new CreateFieldFromUsageQuickFix(text)}
											: LocalQuickFix.EMPTY_ARRAY
                                    );
                                }
                            }
                        }
                    }
                }
            }
        };
    }

    protected static PsiClass checkContext(final XmlAttributeValue attributeValue) {
        if (attributeValue == null) {
            return null;
        }
        final PsiElement parent = attributeValue.getParent();
        if (parent instanceof XmlAttribute) {
            final XmlTag tag = ((XmlAttribute) parent).getParent();
            if (tag != null) {
                final XmlElementDescriptor descriptor = tag.getDescriptor();
                if (descriptor instanceof JavaFxClassBackedElementDescriptor) {
                    final PsiElement declaration = descriptor.getDeclaration();
                    if (declaration instanceof PsiClass) {
                        return (PsiClass) declaration;
                    }
                }
            }
        }
        return null;
    }

    private static class CreateFieldFromUsageQuickFix implements LocalQuickFix {
        private final String myCanonicalName;

        private CreateFieldFromUsageQuickFix(String canonicalName) {
            myCanonicalName = canonicalName;
        }

        @Nonnull
        @Override
        public LocalizeValue getName() {
            return JavaQuickFixLocalize.createFieldFromUsageText(myCanonicalName);
        }

        @Override
        public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
            final PsiElement psiElement = descriptor.getPsiElement();
            final XmlAttributeValue attrValue = PsiTreeUtil.getParentOfType(psiElement, XmlAttributeValue.class, false);
            assert attrValue != null;

            final JavaFxFieldIdReferenceProvider.JavaFxControllerFieldRef reference =
                (JavaFxFieldIdReferenceProvider.JavaFxControllerFieldRef) attrValue.getReference();
            assert reference != null;

            final PsiClass targetClass = reference.getAClass();
            if (!FileModificationService.getInstance().prepareFileForWrite(targetClass.getContainingFile())) {
                return;
            }
            final PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);
            PsiField field = factory.createField(reference.getCanonicalText(), PsiType.INT);
            VisibilityUtil.setVisibility(field.getModifierList(), PsiModifier.PUBLIC);

            field = CreateFieldFromUsageHelper.insertField(targetClass, field, psiElement);

            final PsiClassType fieldType = factory.createType(checkContext(reference.getXmlAttributeValue()));
            final ExpectedTypeInfo[] types = {
                new ExpectedTypeInfoImpl(
                    fieldType,
                    ExpectedTypeInfo.TYPE_OR_SUBTYPE,
                    fieldType,
                    TailType.NONE,
                    null,
                    ExpectedTypeInfoImpl.NULL
                )
            };
            CreateFieldFromUsageFix.createFieldFromUsageTemplate(targetClass, project, types, field, false, psiElement);
        }
    }
}
