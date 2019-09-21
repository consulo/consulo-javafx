package org.jetbrains.plugins.javaFX.fxml;

import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlFileNSInfoProvider;

import javax.annotation.Nonnull;

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
