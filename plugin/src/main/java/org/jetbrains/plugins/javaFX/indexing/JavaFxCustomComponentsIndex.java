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
package org.jetbrains.plugins.javaFX.indexing;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.ApplicationManager;
import consulo.application.dumb.IndexNotReadyException;
import consulo.application.util.function.Computable;
import consulo.index.io.DataIndexer;
import consulo.index.io.EnumeratorStringDescriptor;
import consulo.index.io.ID;
import consulo.index.io.KeyDescriptor;
import consulo.index.io.data.DataExternalizer;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.stub.FileBasedIndex;
import consulo.language.psi.stub.FileBasedIndexExtension;
import consulo.language.psi.stub.FileContent;
import consulo.project.Project;
import consulo.util.xml.fastReader.IXMLBuilder;
import consulo.util.xml.fastReader.NanoXmlBuilder;
import consulo.virtualFileSystem.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.plugins.javaFX.fxml.FxmlConstants;

import jakarta.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;

@ExtensionImpl
public class JavaFxCustomComponentsIndex extends FileBasedIndexExtension<String, Set<String>> {

  @NonNls
  public static final ID<String, Set<String>> KEY = ID.create("javafx.custom.component");

  private final KeyDescriptor<String> myKeyDescriptor = new EnumeratorStringDescriptor();
  private final FileBasedIndex.InputFilter myInputFilter = new JavaFxControllerClassIndex.MyInputFilter();
  private final FxmlDataIndexer myDataIndexer = new FxmlDataIndexer() {
    @Override
    protected IXMLBuilder createParseHandler(final String path, final Map<String, Set<String>> map) {
      return new NanoXmlBuilder() {
        public boolean myFxRootUsed = false;

        @Override
        public void addAttribute(String key, String nsPrefix, String nsURI, String value, String type) throws Exception {
          if (!myFxRootUsed) {
            throw new StopException();
          }
          if (value != null && FxmlConstants.TYPE.equals(key)) {
            Set<String> paths = map.get(value);
            if (paths == null) {
              paths = new HashSet<String>();
              map.put(value, paths);
            }
            paths.add(path);
          }
        }

        @Override
        public void startElement(String name, String nsPrefix, String nsURI, String systemID, int lineNr) throws Exception {
          myFxRootUsed = FxmlConstants.FX_ROOT.equals(nsPrefix + ":" + name);
        }

        @Override
        public void elementAttributesProcessed(String name, String nsPrefix, String nsURI) throws Exception {
          throw new StopException();
        }
      };
    }
  };
  private final FxmlDataExternalizer myDataExternalizer = new FxmlDataExternalizer();

  @Nonnull
  @Override
  public DataIndexer<String, Set<String>, FileContent> getIndexer() {
    return myDataIndexer;
  }

  @Nonnull
  @Override
  public DataExternalizer<Set<String>> getValueExternalizer() {
    return myDataExternalizer;
  }

  @Nonnull
  @Override
  public FileBasedIndex.InputFilter getInputFilter() {
    return myInputFilter;
  }

  @Nonnull
  @Override
  public ID<String, Set<String>> getName() {
    return KEY;
  }

  @Nonnull
  @Override
  public KeyDescriptor<String> getKeyDescriptor() {
    return myKeyDescriptor;
  }

  @Override
  public boolean dependsOnFileContent() {
    return true;
  }

  @Override
  public int getVersion() {
    return 0;
  }

  public static <T> List<T> findCustomFxml(final Project project,
                                           @Nonnull final String className,
                                           final Function<VirtualFile, T> f,
                                           final GlobalSearchScope scope) {
    return ApplicationManager.getApplication().runReadAction(new Computable<List<T>>() {
      @Override
      public List<T> compute() {
        final Collection<VirtualFile> files;
        try {
          files = FileBasedIndex.getInstance().getContainingFiles(KEY, className,
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
