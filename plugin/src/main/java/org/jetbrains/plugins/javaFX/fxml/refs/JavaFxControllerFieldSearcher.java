// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.javaFX.fxml.refs;

import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiField;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.ApplicationManager;
import consulo.application.ReadAction;
import consulo.application.util.function.Processor;
import consulo.content.scope.SearchScope;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiReference;
import consulo.language.psi.PsiUtilCore;
import consulo.language.psi.search.ReferencesSearch;
import consulo.language.psi.search.ReferencesSearchQueryExecutor;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import consulo.xml.psi.XmlRecursiveElementVisitor;
import consulo.xml.psi.xml.XmlAttribute;
import consulo.xml.psi.xml.XmlAttributeValue;
import org.jetbrains.plugins.javaFX.fxml.FxmlConstants;
import org.jetbrains.plugins.javaFX.indexing.JavaFxControllerClassIndex;

import jakarta.annotation.Nonnull;
import java.util.List;

@ExtensionImpl
public class JavaFxControllerFieldSearcher implements ReferencesSearchQueryExecutor {
  @Override
  public boolean execute(@Nonnull final ReferencesSearch.SearchParameters queryParameters,
                         @Nonnull final Processor<? super PsiReference> consumer) {
    final PsiElement elementToSearch = queryParameters.getElementToSearch();
    if (elementToSearch instanceof PsiField) {
      final PsiField field = (PsiField)elementToSearch;
      final PsiClass containingClass = ReadAction.compute(() -> field.getContainingClass());
      if (containingClass != null) {
        final String qualifiedName = ReadAction.compute(() -> containingClass.getQualifiedName());
        if (qualifiedName != null) {
          Project project = PsiUtilCore.getProjectInReadAction(containingClass);
          final List<PsiFile> fxmlWithController =
            JavaFxControllerClassIndex.findFxmlWithController(project, qualifiedName);
          for (final PsiFile file : fxmlWithController) {
            ApplicationManager.getApplication().runReadAction(() -> {
              final String fieldName = field.getName();
              if (fieldName == null) {
                return;
              }
              final VirtualFile virtualFile = file.getViewProvider().getVirtualFile();
              final SearchScope searchScope = queryParameters.getEffectiveSearchScope();
              if (searchScope.contains(virtualFile)) {
                file.accept(new XmlRecursiveElementVisitor() {
                  @Override
                  public void visitXmlAttributeValue(final XmlAttributeValue value) {
                    final PsiReference reference = value.getReference();
                    if (reference != null) {
                      final PsiElement resolve = reference.resolve();
                      if (resolve instanceof XmlAttributeValue) {
                        final PsiElement parent = resolve.getParent();
                        if (parent instanceof XmlAttribute) {
                          final XmlAttribute attribute = (XmlAttribute)parent;
                          if (FxmlConstants.FX_ID.equals(attribute.getName()) && fieldName.equals(attribute.getValue())) {
                            consumer.process(reference);
                          }
                        }
                      }
                    }
                  }
                });
              }
            });
          }
        }
      }
    }
    return true;
  }
}
