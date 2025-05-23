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
import com.intellij.codeInsight.daemon.DaemonAnalyzerTestCase;
import com.intellij.psi.PsiClass;
import consulo.language.editor.refactoring.rename.inplace.MemberInplaceRenameHandler;
import consulo.language.psi.PsiElement;
import consulo.language.editor.refactoring.rename.RenameProcessor;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestUtil;
import consulo.language.editor.TargetElementUtil;
import consulo.codeInsight.TargetElementUtilEx;
import consulo.util.collection.ContainerUtil;

public abstract class JavaFXRenameTest extends DaemonAnalyzerTestCase {
  @Override
  protected void setUpModule() {
    super.setUpModule();
    PsiTestUtil.addLibrary(getModule(), "javafx", "/testData", "jfxrt.jar");
  }

  public void testCustomComponent() throws Exception {
    doTest(getTestName(false) + "1");
  }

  public void testControllerField() throws Exception {
    doTest("newFieldName");
  }

  public void testControllerFieldWithRefs() throws Exception {
    doTest("newFieldName");
  }

  public void testHandler() throws Exception {
    doTest("newHandlerName");
  }

  public void testCustomComponentTag() throws Exception {
    doTest("Foo", true);
  }

  public void testCustomComponentPropertyTag() throws Exception {
    doTest("Foo", true);
  }

  public void testFromReference() throws Exception {
    final String newName = "lbl1";
    doTest(newName);
    final PsiClass controllerClass = findClass(getTestName(false));
    assertNotNull(controllerClass);
    assertNotNull(controllerClass.findFieldByName(newName, false));
  }

  public void testIdWithRefs() throws Exception {
    configureByFiles(null, getTestName(true) + ".fxml");
    PsiElement element = TargetElementUtil
      .findTargetElement(myEditor, ContainerUtil.newHashSet(TargetElementUtilEx.ELEMENT_NAME_ACCEPTED, TargetElementUtilEx.REFERENCED_ELEMENT_ACCEPTED));
    assertNotNull(element);
    new RenameProcessor(getProject(), element, "lb1", true, true).run();
    checkResultByFile(getTestName(true) + "_after.fxml");
  }

  private void doTest(final String newName) throws Exception {
    doTest(newName, false);
  }

  private void doTest(final String newName, boolean inline) throws Exception {
    configureByFiles(null, getTestName(true) + ".fxml", getTestName(false) + ".java");
    PsiElement element = TargetElementUtil
      .findTargetElement(myEditor, ContainerUtil.newHashSet(TargetElementUtilEx.ELEMENT_NAME_ACCEPTED, TargetElementUtilEx.REFERENCED_ELEMENT_ACCEPTED));
    assertNotNull(element);
    if (inline) {
      CodeInsightTestUtil.doInlineRename(new MemberInplaceRenameHandler(), newName, getEditor(), element);
    } else {
      new RenameProcessor(getProject(), element, newName, true, true).run();
    }
    checkResultByFile(getTestName(true) + "_after.fxml");
  }

  @Nonnull
  @Override
  protected String getTestDataPath() {
    return "/testData/rename/";
  }
}
