package consulo.javaFX.fxml;

import consulo.language.file.FileTypeManager;
import consulo.language.psi.PsiFile;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.fileType.FileType;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 23/01/2023
 */
public final class FXMLFileType {
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
}
