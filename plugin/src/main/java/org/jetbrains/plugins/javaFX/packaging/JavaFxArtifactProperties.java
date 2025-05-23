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
package org.jetbrains.plugins.javaFX.packaging;

import com.intellij.java.language.projectRoots.JavaSdkType;
import com.intellij.java.language.projectRoots.JavaSdkVersion;
import consulo.application.ApplicationManager;
import consulo.compiler.CompileContext;
import consulo.compiler.CompilerMessageCategory;
import consulo.compiler.artifact.Artifact;
import consulo.compiler.artifact.ArtifactManager;
import consulo.compiler.artifact.ArtifactProperties;
import consulo.compiler.artifact.ArtifactUtil;
import consulo.compiler.artifact.element.ArchivePackagingElement;
import consulo.compiler.artifact.element.ArtifactPackagingElement;
import consulo.compiler.artifact.element.PackagingElement;
import consulo.compiler.artifact.ui.ArtifactEditorContext;
import consulo.compiler.artifact.ui.ArtifactPropertiesEditor;
import consulo.content.bundle.Sdk;
import consulo.java.language.bundle.JavaSdkTypeUtil;
import consulo.java.language.module.extension.JavaModuleExtension;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.project.Project;
import consulo.util.xml.serializer.XmlSerializerUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.javaFX.packaging.preloader.JavaFxPreloaderArtifactProperties;
import org.jetbrains.plugins.javaFX.packaging.preloader.JavaFxPreloaderArtifactPropertiesProvider;
import org.jetbrains.plugins.javaFX.packaging.preloader.JavaFxPreloaderArtifactType;

import java.io.File;
import java.util.Collections;
import java.util.Set;
import java.util.function.Supplier;

/**
 * User: anna
 * Date: 3/12/13
 */
public class JavaFxArtifactProperties extends ArtifactProperties<JavaFxArtifactProperties> {
  private String myTitle;
  private String myVendor;
  private String myDescription;
  private String myAppClass;
  private String myWidth = JavaFxPackagerConstants.DEFAULT_WEIGHT;
  private String myHeight = JavaFxPackagerConstants.DEFAULT_HEIGHT;
  private String myHtmlParamFile;
  private String myParamFile;
  private String myUpdateMode = JavaFxPackagerConstants.UPDATE_MODE_BACKGROUND;

  private boolean myEnabledSigning = false;
  private boolean mySelfSigning = true;
  private String myAlias;
  private String myKeystore;
  private String myStorepass;
  private String myKeypass;
  private boolean myConvertCss2Bin;
  private String myNativeBundle = JavaFxPackagerConstants.NativeBundles.none.name();

  @Override
  public void onBuildFinished(@Nonnull final Artifact artifact, @Nonnull final CompileContext compileContext) {
    if (!(artifact.getArtifactType() instanceof JavaFxApplicationArtifactType)) {
      return;
    }
    final Project project = compileContext.getProject();
    final Set<Module> modules = ApplicationManager.getApplication().runReadAction(new Supplier<Set<Module>>() {
      @Override
      public Set<Module> get() {
        return ArtifactUtil.getModulesIncludedInArtifacts(Collections.singletonList(artifact), project);
      }
    });
    if (modules.isEmpty()) {
      return;
    }

    Sdk fxCompatibleSdk = null;
    for (Module module : modules) {
      final Sdk sdk = ModuleUtilCore.getSdk(module, JavaModuleExtension.class);
      if (sdk != null && sdk.getSdkType() instanceof JavaSdkType) {
        if (JavaSdkTypeUtil.isOfVersionOrHigher(sdk, JavaSdkVersion.JDK_1_7)) {
          fxCompatibleSdk = sdk;
          break;
        }
      }
    }

    if (fxCompatibleSdk == null) {
      compileContext.addMessage(consulo.compiler.CompilerMessageCategory.ERROR,
                                "Java version 7 or higher is required to build JavaFX package",
                                null,
                                -1,
                                -1);
      return;
    }

    final JavaFxArtifactProperties properties =
      (JavaFxArtifactProperties)artifact.getProperties(JavaFxArtifactPropertiesProvider.getInstance());

    final JavaFxPackager javaFxPackager = new JavaFxPackager(artifact, properties, project) {
      @Override
      protected void registerJavaFxPackagerError(String message) {
        compileContext.addMessage(CompilerMessageCategory.ERROR, message, null, -1, -1);
      }
    };
    javaFxPackager.buildJavaFxArtifact(fxCompatibleSdk.getHomePath());
  }

  @Override
  public ArtifactPropertiesEditor createEditor(@Nonnull ArtifactEditorContext context) {
    return new JavaFxArtifactPropertiesEditor(this, context.getProject(), context.getArtifact());
  }

  @Nullable
  @Override
  public JavaFxArtifactProperties getState() {
    return this;
  }

  @Override
  public void loadState(JavaFxArtifactProperties state) {
    XmlSerializerUtil.copyBean(state, this);
  }

  public String getTitle() {
    return myTitle;
  }

  public void setTitle(String title) {
    myTitle = title;
  }

  public String getVendor() {
    return myVendor;
  }

  public void setVendor(String vendor) {
    myVendor = vendor;
  }

  public String getDescription() {
    return myDescription;
  }

  public void setDescription(String description) {
    myDescription = description;
  }

  public String getAppClass() {
    return myAppClass;
  }

  public void setAppClass(String appClass) {
    myAppClass = appClass;
  }

  public String getWidth() {
    return myWidth;
  }

  public String getHeight() {
    return myHeight;
  }

  public void setWidth(String width) {
    myWidth = width;
  }

  public void setHeight(String height) {
    myHeight = height;
  }

  public String getHtmlParamFile() {
    return myHtmlParamFile;
  }

  public void setHtmlParamFile(String htmlParamFile) {
    myHtmlParamFile = htmlParamFile;
  }

  public String getParamFile() {
    return myParamFile;
  }

  public void setParamFile(String paramFile) {
    myParamFile = paramFile;
  }

  public String getUpdateMode() {
    return myUpdateMode;
  }

  public void setUpdateMode(String updateMode) {
    myUpdateMode = updateMode;
  }

  public boolean isEnabledSigning() {
    return myEnabledSigning;
  }

  public void setEnabledSigning(boolean enabledSigning) {
    myEnabledSigning = enabledSigning;
  }

  public boolean isSelfSigning() {
    return mySelfSigning;
  }

  public void setSelfSigning(boolean selfSigning) {
    mySelfSigning = selfSigning;
  }

  public String getAlias() {
    return myAlias;
  }

  public void setAlias(String alias) {
    myAlias = alias;
  }

  public String getKeystore() {
    return myKeystore;
  }

  public void setKeystore(String keystore) {
    myKeystore = keystore;
  }

  public String getStorepass() {
    return myStorepass;
  }

  public void setStorepass(String storepass) {
    myStorepass = storepass;
  }

  public String getKeypass() {
    return myKeypass;
  }

  public void setKeypass(String keypass) {
    myKeypass = keypass;
  }

  public boolean isConvertCss2Bin() {
    return myConvertCss2Bin;
  }

  public void setConvertCss2Bin(boolean convertCss2Bin) {
    myConvertCss2Bin = convertCss2Bin;
  }

  public String getPreloaderClass(Artifact rootArtifact, Project project) {
    final Artifact artifact = getPreloaderArtifact(rootArtifact, project);
    if (artifact != null) {
      final JavaFxPreloaderArtifactProperties properties =
        (JavaFxPreloaderArtifactProperties)artifact.getProperties(JavaFxPreloaderArtifactPropertiesProvider.getInstance());
      return properties.getPreloaderClass();
    }
    return null;
  }

  public String getPreloaderJar(Artifact rootArtifact, Project project) {
    final Artifact artifact = getPreloaderArtifact(rootArtifact, project);
    if (artifact != null) {
      return ((ArchivePackagingElement)artifact.getRootElement()).getArchiveFileName();
    }
    return null;
  }


  private static Artifact getPreloaderArtifact(Artifact rootArtifact, Project project) {
    for (PackagingElement<?> element : rootArtifact.getRootElement().getChildren()) {
      if (element instanceof ArtifactPackagingElement) {
        final Artifact artifact = ((ArtifactPackagingElement)element)
          .findArtifact(ArtifactManager.getInstance(project).getResolvingContext());
        if (artifact != null && artifact.getArtifactType() instanceof JavaFxPreloaderArtifactType) {
          return artifact;
        }
      }
    }
    return null;
  }

  public String getNativeBundle() {
    return myNativeBundle;
  }

  public void setNativeBundle(String nativeBundle) {
    myNativeBundle = nativeBundle;
  }

  public static abstract class JavaFxPackager extends AbstractJavaFxPackager {
    private final Artifact myArtifact;
    private final JavaFxArtifactProperties myProperties;
    private final Project myProject;

    public JavaFxPackager(Artifact artifact, JavaFxArtifactProperties properties, Project project) {
      myArtifact = artifact;
      myProperties = properties;
      myProject = project;
    }

    @Override
    protected String getArtifactName() {
      return myArtifact.getName();
    }

    @Override
    protected String getArtifactOutputPath() {
      return myArtifact.getOutputPath();
    }

    @Override
    protected String getArtifactOutputFilePath() {
      for (PackagingElement<?> element : myArtifact.getRootElement().getChildren()) {
        if (element instanceof ArchivePackagingElement) {
          return myArtifact.getOutputFilePath() + File.separator + ((ArchivePackagingElement)element)
            .getArchiveFileName();
        }
      }
      return myArtifact.getOutputFilePath();
    }

    @Override
    protected String getAppClass() {
      return myProperties.getAppClass();
    }

    @Override
    protected String getTitle() {
      return myProperties.getTitle();
    }

    @Override
    protected String getVendor() {
      return myProperties.getVendor();
    }

    @Override
    protected String getDescription() {
      return myProperties.getDescription();
    }

    @Override
    protected String getWidth() {
      return myProperties.getWidth();
    }

    @Override
    protected String getHeight() {
      return myProperties.getHeight();
    }

    @Override
    public String getPreloaderClass() {
      return myProperties.getPreloaderClass(myArtifact, myProject);
    }

    @Override
    public String getPreloaderJar() {
      return myProperties.getPreloaderJar(myArtifact, myProject);
    }

    @Override
    public boolean convertCss2Bin() {
      return myProperties.isConvertCss2Bin();
    }

    @Override
    protected String getHtmlParamFile() {
      return myProperties.getHtmlParamFile();
    }

    @Override
    protected String getParamFile() {
      return myProperties.getParamFile();
    }

    @Override
    protected String getUpdateMode() {
      return myProperties.getUpdateMode();
    }

    @Override
    protected JavaFxPackagerConstants.NativeBundles getNativeBundle() {
      return JavaFxPackagerConstants.NativeBundles.valueOf(myProperties.getNativeBundle());
    }

    @Override
    public String getKeypass() {
      return myProperties.getKeypass();
    }

    @Override
    public String getStorepass() {
      return myProperties.getStorepass();
    }

    @Override
    public String getKeystore() {
      return myProperties.getKeystore();
    }

    @Override
    public String getAlias() {
      return myProperties.getAlias();
    }

    @Override
    public boolean isSelfSigning() {
      return myProperties.isSelfSigning();
    }

    @Override
    public boolean isEnabledSigning() {
      return myProperties.isEnabledSigning();
    }
  }
}
