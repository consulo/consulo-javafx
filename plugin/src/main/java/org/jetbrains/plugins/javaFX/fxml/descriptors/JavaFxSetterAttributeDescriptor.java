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
package org.jetbrains.plugins.javaFX.fxml.descriptors;

import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.xml.XmlAttributeDescriptor;
import consulo.language.psi.PsiElement;
import consulo.util.collection.ArrayUtil;
import consulo.util.lang.StringUtil;
import consulo.xml.psi.xml.XmlElement;

import jakarta.annotation.Nullable;

/**
 * User: anna
 * Date: 1/10/13
 */
public class JavaFxSetterAttributeDescriptor implements XmlAttributeDescriptor {
  private final String myName;
  private final PsiClass myPsiClass;
  private final PsiMethod myPsiMethod;

  public JavaFxSetterAttributeDescriptor(PsiMethod psiMethod, PsiClass psiClass) {
    myPsiMethod = psiMethod;
    myName = psiMethod.getName();
    myPsiClass = psiClass;
  }

  @Override
  public boolean isRequired() {
    return false;
  }

  @Override
  public boolean isFixed() {
    return false;
  }

  @Override
  public boolean hasIdType() {
    return false;
  }

  @Override
  public boolean hasIdRefType() {
    return false;
  }

  @Nullable
  @Override
  public String getDefaultValue() {
    return null;
  }

  @Override
  public boolean isEnumerated() {
    return false;
  }

  @Nullable
  @Override
  public String[] getEnumeratedValues() {
    return null;
  }

  @Nullable
  @Override
  public String validateValue(XmlElement context, String value) {
    return null;
  }

  @Override
  public PsiElement getDeclaration() {
    return myPsiMethod != null && myPsiMethod.isValid() ? myPsiMethod : null;
  }

  @Override
  public String getName(PsiElement context) {
    return getName();
  }

  @Override
  public String getName() {
    return myPsiClass.getName() + "." + StringUtil.decapitalize(myName.substring("set".length()));
  }

  @Override
  public void init(PsiElement element) {}

  @Override
  public Object[] getDependences() {
    return ArrayUtil.EMPTY_OBJECT_ARRAY;
  }
}
