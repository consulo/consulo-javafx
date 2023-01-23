package org.jetbrains.plugins.javaFX.fxml.refs;

import com.intellij.java.impl.codeInsight.daemon.impl.quickfix.CreateMethodQuickFix;
import com.intellij.java.language.psi.*;
import com.intellij.java.language.psi.util.TypeConversionUtil;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.intention.QuickFixActionRegistrar;
import consulo.language.editor.intention.UnresolvedReferenceQuickFixProvider;
import consulo.language.psi.PsiElement;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.project.Project;
import consulo.xml.psi.xml.XmlAttribute;
import consulo.xml.psi.xml.XmlAttributeValue;
import org.jetbrains.plugins.javaFX.fxml.JavaFxCommonClassNames;
import org.jetbrains.plugins.javaFX.fxml.JavaFxPsiUtil;

import javax.annotation.Nonnull;

@ExtensionImpl
public class JavaFxUnresolvedReferenceHandlerQuickfixProvider extends UnresolvedReferenceQuickFixProvider<JavaFxEventHandlerReference> {

  @Override
  public void registerFixes(@Nonnull final JavaFxEventHandlerReference ref, @Nonnull final QuickFixActionRegistrar registrar) {
    if (ref.myController != null && ref.myEventHandler == null) {
      final CreateMethodQuickFix quickFix = CreateMethodQuickFix.createFix(ref.myController, getHandlerSignature(ref), "");
      if (quickFix != null) {
        registrar.register(quickFix);
      }
    }
  }

  private static String getHandlerSignature(JavaFxEventHandlerReference ref) {
    final XmlAttributeValue element = ref.getElement();
    String canonicalText = JavaFxCommonClassNames.JAVAFX_EVENT;
    final PsiElement parent = element.getParent();
    if (parent instanceof XmlAttribute) {
      final XmlAttribute xmlAttribute = (XmlAttribute)parent;
      final Project project = element.getProject();
      final PsiField handlerField = ref.myCurrentTagClass.findFieldByName(xmlAttribute.getName(), true);
      if (handlerField != null) {
        final PsiClassType classType = JavaFxPsiUtil.getPropertyClassType(handlerField);
        if (classType != null) {
          final PsiClass eventHandlerClass = JavaPsiFacade.getInstance(project)
                                                          .findClass(JavaFxCommonClassNames.JAVAFX_EVENT_EVENT_HANDLER,
                                                                     GlobalSearchScope.allScope(project));
          final PsiTypeParameter[] typeParameters = eventHandlerClass != null ? eventHandlerClass.getTypeParameters() : null;
          if (typeParameters != null && typeParameters.length == 1) {
            final PsiTypeParameter typeParameter = typeParameters[0];
            final PsiSubstitutor substitutor = TypeConversionUtil.getSuperClassSubstitutor(eventHandlerClass, classType);
            final PsiType eventType = substitutor.substitute(typeParameter);
            if (eventType != null) {
              canonicalText = eventType.getCanonicalText();
            }
          }
        }
      }
    }
    return "public void " + element.getValue().substring(1) + "(" + canonicalText + " e)";
  }

  @Nonnull
  @Override
  public Class<JavaFxEventHandlerReference> getReferenceClass() {
    return JavaFxEventHandlerReference.class;
  }
}
