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
package org.jetbrains.plugins.javaFX.fxml;

import jakarta.annotation.Nonnull;
import consulo.ide.impl.idea.codeInsight.actions.OptimizeImportsProcessor;

public abstract class JavaFXOptimizeImportsTest extends AbstractJavaFXTestCase {
  public void testCollapseOnDemand() throws Exception {
    doTest();
  }

  public void testRemoveUnused() throws Exception {
    doTest();
  }

  public void testDblImports() throws Exception {
    doTest();
  }

  public void testStaticPropertiesAttrAndCustomComponents() throws Exception {
    myFixture.addClass("import javafx.scene.layout.GridPane;\n" +
                       "public class MyGridPane extends GridPane {}\n");
    doTest();
  }

  public void testStaticPropertiesTagAndCustomComponents() throws Exception {
    myFixture.addClass("import javafx.scene.layout.GridPane;\n" +
                       "public class MyGridPane extends GridPane {}\n");
    doTest();
  }

  private void doTest() throws Exception {
    myFixture.configureByFile(getTestName(true) + ".fxml");
    new OptimizeImportsProcessor(getProject(), myFixture.getFile()).run();
    myFixture.checkResultByFile(getTestName(true) + "_after.fxml");
  }

  @Nonnull
  @Override
  protected String getTestDataPath() {
    return "testData/optimizeImports/";
  }
}
