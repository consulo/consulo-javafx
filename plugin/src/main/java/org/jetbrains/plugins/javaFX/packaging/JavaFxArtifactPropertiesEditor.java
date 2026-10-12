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

import consulo.application.Application;
import consulo.compiler.artifact.Artifact;
import consulo.compiler.artifact.ui.ArtifactPropertiesEditor;
import consulo.disposer.Disposable;
import consulo.fileChooser.FileChooserDescriptor;
import consulo.fileChooser.FileChooserDescriptorFactory;
import consulo.fileChooser.FileChooserTextBoxBuilder;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.ui.Button;
import consulo.ui.CheckBox;
import consulo.ui.ComboBox;
import consulo.ui.Component;
import consulo.ui.TextArea;
import consulo.ui.TextBox;
import consulo.ui.UIAccess;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.dialog.DialogService;
import consulo.ui.layout.HorizontalLayout;
import consulo.ui.util.FormBuilder;
import consulo.util.lang.StringUtil;
import org.jetbrains.plugins.javaFX.localize.JavaFXLocalize;
import org.jspecify.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

/**
 * User: anna
 * Date: 3/12/13
 */
public class JavaFxArtifactPropertiesEditor extends ArtifactPropertiesEditor {
    private final JavaFxArtifactProperties myProperties;
    private final Project myProject;
    private final Artifact myArtifact;

    private @Nullable TextBox myTitleField;
    private @Nullable TextBox myVendorField;
    private @Nullable TextArea myDescriptionField;
    private @Nullable TextBox myAppClassField;
    private @Nullable TextBox myWidthField;
    private @Nullable TextBox myHeightField;
    private FileChooserTextBoxBuilder.@Nullable Controller myHtmlParamsField;
    private FileChooserTextBoxBuilder.@Nullable Controller myParamsField;
    private @Nullable CheckBox myUpdateInBackgroundCheckBox;
    private @Nullable CheckBox myEnableSigningCheckBox;
    private @Nullable Button myEditSignCertificateButton;
    private @Nullable CheckBox myConvertCssToBinCheckBox;
    private @Nullable ComboBox<String> myNativeBundleBox;
    private JavaFxEditCertificatesDialog.@Nullable Result myCertificates;

    public JavaFxArtifactPropertiesEditor(JavaFxArtifactProperties properties, Project project, Artifact artifact) {
        myProperties = properties;
        myProject = project;
        myArtifact = artifact;
    }

    @Override
    public LocalizeValue getTabName() {
        return JavaFXLocalize.artifactTab();
    }

    @Override
    @RequiredUIAccess
    public Component createUIComponent(Disposable uiDisposable) {
        TextBox titleField = TextBox.create();
        TextBox vendorField = TextBox.create();
        TextArea descriptionField = TextArea.create();
        TextBox appClassField = TextBox.create();
        JavaFxApplicationClassBrowser.installOn(
            appClassField,
            myProject,
            myArtifact,
            JavaFXLocalize.artifactChooseApplicationClassTitle(),
            "javafx.application.Application"
        );
        TextBox widthField = TextBox.create();
        TextBox heightField = TextBox.create();

        FileChooserTextBoxBuilder.Controller htmlParamsField =
            createPropertiesFileField(JavaFXLocalize.artifactHtmlParametersDescription(), uiDisposable);
        FileChooserTextBoxBuilder.Controller paramsField =
            createPropertiesFileField(JavaFXLocalize.artifactParametersDescription(), uiDisposable);

        List<String> bundleNames = new ArrayList<>();
        for (JavaFxPackagerConstants.NativeBundles bundle : JavaFxPackagerConstants.NativeBundles.values()) {
            bundleNames.add(bundle.name());
        }
        ComboBox<String> nativeBundleBox = ComboBox.create(bundleNames);

        CheckBox updateInBackgroundCheckBox = CheckBox.create(JavaFXLocalize.artifactUpdateInBackgroundCheckbox());
        CheckBox enableSigningCheckBox = CheckBox.create(JavaFXLocalize.artifactEnableSigningCheckbox());
        Button editSignCertificateButton = Button.create(JavaFXLocalize.artifactEditCertificatesButton(), event -> editCertificates());
        enableSigningCheckBox.addValueListener(event -> editSignCertificateButton.setEnabled(Boolean.TRUE.equals(event.getValue())));
        CheckBox convertCssToBinCheckBox = CheckBox.create(JavaFXLocalize.artifactConvertCssToBinCheckbox());

        HorizontalLayout signingPanel = HorizontalLayout.create();
        signingPanel.add(enableSigningCheckBox);
        signingPanel.add(editSignCertificateButton);

        FormBuilder formBuilder = FormBuilder.create();
        formBuilder.addLabeled(JavaFXLocalize.artifactTitleLabel(), titleField);
        formBuilder.addLabeled(JavaFXLocalize.artifactVendorLabel(), vendorField);
        formBuilder.addLabeled(JavaFXLocalize.artifactDescriptionLabel(), descriptionField);
        formBuilder.addLabeled(JavaFXLocalize.artifactApplicationClassLabel(), appClassField);
        formBuilder.addLabeled(JavaFXLocalize.artifactWidthLabel(), widthField);
        formBuilder.addLabeled(JavaFXLocalize.artifactHeightLabel(), heightField);
        formBuilder.addLabeled(JavaFXLocalize.artifactHtmlParametersLabel(), htmlParamsField.getComponent());
        formBuilder.addLabeled(JavaFXLocalize.artifactParametersLabel(), paramsField.getComponent());
        formBuilder.addLabeled(JavaFXLocalize.artifactNativeBundleLabel(), nativeBundleBox);
        formBuilder.addBottom(updateInBackgroundCheckBox);
        formBuilder.addBottom(signingPanel);
        formBuilder.addBottom(convertCssToBinCheckBox);

        myTitleField = titleField;
        myVendorField = vendorField;
        myDescriptionField = descriptionField;
        myAppClassField = appClassField;
        myWidthField = widthField;
        myHeightField = heightField;
        myHtmlParamsField = htmlParamsField;
        myParamsField = paramsField;
        myNativeBundleBox = nativeBundleBox;
        myUpdateInBackgroundCheckBox = updateInBackgroundCheckBox;
        myEnableSigningCheckBox = enableSigningCheckBox;
        myEditSignCertificateButton = editSignCertificateButton;
        myConvertCssToBinCheckBox = convertCssToBinCheckBox;
        return formBuilder.build();
    }

    @RequiredUIAccess
    private FileChooserTextBoxBuilder.Controller createPropertiesFileField(LocalizeValue description, Disposable uiDisposable) {
        FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleLocalFileDescriptor()
            .withFileFilter(file -> Objects.equals(file.getExtension(), "properties"));
        return FileChooserTextBoxBuilder.create(myProject)
            .dialogTitle(JavaFXLocalize.artifactChoosePropertiesFileTitle())
            .dialogDescription(description)
            .fileChooserDescriptor(descriptor)
            .uiDisposable(uiDisposable)
            .build();
    }

    @RequiredUIAccess
    private void editCertificates() {
        JavaFxEditCertificatesDialog descriptor = new JavaFxEditCertificatesDialog(myProject, getCertificates());
        DialogService dialogService = Application.get().getInstance(DialogService.class);
        Button parent = myEditSignCertificateButton;
        UIAccess uiAccess = UIAccess.current();
        (parent == null ? dialogService.build(descriptor) : dialogService.build(parent, descriptor)).showAsync()
            .whenCompleteAsync((value, error) -> {
                if (error == null && value instanceof JavaFxEditCertificatesDialog.Result result) {
                    myCertificates = result;
                }
            }, uiAccess);
    }

    private JavaFxEditCertificatesDialog.Result getCertificates() {
        JavaFxEditCertificatesDialog.Result certificates = myCertificates;
        if (certificates != null) {
            return certificates;
        }
        return new JavaFxEditCertificatesDialog.Result(
            myProperties.isSelfSigning(),
            StringUtil.notNullize(myProperties.getAlias()),
            StringUtil.notNullize(myProperties.getKeystore()),
            decode(myProperties.getKeypass()),
            decode(myProperties.getStorepass())
        );
    }

    private static String decode(@Nullable String value) {
        return value != null ? new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8) : "";
    }

    private static @Nullable String encode(String value) {
        return StringUtil.isEmptyOrSpaces(value) ? null : Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    @RequiredUIAccess
    public boolean isModified() {
        if (myTitleField == null) {
            return false;
        }

        if (isModified(myProperties.getTitle(), myTitleField.getValue())) {
            return true;
        }
        if (isModified(myProperties.getVendor(), getValue(myVendorField))) {
            return true;
        }
        if (isModified(myProperties.getDescription(), myDescriptionField == null ? "" : myDescriptionField.getValue())) {
            return true;
        }
        if (isModified(myProperties.getWidth(), getValue(myWidthField))) {
            return true;
        }
        if (isModified(myProperties.getHeight(), getValue(myHeightField))) {
            return true;
        }
        if (isModified(myProperties.getAppClass(), getValue(myAppClassField))) {
            return true;
        }
        if (isModified(myProperties.getHtmlParamFile(), getValue(myHtmlParamsField))) {
            return true;
        }
        if (isModified(myProperties.getParamFile(), getValue(myParamsField))) {
            return true;
        }
        if (myNativeBundleBox != null && !Objects.equals(myNativeBundleBox.getValue(), myProperties.getNativeBundle())) {
            return true;
        }
        boolean inBackground = Objects.equals(myProperties.getUpdateMode(), JavaFxPackagerConstants.UPDATE_MODE_BACKGROUND);
        if (inBackground != isSelected(myUpdateInBackgroundCheckBox)) {
            return true;
        }
        if (myProperties.isEnabledSigning() != isSelected(myEnableSigningCheckBox)) {
            return true;
        }
        if (myProperties.isConvertCss2Bin() != isSelected(myConvertCssToBinCheckBox)) {
            return true;
        }

        JavaFxEditCertificatesDialog.Result certificates = myCertificates;
        if (certificates != null) {
            if (isModified(myProperties.getAlias(), certificates.alias())) {
                return true;
            }
            if (isModified(myProperties.getKeystore(), certificates.keystore())) {
                return true;
            }
            if (!decode(myProperties.getKeypass()).equals(certificates.keypass())) {
                return true;
            }
            if (!decode(myProperties.getStorepass()).equals(certificates.storepass())) {
                return true;
            }
            if (myProperties.isSelfSigning() != certificates.selfSigning()) {
                return true;
            }
        }
        return false;
    }

    private static boolean isModified(@Nullable String value, @Nullable String fieldValue) {
        return !Objects.equals(StringUtil.notNullize(value), StringUtil.notNullize(fieldValue).trim());
    }

    private static String getValue(@Nullable TextBox field) {
        return field == null ? "" : StringUtil.notNullize(field.getValue());
    }

    private static String getValue(FileChooserTextBoxBuilder.@Nullable Controller field) {
        return field == null ? "" : StringUtil.notNullize(field.getValue());
    }

    private static boolean isSelected(@Nullable CheckBox checkBox) {
        return checkBox != null && Boolean.TRUE.equals(checkBox.getValue());
    }

    @Override
    @RequiredUIAccess
    public void apply() {
        if (myTitleField == null) {
            return;
        }

        myProperties.setTitle(getValue(myTitleField));
        myProperties.setVendor(getValue(myVendorField));
        myProperties.setDescription(myDescriptionField == null ? "" : StringUtil.notNullize(myDescriptionField.getValue()));
        myProperties.setAppClass(getValue(myAppClassField));
        myProperties.setWidth(getValue(myWidthField));
        myProperties.setHeight(getValue(myHeightField));
        myProperties.setHtmlParamFile(getValue(myHtmlParamsField));
        myProperties.setParamFile(getValue(myParamsField));
        myProperties.setUpdateMode(
            isSelected(myUpdateInBackgroundCheckBox) ? JavaFxPackagerConstants.UPDATE_MODE_BACKGROUND : JavaFxPackagerConstants.UPDATE_MODE_ALWAYS
        );
        myProperties.setEnabledSigning(isSelected(myEnableSigningCheckBox));
        myProperties.setConvertCss2Bin(isSelected(myConvertCssToBinCheckBox));
        if (myNativeBundleBox != null) {
            myProperties.setNativeBundle(myNativeBundleBox.getValue());
        }

        JavaFxEditCertificatesDialog.Result certificates = myCertificates;
        if (certificates != null) {
            myProperties.setSelfSigning(certificates.selfSigning());
            myProperties.setAlias(certificates.alias());
            myProperties.setKeystore(certificates.keystore());
            myProperties.setKeypass(encode(certificates.keypass()));
            myProperties.setStorepass(encode(certificates.storepass()));
        }
    }

    @Override
    @RequiredUIAccess
    public void reset() {
        if (myTitleField == null) {
            return;
        }

        setText(myTitleField, myProperties.getTitle());
        setText(myVendorField, myProperties.getVendor());
        if (myDescriptionField != null) {
            myDescriptionField.setValue(StringUtil.notNullize(myProperties.getDescription()).trim());
        }
        setText(myWidthField, myProperties.getWidth());
        setText(myHeightField, myProperties.getHeight());
        setText(myAppClassField, myProperties.getAppClass());
        if (myHtmlParamsField != null) {
            myHtmlParamsField.setValue(StringUtil.notNullize(myProperties.getHtmlParamFile()).trim());
        }
        if (myParamsField != null) {
            myParamsField.setValue(StringUtil.notNullize(myProperties.getParamFile()).trim());
        }
        if (myNativeBundleBox != null) {
            myNativeBundleBox.setValue(myProperties.getNativeBundle());
        }
        if (myUpdateInBackgroundCheckBox != null) {
            myUpdateInBackgroundCheckBox.setValue(Objects.equals(myProperties.getUpdateMode(), JavaFxPackagerConstants.UPDATE_MODE_BACKGROUND));
        }
        if (myEnableSigningCheckBox != null) {
            myEnableSigningCheckBox.setValue(myProperties.isEnabledSigning());
        }
        if (myConvertCssToBinCheckBox != null) {
            myConvertCssToBinCheckBox.setValue(myProperties.isConvertCss2Bin());
        }
        if (myEditSignCertificateButton != null) {
            myEditSignCertificateButton.setEnabled(myProperties.isEnabledSigning());
        }
        myCertificates = null;
    }

    @RequiredUIAccess
    private static void setText(@Nullable TextBox field, @Nullable String value) {
        if (field != null) {
            field.setValue(StringUtil.notNullize(value).trim());
        }
    }

    @Override
    @RequiredUIAccess
    public void disposeUIResources() {
        myCertificates = null;
    }
}
