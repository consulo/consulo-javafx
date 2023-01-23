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
package consulo.javaFX.desktop.action;

import consulo.annotation.component.ActionImpl;
import consulo.annotation.component.ActionParentRef;
import consulo.annotation.component.ActionRef;
import consulo.application.CommonBundle;
import consulo.application.util.SystemInfo;
import consulo.fileChooser.IdeaFileChooser;
import consulo.java.execution.configurations.OwnJavaParameters;
import consulo.javaFX.desktop.JavaFxSettings;
import consulo.javaFX.desktop.JavaFxSettingsConfigurable;
import consulo.javaFX.fxml.FXMLFileType;
import consulo.language.editor.CommonDataKeys;
import consulo.language.util.ModuleUtilCore;
import consulo.logging.Logger;
import consulo.module.Module;
import consulo.process.PathEnvironmentVariableUtil;
import consulo.process.ProcessHandler;
import consulo.process.cmd.GeneralCommandLine;
import consulo.project.Project;
import consulo.ui.ex.action.AnAction;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.Presentation;
import consulo.ui.ex.awt.Messages;
import consulo.util.collection.ArrayUtil;
import consulo.util.io.FileUtil;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * User: anna
 * Date: 2/14/13
 */
@ActionImpl(id = "OpenInSceneBuilder", parents = {
  @ActionParentRef(@ActionRef(id = "EditorPopupMenu")),
  @ActionParentRef(@ActionRef(id = "ProjectViewPopupMenu"))
})
public class OpenInSceneBuilderAction extends AnAction {
  private static final Logger LOG = Logger.getInstance(OpenInSceneBuilderAction.class);
  public static final String ORACLE = "Oracle";

  public OpenInSceneBuilderAction() {
    super("Open In SceneBuilder");
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    final VirtualFile virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
    LOG.assertTrue(virtualFile != null);
    final String path = virtualFile.getPath();

    Project project = e.getData(Project.KEY);
    final JavaFxSettings settings = JavaFxSettings.getInstance();
    String pathToSceneBuilder = settings.getPathToSceneBuilder();
    if (StringUtil.isEmptyOrSpaces(settings.getPathToSceneBuilder())) {
      final VirtualFile sceneBuilderFile = IdeaFileChooser.chooseFile(JavaFxSettingsConfigurable.createSceneBuilderDescriptor(), project,
                                                                      getPredefinedPath());
      if (sceneBuilderFile == null) return;

      pathToSceneBuilder = sceneBuilderFile.getPath();
      settings.setPathToSceneBuilder(FileUtil.toSystemIndependentName(pathToSceneBuilder));
    }

    if (project != null) {
      final Module module = ModuleUtilCore.findModuleForFile(virtualFile, project);
      if (module != null) {
        try {
          final OwnJavaParameters javaParameters = new OwnJavaParameters();
          javaParameters.configureByModule(module, OwnJavaParameters.JDK_AND_CLASSES);

          final File sceneBuilderLibsFile;
          if (SystemInfo.isMac) {
            sceneBuilderLibsFile = new File(new File(pathToSceneBuilder, "Contents"), "Java");
          }
          else if (SystemInfo.isWindows) {
            File sceneBuilderRoot = new File(pathToSceneBuilder);
            File sceneBuilderRootDir = sceneBuilderRoot.getParentFile();
            if (sceneBuilderRootDir == null) {
              final File foundInPath = PathEnvironmentVariableUtil.findInPath(pathToSceneBuilder);
              if (foundInPath != null) {
                sceneBuilderRootDir = foundInPath.getParentFile();
              }
            }
            sceneBuilderRoot = sceneBuilderRootDir != null ? sceneBuilderRootDir.getParentFile() : null;
            if (sceneBuilderRoot != null) {
              final File libFile = new File(sceneBuilderRoot, "lib");
              if (libFile.isDirectory()) {
                sceneBuilderLibsFile = libFile;
              }
              else {
                final File appFile = new File(sceneBuilderRootDir, "app");
                sceneBuilderLibsFile = appFile.isDirectory() ? appFile : null;
              }
            }
            else {
              sceneBuilderLibsFile = null;
            }
          }
          else {
            sceneBuilderLibsFile = new File(new File(pathToSceneBuilder).getParent(), "app");
          }
          if (sceneBuilderLibsFile != null) {
            final File[] sceneBuilderLibs = sceneBuilderLibsFile.listFiles();
            if (sceneBuilderLibs != null) {
              for (File jarFile : sceneBuilderLibs) {
                javaParameters.getClassPath().add(jarFile.getPath());
              }
              javaParameters.setMainClass("com.oracle.javafx.authoring.Main");
              javaParameters.getProgramParametersList().add(path);

              final ProcessHandler processHandler = javaParameters.createOSProcessHandler();
              LOG.info("scene builder command line: " + javaParameters.toCommandLine());
              processHandler.startNotify();
              return;
            }
          }
        }
        catch (Throwable ex) {
          LOG.info(ex);
        }
      }
    }

    if (SystemInfo.isMac) {
      pathToSceneBuilder += "/Contents/MacOS/scenebuilder-launcher.sh";
    }

    final GeneralCommandLine commandLine = new GeneralCommandLine();
    try {
      commandLine.setExePath(FileUtil.toSystemDependentName(pathToSceneBuilder));
      commandLine.addParameter(path);
      commandLine.createProcess();
    }
    catch (Exception ex) {
      Messages.showErrorDialog("Failed to start SceneBuilder: " + commandLine.getCommandLineString(), CommonBundle.getErrorTitle());
    }
  }

  @Override
  public void update(AnActionEvent e) {
    final Presentation presentation = e.getPresentation();
    presentation.setEnabled(false);
    presentation.setVisible(false);
    final VirtualFile virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
    if (virtualFile != null &&
      FXMLFileType.isFxml(virtualFile) &&
      e.getData(Project.KEY) != null) {
      presentation.setEnabled(true);
      presentation.setVisible(true);
    }
  }

  @Nullable
  private static VirtualFile getPredefinedPath() {
    String path = null;
    if (SystemInfo.isWindows) {
      final String sb11 = File.separator + "JavaFX Scene Builder 1.1" + File.separator + "JavaFX Scene Builder 1.1.exe";
      final String sb10 = File.separator + "JavaFX Scene Builder 1.0" + File.separator + "bin" + File.separator + "scenebuilder.exe";
      final List<String> suspiciousPaths = new ArrayList<String>();
      final String programFiles = "C:\\Program Files";
      fillPaths(programFiles, sb11, sb10, suspiciousPaths);
      fillPaths(programFiles + " (x86)", sb11, sb10, suspiciousPaths);
      final File sb = findFirstThatExist(ArrayUtil.toStringArray(suspiciousPaths));
      if (sb != null) {
        path = sb.getPath();
      }
    }
    else if (SystemInfo.isMac) {
      final File sb = findFirstThatExist("/Applications/JavaFX Scene Builder 1.1.app", "/Applications/JavaFX Scene Builder 1.0.app");
      if (sb != null) {
        path = sb.getPath();
      }
    }
    else if (SystemInfo.isUnix) {
      path = "/opt/JavaFXSceneBuilder1.1/JavaFXSceneBuilder1.1";
    }

    return path != null ? LocalFileSystem.getInstance().findFileByPath(FileUtil.toSystemIndependentName(path)) : null;
  }


  // TODO REMOVE - replace by FileUtil
  @Nullable
  public static File findFirstThatExist(@Nonnull String... paths) {
    for (String path : paths) {
      if (!StringUtil.isEmptyOrSpaces(path)) {
        File file = new File(FileUtil.toSystemDependentName(path));
        if (file.exists()) return file;
      }
    }

    return null;
  }

  private static void fillPaths(String programFilesPath, String sb11, String sb10, List<String> suspiciousPaths) {
    suspiciousPaths.add(new File(programFilesPath, ORACLE).getPath() + sb11);
    suspiciousPaths.add(new File(programFilesPath, ORACLE).getPath() + sb10);
  }
}
