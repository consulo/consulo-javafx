package org.jetbrains.plugins.javaFX.fxml.refs;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.annotation.Annotator;
import consulo.language.editor.annotation.AnnotatorFactory;
import consulo.xml.lang.xml.XMLLanguage;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 23/01/2023
 */
@ExtensionImpl
public class JavaFxAnnotatorFactory implements AnnotatorFactory {
  @Nullable
  @Override
  public Annotator createAnnotator() {
    return new JavaFxAnnotator();
  }

  @Nonnull
  @Override
  public Language getLanguage() {
    return XMLLanguage.INSTANCE;
  }
}
