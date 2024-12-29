package org.jetbrains.plugins.javaFX.fxml;

import com.intellij.xml.DefaultXmlExtension;
import com.intellij.xml.util.XmlUtil;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiFile;
import consulo.xml.psi.impl.source.xml.TagNameReference;
import consulo.xml.psi.xml.XmlDocument;
import org.jetbrains.plugins.javaFX.fxml.refs.JavaFxTagNameReference;

import jakarta.annotation.Nullable;

@ExtensionImpl
public class JavaFxXmlExtension extends DefaultXmlExtension {
  @Override
  public boolean isAvailable(final PsiFile file) {
    return JavaFxFileTypeFactory.isFxml(file);
  }

  @Override
  public TagNameReference createTagNameReference(final ASTNode nameElement, final boolean startTagFlag) {
    return new JavaFxTagNameReference(nameElement, startTagFlag);
  }

  @Nullable
  @Override
  public String[][] getNamespacesFromDocument(XmlDocument parent, boolean declarationsExist) {
    return XmlUtil.getDefaultNamespaces(parent);
  }
}
