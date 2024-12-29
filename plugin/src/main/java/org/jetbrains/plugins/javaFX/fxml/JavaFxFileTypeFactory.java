package org.jetbrains.plugins.javaFX.fxml;

import consulo.annotation.component.ExtensionImpl;
import consulo.javaFX.fxml.FXMLFileType;
import consulo.language.psi.PsiFile;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.fileType.FileTypeConsumer;
import consulo.virtualFileSystem.fileType.FileTypeFactory;
import consulo.xml.ide.highlighter.XmlFileType;

import jakarta.annotation.Nonnull;

/**
 * User: anna
 * Date: 1/8/13
 */
@ExtensionImpl
public class JavaFxFileTypeFactory extends FileTypeFactory {
  @Deprecated
  public static final String FXML_EXTENSION = FXMLFileType.FXML_EXTENSION;

  @Deprecated
  public static boolean isFxml(@Nonnull PsiFile file) {
    return FXMLFileType.isFxml(file);
  }

  @Deprecated
  public static boolean isFxml(@Nonnull VirtualFile virtualFile) {
    return FXMLFileType.isFxml(virtualFile);
  }

  @Override
  public void createFileTypes(@Nonnull FileTypeConsumer consumer) {
    consumer.consume(XmlFileType.INSTANCE, FXML_EXTENSION);
  }
}
