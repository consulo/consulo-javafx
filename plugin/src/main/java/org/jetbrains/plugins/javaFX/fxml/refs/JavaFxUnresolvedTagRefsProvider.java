package org.jetbrains.plugins.javaFX.fxml.refs;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.intention.QuickFixActionRegistrar;
import consulo.language.editor.intention.UnresolvedReferenceQuickFixProvider;
import consulo.xml.psi.xml.XmlTag;

import jakarta.annotation.Nonnull;

@ExtensionImpl
public class JavaFxUnresolvedTagRefsProvider extends UnresolvedReferenceQuickFixProvider<JavaFxTagNameReference> {
  @Override
  public void registerFixes(@Nonnull JavaFxTagNameReference ref, @Nonnull QuickFixActionRegistrar registrar) {
    XmlTag element = ref.getTagElement();
    if (element != null) {
      registrar.register(new JavaFxImportClassFix(ref, element) {
        @Override
        protected XmlTag getTagElement(JavaFxTagNameReference ref) {
          return ref.getTagElement();
        }
      });
    }
  }

  @Nonnull
  @Override
  public Class<JavaFxTagNameReference> getReferenceClass() {
    return JavaFxTagNameReference.class;
  }
}
