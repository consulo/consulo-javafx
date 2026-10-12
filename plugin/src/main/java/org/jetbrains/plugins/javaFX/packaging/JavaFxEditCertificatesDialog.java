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

import consulo.disposer.Disposable;
import consulo.fileChooser.FileChooserDescriptorFactory;
import consulo.fileChooser.FileChooserTextBoxBuilder;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.ui.Component;
import consulo.ui.MessageBoxes;
import consulo.ui.PasswordBox;
import consulo.ui.RadioButton;
import consulo.ui.RadioGroup;
import consulo.ui.TextBox;
import consulo.ui.Window;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.AnAction;
import consulo.ui.ex.dialog.DialogDescriptor;
import consulo.ui.ex.dialog.DialogValue;
import consulo.ui.layout.VerticalLayout;
import consulo.ui.util.FormBuilder;
import consulo.util.lang.StringUtil;
import org.jetbrains.plugins.javaFX.localize.JavaFXLocalize;
import org.jspecify.annotations.Nullable;

import java.io.File;

public class JavaFxEditCertificatesDialog extends DialogDescriptor {
    public record Result(boolean selfSigning, String alias, String keystore, String keypass, String storepass) implements DialogValue {
    }

    private final Project myProject;
    private final Result myInitial;

    private @Nullable RadioGroup<Boolean> mySigningGroup;
    private @Nullable TextBox myAliasField;
    private FileChooserTextBoxBuilder.@Nullable Controller myKeystoreField;
    private @Nullable PasswordBox myKeypassField;
    private @Nullable PasswordBox myStorepassField;

    public JavaFxEditCertificatesDialog(Project project, Result initial) {
        super(JavaFXLocalize.certificatesDialogTitle());
        myProject = project;
        myInitial = initial;
    }

    @Override
    @RequiredUIAccess
    public Component createCenterComponent(Disposable uiDisposable) {
        RadioGroup<Boolean> signingGroup = RadioGroup.create();
        RadioButton selfSignedRadioButton = signingGroup.newButton(JavaFXLocalize.certificatesSelfSignedRadio(), Boolean.TRUE);
        RadioButton signedByKeyRadioButton = signingGroup.newButton(JavaFXLocalize.certificatesSignedByKeyRadio(), Boolean.FALSE);
        signingGroup.setValue(myInitial.selfSigning());

        TextBox aliasField = TextBox.create(myInitial.alias());
        FileChooserTextBoxBuilder.Controller keystoreField = FileChooserTextBoxBuilder.create(myProject)
            .dialogTitle(JavaFXLocalize.certificatesKeystoreChooseTitle())
            .dialogDescription(JavaFXLocalize.certificatesKeystoreChooseDescription())
            .fileChooserDescriptor(FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor())
            .uiDisposable(uiDisposable)
            .build();
        keystoreField.setValue(myInitial.keystore(), false);
        PasswordBox keypassField = PasswordBox.create(myInitial.keypass());
        PasswordBox storepassField = PasswordBox.create(myInitial.storepass());

        FormBuilder keysBuilder = FormBuilder.create();
        keysBuilder.addLabeled(JavaFXLocalize.certificatesAliasLabel(), aliasField);
        keysBuilder.addLabeled(JavaFXLocalize.certificatesKeystoreLabel(), keystoreField.getComponent());
        keysBuilder.addLabeled(JavaFXLocalize.certificatesKeypassLabel(), keypassField);
        keysBuilder.addLabeled(JavaFXLocalize.certificatesStorepassLabel(), storepassField);
        Component keysPanel = keysBuilder.build();
        keysPanel.setEnabledRecursive(!myInitial.selfSigning());

        signingGroup.addValueListener(selfSigning -> keysPanel.setEnabledRecursive(!Boolean.TRUE.equals(selfSigning)));

        mySigningGroup = signingGroup;
        myAliasField = aliasField;
        myKeystoreField = keystoreField;
        myKeypassField = keypassField;
        myStorepassField = storepassField;

        VerticalLayout layout = VerticalLayout.create();
        layout.add(selfSignedRadioButton);
        layout.add(signedByKeyRadioButton);
        layout.add(keysPanel);
        return layout;
    }

    @Override
    public DialogValue getOkValue() {
        RadioGroup<Boolean> signingGroup = mySigningGroup;
        TextBox aliasField = myAliasField;
        FileChooserTextBoxBuilder.Controller keystoreField = myKeystoreField;
        PasswordBox keypassField = myKeypassField;
        PasswordBox storepassField = myStorepassField;
        if (signingGroup == null || aliasField == null || keystoreField == null || keypassField == null || storepassField == null) {
            return myInitial;
        }

        return new Result(
            Boolean.TRUE.equals(signingGroup.getValue()),
            StringUtil.notNullize(aliasField.getValue()),
            StringUtil.notNullize(keystoreField.getValue()),
            StringUtil.notNullize(keypassField.getValue()),
            StringUtil.notNullize(storepassField.getValue())
        );
    }

    @Override
    @RequiredUIAccess
    public boolean canHandle(AnAction action, @Nullable DialogValue value, Window window) {
        if (value instanceof Result result && !result.selfSigning()) {
            LocalizeValue error = validate(result);
            if (error.isNotEmpty()) {
                MessageBoxes.okError(error).showAsync(window);
                return false;
            }
        }
        return super.canHandle(action, value, window);
    }

    private static LocalizeValue validate(Result result) {
        if (StringUtil.isEmptyOrSpaces(result.alias())) {
            return JavaFXLocalize.certificatesAliasEmptyError();
        }
        if (StringUtil.isEmptyOrSpaces(result.keystore())) {
            return JavaFXLocalize.certificatesKeystoreEmptyError();
        }
        if (!new File(result.keystore()).isFile()) {
            return JavaFXLocalize.certificatesKeystoreMissingError();
        }
        if (StringUtil.isEmptyOrSpaces(result.keypass()) || StringUtil.isEmptyOrSpaces(result.storepass())) {
            return JavaFXLocalize.certificatesPasswordsEmptyError();
        }
        return LocalizeValue.empty();
    }
}
