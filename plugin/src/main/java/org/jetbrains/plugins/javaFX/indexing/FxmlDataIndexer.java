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

import consulo.index.io.DataIndexer;
import consulo.language.psi.stub.FileContent;
import consulo.module.content.ProjectRootManager;
import consulo.project.Project;
import consulo.util.xml.fastReader.IXMLBuilder;
import consulo.util.xml.fastReader.NanoXmlUtil;
import consulo.virtualFileSystem.VirtualFile;
import org.jetbrains.plugins.javaFX.fxml.FxmlConstants;
import org.jetbrains.plugins.javaFX.fxml.JavaFxNamespaceDataProvider;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.io.StringReader;
import java.util.*;

/**
 * User: anna
 * Date: 3/14/13
 */
public class FxmlDataIndexer implements DataIndexer<String, Set<String>, FileContent> {
  @Override
  @Nonnull
  public Map<String, Set<String>> map(final FileContent inputData) {
    final Map<String, Set<String>> map = getIds(inputData.getContentAsText().toString(), inputData.getFile(), inputData.getProject());
    if (map != null) {
      return map;
    }
    return Collections.emptyMap();
  }

  @Nullable
  protected Map<String, Set<String>> getIds(String content, final VirtualFile file, Project project) {
    if (!content.contains(JavaFxNamespaceDataProvider.JAVAFX_NAMESPACE)) {
      return null;
    }

    final Map<String, Set<String>> map = new HashMap<String, Set<String>>();
    final String path = file.getPath();
    final IXMLBuilder handler = createParseHandler(path, map);
    try {
      NanoXmlUtil.parse(new StringReader(content), handler);
    }
    catch (StopException ignore) {
    }
    final VirtualFile sourceRoot = ProjectRootManager.getInstance(project).getFileIndex().getSourceRootForFile(file);
    endDocument(path, sourceRoot, map, handler);
    return map;
  }

  protected void endDocument(String math, VirtualFile sourceRoot, Map<String, Set<String>> map, IXMLBuilder handler) {
  }

  protected IXMLBuilder createParseHandler(final String path, final Map<String, Set<String>> map) {
    return new NanoXmlUtil.IXMLBuilderAdapter() {
      @Override
      public void addAttribute(String key, String nsPrefix, String nsURI, String value, String type) throws Exception {
        if (value != null && FxmlConstants.FX_ID.equals(nsPrefix + ":" + key)) {
          Set<String> paths = map.get(value);
          if (paths == null) {
            paths = new HashSet<String>();
            map.put(value, paths);
          }
          paths.add(path);
        }
      }
    };
  }

  protected static class StopException extends RuntimeException {
  }
}
