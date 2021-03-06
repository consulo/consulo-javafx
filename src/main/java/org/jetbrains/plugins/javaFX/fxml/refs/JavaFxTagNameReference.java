package org.jetbrains.plugins.javaFX.fxml.refs;

import javax.annotation.Nonnull;

import com.intellij.codeInsight.daemon.QuickFixActionRegistrar;
import com.intellij.codeInsight.quickfix.UnresolvedReferenceQuickFixProvider;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.xml.TagNameReference;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.IncorrectOperationException;

/**
 * User: anna
 * Date: 1/8/13
 */
public class JavaFxTagNameReference extends TagNameReference{
  private static final Logger LOGGER = Logger.getInstance("#" + JavaFxTagNameReference.class.getName());

  public JavaFxTagNameReference(ASTNode element, boolean startTagFlag) {
    super(element, startTagFlag);
  }

  @Override
  public TextRange getRangeInElement() {
    final TextRange rangeInElement = super.getRangeInElement();
    final XmlTag tagElement = getTagElement();
    if (tagElement != null) {
      final String tagElementName = tagElement.getName();
      final int dotIdx = tagElementName.indexOf(".");
      final int startOffset = rangeInElement.getStartOffset();
      if (dotIdx > -1 && startOffset + dotIdx + 2 < rangeInElement.getEndOffset()) {
        return new TextRange(startOffset + dotIdx + 1, rangeInElement.getEndOffset());
      }
    }
    return rangeInElement;
  }

  @Override
  public PsiElement bindToElement(@Nonnull PsiElement element) throws IncorrectOperationException {
    if (element instanceof PsiClass) {
      final String qualifiedName = ((PsiClass)element).getQualifiedName();
      if (qualifiedName != null) {
        final String shortName = StringUtil.getShortName(qualifiedName);
        final XmlTag tagElement = getTagElement();
        if (tagElement != null) {
          final String oldTagName = tagElement.getName();
          if (oldTagName.contains(".")) {
            return tagElement.setName(qualifiedName);
          }
          return tagElement.setName(shortName);
        }
        return getElement();
      }
    }
    return super.bindToElement(element);
  }

  public static class JavaFxUnresolvedTagRefsProvider extends UnresolvedReferenceQuickFixProvider<JavaFxTagNameReference> {
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
}
