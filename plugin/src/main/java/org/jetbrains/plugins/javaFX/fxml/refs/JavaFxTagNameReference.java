package org.jetbrains.plugins.javaFX.fxml.refs;

import com.intellij.java.language.psi.PsiClass;
import consulo.document.util.TextRange;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.util.IncorrectOperationException;
import consulo.util.lang.StringUtil;
import consulo.xml.psi.impl.source.xml.TagNameReference;
import consulo.xml.psi.xml.XmlTag;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * User: anna
 * Date: 1/8/13
 */
public class JavaFxTagNameReference extends TagNameReference {
  public JavaFxTagNameReference(ASTNode element, boolean startTagFlag) {
    super(element, startTagFlag);
  }

  @Nullable
  @Override
  public XmlTag getTagElement() {
    return super.getTagElement();
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
}
