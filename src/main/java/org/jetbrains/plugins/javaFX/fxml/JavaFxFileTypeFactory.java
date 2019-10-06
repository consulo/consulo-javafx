package org.jetbrains.plugins.javaFX.fxml;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;

import javax.annotation.Nonnull;

/**
 * User: anna
 * Date: 1/8/13
 */
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
