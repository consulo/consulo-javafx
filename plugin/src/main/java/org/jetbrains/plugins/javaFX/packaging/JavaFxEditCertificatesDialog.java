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

import consulo.ui.ex.awt.Messages;
import consulo.ui.ex.awt.TextFieldWithBrowseButton;
import consulo.ui.ex.awt.util.BrowseFilesListener;
import consulo.project.Project;
import consulo.ui.ex.awt.DialogWrapper;
import consulo.ui.ex.awt.UIUtil;
import consulo.util.lang.StringUtil;

import jakarta.annotation.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Base64;

/**
 * User: anna
 * Date: 3/15/13
 */
public class JavaFxEditCertificatesDialog extends DialogWrapper {

  Panel myPanel;

  protected JavaFxEditCertificatesDialog(JComponent parent, JavaFxArtifactProperties properties, Project project) {
    super(parent, true);
    setTitle("Choose Certificate");
    init();
    final ActionListener actionListener = new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        UIUtil.setEnabled(myPanel.myKeysPanel, !myPanel.mySelfSignedRadioButton.isSelected(), true);
      }
    };
    myPanel.mySelfSignedRadioButton.addActionListener(actionListener);
    myPanel.mySignedByKeyRadioButton.addActionListener(actionListener);
    final boolean selfSigning = properties.isSelfSigning();
    UIUtil.setEnabled(myPanel.myKeysPanel, !selfSigning, true);
    myPanel.mySelfSignedRadioButton.setSelected(selfSigning);
    myPanel.mySignedByKeyRadioButton.setSelected(!selfSigning);

    myPanel.myAliasTF.setText(properties.getAlias());
    myPanel.myKeystore.setText(properties.getKeystore());
    final String keypass = properties.getKeypass();
    myPanel.myKeypassTF.setText(keypass != null ? new String(Base64.getDecoder().decode(keypass)) : "");
    final String storepass = properties.getStorepass();
    myPanel.myStorePassTF.setText(storepass != null ? new String(Base64.getDecoder().decode(storepass)) : "");
    myPanel.myKeystore.addBrowseFolderListener("Choose Keystore File", "Select file containing generated keys", project, BrowseFilesListener.SINGLE_FILE_DESCRIPTOR);
  }

  @Override
  protected void doOKAction() {
    if (myPanel.mySignedByKeyRadioButton.isSelected()) {
      if (StringUtil.isEmptyOrSpaces(myPanel.myAliasTF.getText())) {
        Messages.showErrorDialog(myPanel.myWholePanel, "Alias should be non-empty");
        return;
      }
      final String keystore = myPanel.myKeystore.getText();
      if (StringUtil.isEmptyOrSpaces(keystore)) {
        Messages.showErrorDialog(myPanel.myWholePanel, "Path to the keystore file should be set");
        return;
      }
      if (!new File(keystore).isFile()) {
        Messages.showErrorDialog(myPanel.myWholePanel, "Keystore file should exist");
        return;
      }
      if (StringUtil.isEmptyOrSpaces(String.valueOf(myPanel.myKeypassTF.getPassword())) ||
          StringUtil.isEmptyOrSpaces(String.valueOf(myPanel.myStorePassTF.getPassword()))) {
        Messages.showErrorDialog(myPanel.myWholePanel, "Passwords should be set");
        return;
      }
    }
    super.doOKAction();
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    myPanel = new Panel();
    return myPanel.myWholePanel;
  }

  @Override
  protected void dispose() {
    super.dispose();
  }

  protected static class Panel {
    JRadioButton mySelfSignedRadioButton;
    JRadioButton mySignedByKeyRadioButton;
    JPasswordField myStorePassTF;
    JPasswordField myKeypassTF;
    JTextField myAliasTF;
    TextFieldWithBrowseButton myKeystore;
    JPanel myWholePanel;
    JPanel myKeysPanel;
  }
}
