package org.jetbrains.plugins.javaFX.fxml;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.meta.MetaDataContributor;
import consulo.language.psi.meta.MetaDataRegistrar;
import consulo.xml.psi.filters.position.NamespaceFilter;
import consulo.xml.psi.filters.position.RootTagFilter;

/**
 * User: anna
 * Date: 1/9/13
 */
@ExtensionImpl
public class JavaFXMetaDataContributor implements MetaDataContributor {
  @Override
  public void contributeMetaData(MetaDataRegistrar registrar) {

    registrar.registerMetaData(new RootTagFilter(new NamespaceFilter(JavaFxNamespaceDataProvider.JAVAFX_NAMESPACE)),
                               JavaFXNSDescriptor::new);
  }
}
