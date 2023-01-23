package org.jetbrains.plugins.javaFX.fxml;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.file.FileTypeManager;
import consulo.language.psi.PsiFile;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.fileType.FileType;
import consulo.virtualFileSystem.fileType.FileTypeConsumer;
import consulo.virtualFileSystem.fileType.FileTypeFactory;
import consulo.xml.ide.highlighter.XmlFileType;

import javax.annotation.Nonnull;

/**
 * User: anna
 * Date: 1/8/13
 */
@ExtensionImpl
public class JavaFxFileTypeFactory extends FileTypeFactory {
  public static final String FXML_EXTENSION = "fxml";

  public static boolean isFxml(@Nonnull PsiFile file) {
    final VirtualFile virtualFile = file.getViewProvider().getVirtualFile();
    return isFxml(virtualFile);
  }

  public static boolean isFxml(@Nonnull VirtualFile virtualFile) {
    if (FXML_EXTENSION.equals(virtualFile.getExtension())) {
      final FileType fileType = virtualFile.getFileType();
      if (fileType == FileTypeManager.getInstance().getFileTypeByExtension(FXML_EXTENSION) && !fileType.isBinary()) return true;
    }
    return false;
  }

  @Override
  public void createFileTypes(@Nonnull FileTypeConsumer consumer) {
    consumer.consume(XmlFileType.INSTANCE, FXML_EXTENSION);
  }
}
