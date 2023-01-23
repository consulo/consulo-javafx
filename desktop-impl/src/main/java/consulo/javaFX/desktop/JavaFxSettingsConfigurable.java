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
package consulo.javaFX.desktop;

import consulo.annotation.component.ExtensionImpl;
import consulo.configurable.ApplicationConfigurable;
import consulo.configurable.SimpleConfigurableByProperties;
import consulo.disposer.Disposable;
import consulo.fileChooser.FileChooserDescriptor;
import consulo.fileChooser.FileChooserDescriptorFactory;
import consulo.localize.LocalizeValue;
import consulo.ui.Component;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.FileChooserTextBoxBuilder;
import consulo.ui.layout.VerticalLayout;
import consulo.ui.util.LabeledBuilder;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: anna
 * Date: 2/14/13
 */
@ExtensionImpl
public class JavaFxSettingsConfigurable extends SimpleConfigurableByProperties implements ApplicationConfigurable {

  private final Provider<JavaFxSettings> mySettings;

  @Inject
  public JavaFxSettingsConfigurable(Provider<JavaFxSettings> settings) {
    mySettings = settings;
  }

  @RequiredUIAccess
  @Nonnull
  @Override
  protected Component createLayout(@Nonnull PropertyBuilder propertyBuilder,
                                   @Nonnull Disposable disposable) {
    JavaFxSettings javaFxSettings = mySettings.get();

    VerticalLayout layout = VerticalLayout.create();

    FileChooserTextBoxBuilder sceneChooserBuilder = FileChooserTextBoxBuilder.create(null);
    sceneChooserBuilder.fileChooserDescriptor(createSceneBuilderDescriptor());
    sceneChooserBuilder.dialogTitle("SceneBuilder Configuration");
    sceneChooserBuilder.dialogDescription("Select path to SceneBuilder executable");

    FileChooserTextBoxBuilder.Controller controller = sceneChooserBuilder.build();
    layout.add(LabeledBuilder.filled(LocalizeValue.localizeTODO("&Path to SceneBuilder:"), controller.getComponent()));
    propertyBuilder.add(controller::getValue,
                        controller::setValue,
                        javaFxSettings::getPathToSceneBuilder,
                        javaFxSettings::setPathToSceneBuilder);

    return layout;
  }

  @Nonnull
  @Override
  public String getId() {
    return "preferences.JavaFX";
  }

  @Nonnull
  @Override
  public String getDisplayName() {
    return "JavaFX";
  }

  @Nullable
  @Override
  public String getParentId() {
    return "editor";
  }

  public static FileChooserDescriptor createSceneBuilderDescriptor() {
    final FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFileOrExecutableAppDescriptor();
    descriptor.setTitle("SceneBuilder Configuration");
    descriptor.setDescription("Select path to SceneBuilder executable");
    return descriptor;
  }
}
