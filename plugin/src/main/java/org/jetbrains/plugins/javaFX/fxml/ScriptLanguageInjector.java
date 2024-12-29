package org.jetbrains.plugins.javaFX.fxml;

import consulo.annotation.component.ExtensionImpl;
import consulo.document.util.TextRange;
import consulo.language.Language;
import consulo.language.inject.MultiHostInjector;
import consulo.language.inject.MultiHostRegistrar;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiLanguageInjectionHost;
import consulo.util.lang.StringUtil;
import consulo.xml.patterns.XmlElementPattern;
import consulo.xml.patterns.XmlPatterns;
import consulo.xml.psi.xml.XmlFile;
import consulo.xml.psi.xml.XmlText;

import jakarta.annotation.Nonnull;
import java.util.List;

@ExtensionImpl
public class ScriptLanguageInjector implements MultiHostInjector {

  private static final XmlElementPattern.XmlTextPattern SCRIPT_PATTERN = XmlPatterns.xmlText().withParent(
    XmlPatterns.xmlTag().withName(FxmlConstants.FX_SCRIPT));

  @Nonnull
  @Override
  public Class<? extends PsiElement> getElementClass() {
    return XmlText.class;
  }

  @Override
  public void injectLanguages(@Nonnull MultiHostRegistrar multiHostRegistrar, @Nonnull PsiElement element) {
    if (SCRIPT_PATTERN.accepts(element)) {
      final List<String> registeredLanguages = JavaFxPsiUtil.parseInjectedLanguages((XmlFile)element.getContainingFile());
      for (Language language : Language.getRegisteredLanguages()) {
        for (String registeredLanguage : registeredLanguages) {
          if (StringUtil.equalsIgnoreCase(language.getID(), registeredLanguage)) {
            multiHostRegistrar.startInjecting(language)
                              .addPlace(null, null, (PsiLanguageInjectionHost)element,
                                        TextRange.from(0, element.getTextLength() - 1))
                              .doneInjecting();
            break;
          }
        }
      }
    }
  }
}