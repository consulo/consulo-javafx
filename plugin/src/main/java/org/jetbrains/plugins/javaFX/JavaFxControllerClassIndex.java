/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.plugins.javaFX;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.ApplicationManager;
import consulo.application.dumb.IndexNotReadyException;
import consulo.application.util.function.Computable;
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
import consulo.project.content.scope.ProjectAwareSearchScope;
import consulo.project.content.scope.ProjectScopes;
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

@ExtensionImpl
public class JavaFxControllerClassIndex extends ScalarIndexExtension<String> {
  @NonNls
  public static final ID<String, Void> NAME = ID.create("JavaFxControllerClassIndex");
  private final EnumeratorStringDescriptor myKeyDescriptor = new EnumeratorStringDescriptor();
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

  @Override
  public KeyDescriptor<String> getKeyDescriptor() {
    return myKeyDescriptor;
  }

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
    public Map<String, Void> map(final FileContent inputData) {
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

      class StopException extends RuntimeException {
      }

      try {
        NanoXmlUtil.parse(new StringReader(content), new NanoXmlUtil.IXMLBuilderAdapter() {
          private boolean myFxRootUsed = false;

          @Override
          public void addAttribute(String key, String nsPrefix, String nsURI, String value, String type) throws Exception {
            if (value != null &&
              (FxmlConstants.FX_CONTROLLER.equals(nsPrefix + ":" + key) || FxmlConstants.TYPE.equals(key) && myFxRootUsed)) {
              className[0] = value;
            }
          }

          @Override
          public void elementAttributesProcessed(String name, String nsPrefix, String nsURI) throws Exception {
            throw new StopException();
          }

          @Override
          public void startElement(String name, String nsPrefix, String nsURI, String systemID, int lineNr)
            throws Exception {
            myFxRootUsed = FxmlConstants.FX_ROOT.equals(nsPrefix + ":" + name);
          }
        });
      }
      catch (StopException ignore) {
      }
      return className[0];
    }
  }

  public static class MyInputFilter extends DefaultFileTypeSpecificInputFilter {
    public MyInputFilter() {
      super(XmlFileType.INSTANCE);
    }

    @Override
    public boolean acceptInput(final Project project, final VirtualFile file) {
      return JavaFxFileTypeFactory.isFxml(file);
    }
  }

  public static List<PsiFile> findFxmlWithController(final Project project, @Nonnull String className) {
    return findFxmlWithController(project,
                                  className,
                                  file -> PsiManager.getInstance(project).findFile(file),
                                  ProjectScopes
                                    .getAllScope(project));
  }

  public static List<VirtualFile> findFxmlsWithController(final Project project, @Nonnull String className) {
    return findFxmlWithController(project, className, Function.identity(), ProjectScopes.getAllScope(project));
  }

  public static <T> List<T> findFxmlWithController(final Project project,
                                                   @Nonnull final String className,
                                                   final Function<VirtualFile, T> f,
                                                   final ProjectAwareSearchScope scope) {
    return ApplicationManager.getApplication().runReadAction(new Computable<List<T>>() {
      @Override
      public List<T> compute() {
        final Collection<VirtualFile> files;
        try {
          files = FileBasedIndex.getInstance().getContainingFiles(NAME, className,
                                                                  GlobalSearchScope.projectScope(project).intersectWith(scope));
        }
        catch (IndexNotReadyException e) {
          return Collections.emptyList();
        }
        if (files.isEmpty()) return Collections.emptyList();
        List<T> result = new ArrayList<T>();
        for (VirtualFile file : files) {
          if (!file.isValid()) continue;
          final T fFile = f.apply(file);
          if (fFile != null) {
            result.add(fFile);
          }
        }
        return result;
      }
    });
  }
}
