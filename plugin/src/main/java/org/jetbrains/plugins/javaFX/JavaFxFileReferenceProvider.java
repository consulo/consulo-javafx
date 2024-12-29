package org.jetbrains.plugins.javaFX;

import com.intellij.java.language.psi.PsiLiteralExpression;
import consulo.language.psi.*;
import consulo.language.psi.path.FileReferenceSet;
import consulo.language.util.ProcessingContext;
import consulo.util.lang.function.Condition;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.VirtualFileSystem;

import jakarta.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;

/**
 * User: anna
 * Date: 4/3/13
 */
public class JavaFxFileReferenceProvider extends PsiReferenceProvider {

  private final String myAcceptedExtension;

  public JavaFxFileReferenceProvider(String acceptedExtension) {
    myAcceptedExtension = acceptedExtension;
  }

  @Nonnull
  @Override
  public PsiReference[] getReferencesByElement(@Nonnull final PsiElement element, @Nonnull ProcessingContext context) {
    final Object value = ((PsiLiteralExpression)element).getValue();
    if (!(value instanceof String)) return PsiReference.EMPTY_ARRAY;
    return getReferences(element, preprocessValue((String)value), myAcceptedExtension);
  }

  protected String preprocessValue(String value) {
    return value;
  }

  public static PsiReference[] getReferences(final PsiElement element, String value, final String acceptedExtension) {
    final PsiDirectory directory = element.getContainingFile().getOriginalFile().getParent();
    if (directory == null) return PsiReference.EMPTY_ARRAY;
    final boolean startsWithSlash = value.startsWith("/");
    final VirtualFileSystem fs = directory.getVirtualFile().getFileSystem();
    final FileReferenceSet fileReferenceSet = new FileReferenceSet(value, element, 1, null, fs.isCaseSensitive()) {
      @Nonnull
      @Override
      public Collection<PsiFileSystemItem> getDefaultContexts() {
        if (startsWithSlash || !directory.isValid()) {
          return super.getDefaultContexts();
        }
        return Collections.<PsiFileSystemItem>singletonList(directory);
      }

      @Override
      public Condition<PsiFileSystemItem> getReferenceCompletionFilter() {
        return item -> {
          if (item instanceof PsiDirectory) return true;
          final VirtualFile virtualFile = PsiUtilCore.getVirtualFile(item);
          return virtualFile != null && acceptedExtension.equals(virtualFile.getExtension());
        };
      }
    };
    if (startsWithSlash) {
      fileReferenceSet.addCustomization(FileReferenceSet.DEFAULT_PATH_EVALUATOR_OPTION, FileReferenceSet.ABSOLUTE_TOP_LEVEL);
    }
    return fileReferenceSet.getAllReferences();
  }
}
