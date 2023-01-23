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

import java.util.List;

import javax.annotation.Nonnull;
import com.intellij.codeInsight.daemon.DaemonAnalyzerTestCase;
import consulo.codeEditor.Editor;
import consulo.ide.impl.idea.codeInsight.intention.impl.ShowIntentionActionsHandler;
import consulo.language.psi.PsiFile;
import com.intellij.testFramework.PsiTestUtil;
import consulo.language.editor.intention.IntentionAction;
import consulo.language.editor.rawHighlight.HighlightInfo;

public abstract class JavaFXExpandAttributeTest extends DaemonAnalyzerTestCase {
  @Override
  protected void setUpModule() {
    super.setUpModule();
    PsiTestUtil.addLibrary(getModule(), "javafx", "testData", "jfxrt.jar");
  }

  public void testDefaultAttr() throws Exception {
    doTest(false, "fx:id");
  }

  public void testSimple() throws Exception {
    doTest(true, "disable");
  }

  public void testStaticAttr() throws Exception {
    doTest(true, "GridPane.columnIndex");
  }

  public void testExpandMultipleVals2List() throws Exception {
    doTest(true, "stylesheets");
  }

  public void testExpandVal2List() throws Exception {
    doTest(true, "stylesheets");
  }

  private void doTest(boolean available, final String attrName) throws Exception {
    configureByFiles(null, getTestName(true) + ".fxml");
    
    final List<HighlightInfo> infos = doHighlighting();
    Editor editor = getEditor();
    PsiFile file = getFile();

    final String actionName = "Expand '" + attrName + "' to tag";
    IntentionAction intentionAction = findIntentionAction(infos, actionName, editor, file);

    if (available) {
      assertNotNull(actionName, intentionAction);
      assertTrue(consulo.ide.impl.idea.codeInsight.intention.impl.ShowIntentionActionsHandler.chooseActionAndInvoke(file, editor, intentionAction, actionName));
      checkResultByFile(getTestName(true) + "_after.fxml");
    } else {
      assertNull(intentionAction);
    }
  }

  @Nonnull
  @Override
  protected String getTestDataPath() {
    return "testData/intentions/expandAttr/";
  }
}
