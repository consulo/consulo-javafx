package org.jetbrains.plugins.javaFX.fxml;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.xml.XmlSchemaProvider;
import org.jetbrains.annotations.NonNls;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.net.URL;

/**
 * User: anna
 * Date: 1/10/13
 */
public class JavaFXSchemaHandler extends XmlSchemaProvider {
  private static final Logger LOG = Logger.getInstance("#" + JavaFXSchemaHandler.class.getName());

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
    final VirtualFile fileByURL = VfsUtil.findFileByURL(resource);

    PsiFile psiFile = PsiManager.getInstance(module.getProject()).findFile(fileByURL);
    LOG.assertTrue(psiFile != null);
    return (XmlFile)psiFile.copy();
  }

}
