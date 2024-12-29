package org.jetbrains.plugins.javaFX.fxml;

import com.intellij.xml.XmlSchemaProvider;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.logging.Logger;
import consulo.module.Module;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import consulo.xml.psi.xml.XmlFile;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.net.URL;

/**
 * User: anna
 * Date: 1/10/13
 */
@ExtensionImpl
public class JavaFXSchemaHandler extends XmlSchemaProvider {
  private static final Logger LOG = Logger.getInstance(JavaFXSchemaHandler.class);

  @Override
  public boolean isAvailable(final @Nonnull XmlFile file) {
    return JavaFxFileTypeFactory.isFxml(file);
  }

  @Nullable
  @Override
  public XmlFile getSchema(@Nonnull @NonNls String url, @Nullable Module module, @Nonnull PsiFile baseFile) {
    return module != null && JavaFxFileTypeFactory.isFxml(baseFile) ? getReference(module) : null;
  }

  private static XmlFile getReference(@Nonnull Module module) {
    final URL resource = JavaFXSchemaHandler.class.getResource("fx.xsd");
    final VirtualFile fileByURL = VirtualFileUtil.findFileByURL(resource);

    PsiFile psiFile = PsiManager.getInstance(module.getProject()).findFile(fileByURL);
    LOG.assertTrue(psiFile != null);
    return (XmlFile)psiFile.copy();
  }

}
