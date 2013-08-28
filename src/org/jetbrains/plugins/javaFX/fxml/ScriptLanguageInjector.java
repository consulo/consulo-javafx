package org.jetbrains.plugins.javaFX.fxml;

import com.intellij.lang.Language;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.patterns.XmlElementPattern;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.xml.XmlFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ScriptLanguageInjector implements MultiHostInjector {

	private static final XmlElementPattern.XmlTextPattern SCRIPT_PATTERN = XmlPatterns.xmlText().withParent(
			XmlPatterns.xmlTag().withName(FxmlConstants.FX_SCRIPT));

	@Override
	public void injectLanguages(@NotNull MultiHostRegistrar multiHostRegistrar, @NotNull PsiElement element) {
		if (SCRIPT_PATTERN.accepts(element)) {
			final List<String> registeredLanguages = JavaFxPsiUtil.parseInjectedLanguages((XmlFile) element.getContainingFile());
			for (Language language : Language.getRegisteredLanguages()) {
				for (String registeredLanguage : registeredLanguages) {
					if (StringUtil.equalsIgnoreCase(language.getID(), registeredLanguage)) {
						multiHostRegistrar.startInjecting(language)
								.addPlace(null, null, (PsiLanguageInjectionHost) element,
										TextRange.from(0, element.getTextLength() - 1))
								.doneInjecting();
						break;
					}
				}
			}
		}
	}
}