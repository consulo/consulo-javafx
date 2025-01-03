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

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import com.intellij.codeInsight.daemon.DaemonAnalyzerTestCase;
import consulo.ide.impl.idea.codeInsight.generation.ClassMember;
import com.intellij.codeInsight.generation.GenerateGetterAndSetterHandler;
import consulo.codeEditor.Editor;
import consulo.project.Project;
import com.intellij.testFramework.PsiTestUtil;

public abstract class GenerateGetterSetterTest extends DaemonAnalyzerTestCase {
  public void testDouble() throws Exception {
    doTest();
  }

  @Override
  protected void setUpModule() {
    super.setUpModule();
    PsiTestUtil.addLibrary(getModule(), "javafx", "testData", "jfxrt.jar");
  }

  protected void doTest() throws Exception {
    configureByFile("/generateGetterSetter/before" + getTestName(false) + ".java");
    new GenerateGetterAndSetterHandler() {
      @Nullable
      @Override
      protected consulo.ide.impl.idea.codeInsight.generation.ClassMember[] chooseMembers(ClassMember[] members,
																						 boolean allowEmptySelection,
																						 boolean copyJavadocCheckbox,
																						 Project project,
																						 @Nullable Editor editor) {
        return members;
      }
    }.invoke(getProject(), getEditor(), getFile());
    checkResultByFile("/generateGetterSetter/after" + getTestName(false) + ".java");
  }
  
  @Nonnull
    @Override
    protected String getTestDataPath() {
      return "testData";
    }
}
