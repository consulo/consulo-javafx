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
package org.jetbrains.plugins.javaFX.fxml.codeInsight;

import com.intellij.java.impl.psi.codeStyle.JavaCodeStyleSettings;
import com.intellij.java.impl.psi.impl.source.codeStyle.ImportHelper;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiMember;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.editor.refactoring.ImportOptimizer;
import consulo.language.file.inject.VirtualFileWindow;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiFileFactory;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.module.content.ProjectRootManager;
import consulo.project.Project;
import consulo.util.lang.EmptyRunnable;
import consulo.util.lang.Pair;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFile;
import consulo.xml.ide.highlighter.XmlFileType;
import consulo.xml.lang.xml.XMLLanguage;
import consulo.xml.psi.XmlRecursiveElementVisitor;
import consulo.xml.psi.xml.*;
import org.jetbrains.plugins.javaFX.fxml.JavaFxFileTypeFactory;
import org.jetbrains.plugins.javaFX.fxml.descriptors.JavaFxClassBackedElementDescriptor;
import org.jetbrains.plugins.javaFX.fxml.descriptors.JavaFxPropertyElementDescriptor;
import org.jetbrains.plugins.javaFX.fxml.descriptors.JavaFxStaticPropertyAttributeDescriptor;

import jakarta.annotation.Nonnull;
import java.util.*;

/**
 * User: anna
 * Date: 2/22/13
 */
@ExtensionImpl
public class JavaFxImportsOptimizer implements ImportOptimizer {
  @Override
  public boolean supports(PsiFile file) {
    return JavaFxFileTypeFactory.isFxml(file);
  }

  @Nonnull
  @Override
  public Runnable processFile(final PsiFile file) {
    VirtualFile vFile = file.getVirtualFile();
    if (vFile instanceof VirtualFileWindow) {
      vFile = ((VirtualFileWindow)vFile).getDelegate();
    }
    final Project project = file.getProject();
    if (vFile == null || !ProjectRootManager.getInstance(project).getFileIndex().isInSourceContent(vFile)) {
      return EmptyRunnable.INSTANCE;
    }
    final List<Pair<String, Boolean>> names = new ArrayList<>();
    collectNamesToImport(names, (XmlFile)file);
    Collections.sort(names, (o1, o2) -> StringUtil.compare(o1.first, o2.first, true));
    final JavaCodeStyleSettings settings = JavaCodeStyleSettings.getInstance(file);
    final List<Pair<String, Boolean>> sortedNames = ImportHelper.sortItemsAccordingToSettings(names, settings);
    final Map<String, Boolean> onDemand = new HashMap<>();
    ImportHelper.collectOnDemandImports(sortedNames, settings, onDemand);
    final Set<String> imported = new HashSet<>();
    final List<String> imports = new ArrayList<>();
    for (Pair<String, Boolean> pair : sortedNames) {
      final String qName = pair.first;
      final String packageName = StringUtil.getPackageName(qName);
      if (imported.contains(packageName) || imported.contains(qName)) {
        continue;
      }
      if (onDemand.containsKey(packageName)) {
        imported.add(packageName);
        imports.add("<?import " + packageName + ".*?>");
      }
      else {
        imported.add(qName);
        imports.add("<?import " + qName + "?>");
      }
    }
    final PsiFileFactory factory = PsiFileFactory.getInstance(file.getProject());

    final XmlFile dummyFile = (XmlFile)factory.createFileFromText("_Dummy_.fxml", XmlFileType.INSTANCE, StringUtil.join(imports, "\n"));
    final XmlDocument document = dummyFile.getDocument();
    final XmlProlog newImportList = document.getProlog();
    if (newImportList == null) {
      return EmptyRunnable.getInstance();
    }
    return new Runnable() {
      @Override
      public void run() {
        final XmlDocument xmlDocument = ((XmlFile)file).getDocument();
        final XmlProlog prolog = xmlDocument.getProlog();
        if (prolog != null) {
          final Collection<XmlProcessingInstruction> instructions = PsiTreeUtil.findChildrenOfType(prolog, XmlProcessingInstruction.class);
          for (final XmlProcessingInstruction instruction : instructions) {
            final ASTNode node = instruction.getNode();
            final ASTNode nameNode = node.findChildByType(XmlTokenType.XML_NAME);
            if (nameNode != null && nameNode.getText().equals("import")) {
              instruction.delete();
            }
          }
          prolog.add(newImportList);
        }
        else {
          document.addBefore(newImportList, document.getRootTag());
        }
      }
    };
  }

  private static void collectNamesToImport(@Nonnull final Collection<Pair<String, Boolean>> names, XmlFile file) {
    file.accept(new JavaFxUsedClassesVisitor() {
      @Override
      protected void appendClassName(String fqn) {
        names.add(Pair.create(fqn, false));
      }
    });
  }

  @Nonnull
  @Override
  public Language getLanguage() {
    return XMLLanguage.INSTANCE;
  }

  public static abstract class JavaFxUsedClassesVisitor extends XmlRecursiveElementVisitor {
    @Override
    public void visitXmlProlog(XmlProlog prolog) {
    }

    @Override
    public void visitXmlProcessingInstruction(XmlProcessingInstruction processingInstruction) {
    }

    @Override
    public void visitXmlAttribute(XmlAttribute attribute) {
      final XmlAttributeDescriptor descriptor = attribute.getDescriptor();
      if (descriptor instanceof JavaFxStaticPropertyAttributeDescriptor) {
        final PsiElement declaration = descriptor.getDeclaration();
        if (declaration instanceof PsiMember) {
          appendClassName((PsiElement)((PsiMember)declaration).getContainingClass());
        }
      }
    }

    @Override
    public void visitXmlTag(XmlTag tag) {
      super.visitXmlTag(tag);
      final XmlElementDescriptor descriptor = tag.getDescriptor();
      if (descriptor instanceof JavaFxClassBackedElementDescriptor) {
        appendClassName(descriptor.getDeclaration());
      }
      else if (descriptor instanceof JavaFxPropertyElementDescriptor && ((JavaFxPropertyElementDescriptor)descriptor).isStatic()) {
        final PsiElement declaration = descriptor.getDeclaration();
        if (declaration instanceof PsiMember) {
          appendClassName((PsiElement)((PsiMember)declaration).getContainingClass());
        }
      }
    }

    private void appendClassName(PsiElement declaration) {
      if (declaration instanceof PsiClass) {
        appendClassName(((PsiClass)declaration).getQualifiedName());
      }
    }

    protected abstract void appendClassName(String fqn);
  }
}
