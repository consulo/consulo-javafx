package consulo.javaFX.impl;

import consulo.annotation.component.ExtensionImpl;
import org.osmorc.manifest.lang.headerparser.ManifestHeaderParserContributor;
import org.osmorc.manifest.lang.headerparser.ManifestHeaderParserRegistrator;
import org.osmorc.manifest.lang.headerparser.impl.GenericComplexHeaderParser;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 23/01/2023
 */
@ExtensionImpl
public class JavaFXManifestHeaderParserContributor implements ManifestHeaderParserContributor {
  @Override
  public void contribute(@Nonnull ManifestHeaderParserRegistrator registrator) {
    registrator.register("JavaFX-Application-Class", new GenericComplexHeaderParser());
    registrator.register("JavaFX-Version", new GenericComplexHeaderParser());
    registrator.register("JavaFX-Class-Path", new GenericComplexHeaderParser());
    registrator.register("JavaFX-Preloader-Class", new GenericComplexHeaderParser());
    registrator.register("JavaFX-Fallback-Class", new GenericComplexHeaderParser());
  }
}
