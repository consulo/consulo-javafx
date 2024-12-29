// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.javaFX.indexing;

import consulo.application.ReadAction;
import consulo.application.dumb.IndexNotReadyException;
import consulo.index.io.DataIndexer;
import consulo.index.io.EnumeratorStringDescriptor;
import consulo.index.io.ID;
import consulo.index.io.KeyDescriptor;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.stub.DefaultFileTypeSpecificInputFilter;
import consulo.language.psi.stub.FileBasedIndex;
import consulo.language.psi.stub.FileContent;
import consulo.language.psi.stub.ScalarIndexExtension;
import consulo.project.Project;
import consulo.project.content.scope.ProjectScopes;
import consulo.util.lang.function.Functions;
import consulo.util.xml.fastReader.NanoXmlBuilder;
import consulo.util.xml.fastReader.NanoXmlUtil;
import consulo.virtualFileSystem.VirtualFile;
import consulo.xml.ide.highlighter.XmlFileType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.plugins.javaFX.fxml.FxmlConstants;
import org.jetbrains.plugins.javaFX.fxml.JavaFxFileTypeFactory;
import org.jetbrains.plugins.javaFX.fxml.JavaFxNamespaceDataProvider;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.io.StringReader;
import java.util.*;
import java.util.function.Function;

public class JavaFxControllerClassIndex extends ScalarIndexExtension<String> {
  @NonNls
  public static final ID<String, Void> NAME = ID.create("JavaFxControllerClassIndex");
  private final MyInputFilter myInputFilter = new MyInputFilter();
  private final MyDataIndexer myDataIndexer = new MyDataIndexer();

  @Override
  @Nonnull
  public ID<String, Void> getName() {
    return NAME;
  }

  @Override
  @Nonnull
  public DataIndexer<String, Void, FileContent> getIndexer() {
    return myDataIndexer;
  }

  @Nonnull
  @Override
  public KeyDescriptor<String> getKeyDescriptor() {
    return EnumeratorStringDescriptor.INSTANCE;
  }

  @Nonnull
  @Override
  public FileBasedIndex.InputFilter getInputFilter() {
    return myInputFilter;
  }

  @Override
  public boolean dependsOnFileContent() {
    return true;
  }

  @Override
  public int getVersion() {
    return 1;
  }

  private static class MyDataIndexer implements DataIndexer<String, Void, FileContent> {
    @Override
    @Nonnull
    public Map<String, Void> map(@Nonnull final FileContent inputData) {
      final String className = getControllerClassName(inputData.getContentAsText().toString());
      if (className != null) {
        return Collections.singletonMap(className, null);
      }
      return Collections.emptyMap();
    }

    @Nullable
    private static String getControllerClassName(String content) {
      if (!content.contains(JavaFxNamespaceDataProvider.JAVAFX_NAMESPACE)) {
        return null;
      }

      final String[] className = new String[]{null};
      NanoXmlUtil.parse(new StringReader(content), new NanoXmlBuilder() {
        private boolean myFxRootUsed = false;

        @Override
        public void addAttribute(String key, String nsPrefix, String nsURI, String value, String type) {
          if (value != null &&
            (FxmlConstants.FX_CONTROLLER.equals(nsPrefix + ":" + key) || FxmlConstants.TYPE.equals(key) && myFxRootUsed)) {
            className[0] = value;
          }
        }

        @Override
        public void elementAttributesProcessed(String name, String nsPrefix, String nsURI) throws Exception {
          throw NanoXmlUtil.ParserStoppedXmlException.INSTANCE;
        }

        @Override
        public void startElement(String name, String nsPrefix, String nsURI, String systemID, int lineNr) {
          myFxRootUsed = FxmlConstants.FX_ROOT.equals(nsPrefix + ":" + name);
        }
      });
      return className[0];
    }
  }

  public static class MyInputFilter extends DefaultFileTypeSpecificInputFilter {
    public MyInputFilter() {
      super(XmlFileType.INSTANCE);
    }

    @Override
    public boolean acceptInput(@javax.annotation.Nullable Project project, @Nonnull final VirtualFile file) {
      return JavaFxFileTypeFactory.isFxml(file);
    }
  }

  public static List<PsiFile> findFxmlWithController(final Project project, @Nonnull String className) {
    return findFxmlWithController(project, className, (GlobalSearchScope)ProjectScopes.getAllScope(project));
  }

  public static List<PsiFile> findFxmlWithController(final Project project, @Nonnull String className, @Nonnull GlobalSearchScope scope) {
    final PsiManager psiManager = PsiManager.getInstance(project);
    return findFxmlWithController(project, className, psiManager::findFile, scope);
  }

  public static List<VirtualFile> findFxmlsWithController(final Project project, @Nonnull String className) {
    return findFxmlsWithController(project, className, (GlobalSearchScope)ProjectScopes.getAllScope(project));
  }

  public static List<VirtualFile> findFxmlsWithController(final Project project,
                                                          @Nonnull String className,
                                                          @Nonnull GlobalSearchScope scope) {
    return findFxmlWithController(project, className, Functions.id(), scope);
  }

  private static <T> List<T> findFxmlWithController(final Project project,
                                                    @Nonnull final String className,
                                                    final Function<VirtualFile, T> f,
                                                    final GlobalSearchScope scope) {
    return findFxmls(NAME, project, className, f, scope);
  }

  static <T> List<T> findFxmls(ID<String, ?> id, Project project,
                               @Nonnull String className,
                               Function<VirtualFile, T> f,
                               GlobalSearchScope scope) {
    return ReadAction.compute(() -> {
      final Collection<VirtualFile> files;
      try {
        files = FileBasedIndex.getInstance().getContainingFiles(id, className,
                                                                GlobalSearchScope.projectScope(project).intersectWith(scope));
      }
      catch (IndexNotReadyException e) {
        return Collections.emptyList();
      }
      if (files.isEmpty()) {
        return Collections.emptyList();
      }
      List<T> result = new ArrayList<>();
      for (VirtualFile file : files) {
        if (!file.isValid()) {
          continue;
        }
        final T fFile = f.apply(file);
        if (fFile != null) {
          result.add(fFile);
        }
      }
      return result;
    });
  }
}
