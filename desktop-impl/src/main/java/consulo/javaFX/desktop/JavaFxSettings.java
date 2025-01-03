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

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.component.persist.PersistentStateComponent;
import consulo.component.persist.RoamingType;
import consulo.component.persist.State;
import consulo.component.persist.Storage;
import consulo.ide.ServiceManager;
import consulo.util.xml.serializer.XmlSerializerUtil;
import jakarta.inject.Singleton;

import jakarta.annotation.Nullable;

/**
 * User: anna
 * Date: 2/14/13
 */
@Singleton
@State(name = "JavaFxSettings", storages = {@Storage(value = "javafx.xml", roamingType = RoamingType.PER_OS)})
@ServiceAPI(ComponentScope.APPLICATION)
@ServiceImpl
public class JavaFxSettings implements PersistentStateComponent<JavaFxSettings> {
  public String myPathToSceneBuilder = null;

  public static JavaFxSettings getInstance() {
    return ServiceManager.getService(JavaFxSettings.class);
  }

  public JavaFxSettings getState() {
    return this;
  }

  public void loadState(JavaFxSettings object) {
    XmlSerializerUtil.copyBean(object, this);
  }

  @Nullable
  public String getPathToSceneBuilder() {
    return myPathToSceneBuilder;
  }

  public void setPathToSceneBuilder(String pathToSceneBuilder) {
    myPathToSceneBuilder = pathToSceneBuilder;
  }
}