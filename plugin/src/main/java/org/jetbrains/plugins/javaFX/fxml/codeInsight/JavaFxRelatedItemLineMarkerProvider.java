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
package org.jetbrains.plugins.javaFX.fxml.codeInsight;

import com.intellij.java.language.JavaLanguage;
import com.intellij.java.language.psi.*;
import consulo.application.AllIcons;
import consulo.codeEditor.markup.GutterIconRenderer;
import consulo.language.Language;
import consulo.language.editor.Pass;
import consulo.language.editor.gutter.GutterIconNavigationHandler;
import consulo.language.editor.gutter.RelatedItemLineMarkerInfo;
import consulo.language.editor.gutter.RelatedItemLineMarkerProvider;
import consulo.language.editor.ui.PopupNavigationUtil;
import consulo.language.editor.util.LanguageEditorNavigationUtil;
import consulo.language.navigation.GotoRelatedItem;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.search.ReferencesSearch;
import consulo.logging.Logger;
import consulo.ui.ex.RelativePoint;
import consulo.ui.ex.popup.JBPopup;
import consulo.util.lang.function.Functions;
import consulo.virtualFileSystem.VirtualFile;
import consulo.xml.psi.xml.XmlAttribute;
import consulo.xml.psi.xml.XmlAttributeValue;
import org.jetbrains.plugins.javaFX.fxml.FxmlConstants;
import org.jetbrains.plugins.javaFX.fxml.JavaFxFileTypeFactory;
import org.jetbrains.plugins.javaFX.fxml.JavaFxPsiUtil;
import org.jetbrains.plugins.javaFX.indexing.JavaFxControllerClassIndex;

import javax.annotation.Nonnull;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class JavaFxRelatedItemLineMarkerProvider extends RelatedItemLineMarkerProvider {
  private static final Logger LOG = Logger.getInstance(JavaFxRelatedItemLineMarkerProvider.class);

  @Override
  protected void collectNavigationMarkers(@Nonnull PsiElement element,
                                          @Nonnull final Collection<? super RelatedItemLineMarkerInfo> result) {
    PsiElement f;
    if (element instanceof PsiIdentifier && (f = element.getParent()) instanceof PsiField) {
      final PsiField field = (PsiField)f;
      if (JavaFxPsiUtil.isVisibleInFxml(field) && !field.hasModifierProperty(PsiModifier.STATIC) && !field.hasModifierProperty(PsiModifier.FINAL)) {
        final PsiClass containingClass = field.getContainingClass();
        if (containingClass != null && containingClass.hasModifierProperty(PsiModifier.PUBLIC) && containingClass.getQualifiedName() != null) {
          final PsiMethod[] constructors = containingClass.getConstructors();
          boolean defaultConstructor = constructors.length == 0;
          for (PsiMethod constructor : constructors) {
            if (constructor.getParameterList().getParametersCount() == 0) {
              defaultConstructor = true;
              break;
            }
          }
          if (!defaultConstructor) {
            return;
          }
          final ArrayList<GotoRelatedItem> targets = new ArrayList<>();
          collectTargets(field, targets, GotoRelatedItem::new, true);
          if (targets.isEmpty()) {
            return;
          }

          result.add(new RelatedItemLineMarkerInfo<>((PsiIdentifier)element, element.getTextRange(),
                                                     AllIcons.FileTypes.Xml, Pass.LINE_MARKERS, null,
                                                     new JavaFXIdIconNavigationHandler(), GutterIconRenderer.Alignment.LEFT,
                                                     targets));
        }
      }
    }
  }

  private static <T> void collectTargets(PsiField field, List<T> targets, final Function<PsiElement, T> fun, final boolean stopAtFirst) {
    final PsiClass containingClass = field.getContainingClass();
    LOG.assertTrue(containingClass != null);
    final String qualifiedName = containingClass.getQualifiedName();
    LOG.assertTrue(qualifiedName != null);
    final List<VirtualFile> fxmls = JavaFxControllerClassIndex.findFxmlsWithController(field.getProject(), qualifiedName);
    if (fxmls.isEmpty()) {
      return;
    }
    ReferencesSearch.search(field, GlobalSearchScope.filesScope(field.getProject(), fxmls)).forEach(
      reference -> {
        final PsiElement referenceElement = reference.getElement();
        if (referenceElement == null) {
          return true;
        }
        final PsiFile containingFile = referenceElement.getContainingFile();
        if (containingFile == null) {
          return true;
        }
        if (!JavaFxFileTypeFactory.isFxml(containingFile)) {
          return true;
        }
        if (!(referenceElement instanceof XmlAttributeValue)) {
          return true;
        }
        final XmlAttributeValue attributeValue = (XmlAttributeValue)referenceElement;
        final PsiElement parent = attributeValue.getParent();
        if (!(parent instanceof XmlAttribute)) {
          return true;
        }
        if (!FxmlConstants.FX_ID.equals(((XmlAttribute)parent).getName())) {
          return true;
        }
        targets.add(fun.apply(parent));
        return !stopAtFirst;
      });
  }

  @Nonnull
  @Override
  public Language getLanguage() {
    return JavaLanguage.INSTANCE;
  }

  private static class JavaFXIdIconNavigationHandler implements GutterIconNavigationHandler<PsiIdentifier> {
    @Override
    public void navigate(MouseEvent e, PsiIdentifier fieldName) {
      List<PsiElement> relatedItems = new ArrayList<>();
      PsiElement f = fieldName.getParent();
      if (f instanceof PsiField) {
        collectTargets((PsiField)f, relatedItems, Functions.id(), false);
      }
      if (relatedItems.size() == 1) {
        LanguageEditorNavigationUtil.activateFileWithPsiElement(relatedItems.get(0));
        return;
      }
      final JBPopup popup = PopupNavigationUtil
        .getPsiElementPopup(relatedItems.toArray(PsiElement.EMPTY_ARRAY),
                            "<html>Choose component with fx:id <b>" + fieldName.getText() + "<b></html>");
      popup.show(new RelativePoint(e));
    }
  }
}
