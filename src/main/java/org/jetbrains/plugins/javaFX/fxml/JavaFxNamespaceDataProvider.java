package org.jetbrains.plugins.javaFX.fxml;

import consulo.annotation.component.ExtensionImpl;
import consulo.xml.psi.xml.XmlFile;
import consulo.xml.psi.xml.XmlFileNSInfoProvider;

import javax.annotation.Nonnull;

@ExtensionImpl
public class JavaFxNamespaceDataProvider implements XmlFileNSInfoProvider {
  public static final String JAVAFX_NAMESPACE = "http://javafx.com/fxml";

  private static final String[][] NAMESPACES = {{"", JAVAFX_NAMESPACE}};

  public String[][] getDefaultNamespaces(@Nonnull XmlFile file) {
    return JavaFxFileTypeFactory.isFxml(file) ? NAMESPACES : null;
  }

  @Override
  public boolean overrideNamespaceFromDocType(@Nonnull XmlFile file) {
    return false;
  }
}
