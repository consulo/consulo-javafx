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

import consulo.module.Module;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.TestModuleDescriptor;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import consulo.module.content.layer.ContentEntry;
import consulo.module.content.layer.ModifiableRootModel;

/**
 * User: anna
 * Date: 3/20/13
 */
public abstract class AbstractJavaFXTestCase extends LightCodeInsightFixtureTestCase {
  public static final DefaultLightProjectDescriptor JAVA_FX_DESCRIPTOR = new DefaultLightProjectDescriptor() {
    @Override
       public void configureModule(Module module, ModifiableRootModel model, ContentEntry contentEntry) {
       PsiTestUtil.addLibrary(module, model, "javafx", "testData", "jfxrt.jar");
       super.configureModule(module, model, contentEntry);
     }
   };

  @Nonnull
  @Override
  protected TestModuleDescriptor getProjectDescriptor() {
    return JAVA_FX_DESCRIPTOR;
  }

  protected void enableInspections() {}
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    enableInspections();
  }

  
}
