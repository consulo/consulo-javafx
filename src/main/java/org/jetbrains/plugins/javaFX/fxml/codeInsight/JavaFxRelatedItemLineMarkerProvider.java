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

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.icons.AllIcons;
import com.intellij.navigation.GotoRelatedItem;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.Function;
import com.intellij.util.Functions;
import javax.annotation.Nonnull;
import org.jetbrains.plugins.javaFX.fxml.FxmlConstants;
import org.jetbrains.plugins.javaFX.fxml.JavaFxFileTypeFactory;
import org.jetbrains.plugins.javaFX.fxml.JavaFxPsiUtil;
import org.jetbrains.plugins.javaFX.indexing.JavaFxControllerClassIndex;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JavaFxRelatedItemLineMarkerProvider extends RelatedItemLineMarkerProvider
{
	private static final Logger LOG = Logger.getInstance(JavaFxRelatedItemLineMarkerProvider.class);

	@Override
	protected void collectNavigationMarkers(@Nonnull PsiElement element, @Nonnull final Collection<? super RelatedItemLineMarkerInfo> result)
	{
		PsiElement f;
		if(element instanceof PsiIdentifier && (f = element.getParent()) instanceof PsiField)
		{
			final PsiField field = (PsiField) f;
			if(JavaFxPsiUtil.isVisibleInFxml(field) && !field.hasModifierProperty(PsiModifier.STATIC) && !field.hasModifierProperty(PsiModifier.FINAL))
			{
				final PsiClass containingClass = field.getContainingClass();
				if(containingClass != null && containingClass.hasModifierProperty(PsiModifier.PUBLIC) && containingClass.getQualifiedName() != null)
				{
					final PsiMethod[] constructors = containingClass.getConstructors();
					boolean defaultConstructor = constructors.length == 0;
					for(PsiMethod constructor : constructors)
					{
						if(constructor.getParameterList().getParametersCount() == 0)
						{
							defaultConstructor = true;
							break;
						}
					}
					if(!defaultConstructor)
					{
						return;
					}
					final ArrayList<GotoRelatedItem> targets = new ArrayList<>();
					collectTargets(field, targets, GotoRelatedItem::new, true);
					if(targets.isEmpty())
					{
						return;
					}

					result.add(new RelatedItemLineMarkerInfo<>((PsiIdentifier) element, element.getTextRange(),
							AllIcons.FileTypes.Xml, Pass.LINE_MARKERS, null,
							new JavaFXIdIconNavigationHandler(), GutterIconRenderer.Alignment.LEFT,
							targets));
				}
			}
		}
	}

	private static <T> void collectTargets(PsiField field, List<T> targets, final Function<PsiElement, T> fun, final boolean stopAtFirst)
	{
		final PsiClass containingClass = field.getContainingClass();
		LOG.assertTrue(containingClass != null);
		final String qualifiedName = containingClass.getQualifiedName();
		LOG.assertTrue(qualifiedName != null);
		final List<VirtualFile> fxmls = JavaFxControllerClassIndex.findFxmlsWithController(field.getProject(), qualifiedName);
		if(fxmls.isEmpty())
		{
			return;
		}
		ReferencesSearch.search(field, GlobalSearchScope.filesScope(field.getProject(), fxmls)).forEach(
				reference -> {
					final PsiElement referenceElement = reference.getElement();
					if(referenceElement == null)
					{
						return true;
					}
					final PsiFile containingFile = referenceElement.getContainingFile();
					if(containingFile == null)
					{
						return true;
					}
					if(!JavaFxFileTypeFactory.isFxml(containingFile))
					{
						return true;
					}
					if(!(referenceElement instanceof XmlAttributeValue))
					{
						return true;
					}
					final XmlAttributeValue attributeValue = (XmlAttributeValue) referenceElement;
					final PsiElement parent = attributeValue.getParent();
					if(!(parent instanceof XmlAttribute))
					{
						return true;
					}
					if(!FxmlConstants.FX_ID.equals(((XmlAttribute) parent).getName()))
					{
						return true;
					}
					targets.add(fun.fun(parent));
					return !stopAtFirst;
				});
	}

	private static class JavaFXIdIconNavigationHandler implements GutterIconNavigationHandler<PsiIdentifier>
	{
		@Override
		public void navigate(MouseEvent e, PsiIdentifier fieldName)
		{
			List<PsiElement> relatedItems = new ArrayList<>();
			PsiElement f = fieldName.getParent();
			if(f instanceof PsiField)
			{
				collectTargets((PsiField) f, relatedItems, Functions.id(), false);
			}
			if(relatedItems.size() == 1)
			{
				NavigationUtil.activateFileWithPsiElement(relatedItems.get(0));
				return;
			}
			final JBPopup popup = NavigationUtil
					.getPsiElementPopup(relatedItems.toArray(PsiElement.EMPTY_ARRAY), "<html>Choose component with fx:id <b>" + fieldName.getText() + "<b></html>");
			popup.show(new RelativePoint(e));
		}
	}
}
