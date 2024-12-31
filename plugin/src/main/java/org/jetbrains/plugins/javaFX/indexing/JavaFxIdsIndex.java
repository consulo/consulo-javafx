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
import consulo.application.util.function.CommonProcessors;
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
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.NonNls;

import java.util.*;

@ExtensionImpl
public class JavaFxIdsIndex extends FileBasedIndexExtension<String, Set<String>> {

  @NonNls
  public static final ID<String, Set<String>> KEY = ID.create("javafx.id.name");

  private final KeyDescriptor<String> myKeyDescriptor = new EnumeratorStringDescriptor();
  private final FileBasedIndex.InputFilter myInputFilter = new JavaFxControllerClassIndex.MyInputFilter();
  private final FxmlDataIndexer myDataIndexer = new FxmlDataIndexer();
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
    return 1;
  }

  @Nonnull
  public static Collection<String> getAllRegisteredIds(Project project) {
    CommonProcessors.CollectUniquesProcessor<String> processor = new CommonProcessors.CollectUniquesProcessor<String>();
    FileBasedIndex.getInstance().processAllKeys(KEY, processor, project);
    final Collection<String> results = new ArrayList<String>(processor.getResults());
    final GlobalSearchScope searchScope = GlobalSearchScope.projectScope(project);
    for (Iterator<String> iterator = results.iterator(); iterator.hasNext(); ) {
      final String id = iterator.next();
      final List<Set<String>> values = FileBasedIndex.getInstance().getValues(KEY, id, searchScope);
      if (!values.isEmpty()) {
        final Set<String> pathSet = values.get(0);
        if (pathSet != null) {
          continue;
        }
      }
      iterator.remove();
    }
    return results;
  }

  @Nonnull
  public static Collection<String> getFilePaths(Project project, String id) {
    final List<Set<String>> values = FileBasedIndex.getInstance().getValues(KEY, id, GlobalSearchScope.projectScope(project));
    return (Collection<String>)(values.isEmpty() ? Collections.emptySet() : values.get(0));
  }
}