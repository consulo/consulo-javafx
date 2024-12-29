package org.jetbrains.plugins.javaFX.fxml;

import com.intellij.java.indexing.search.searches.ClassInheritorsSearch;
import com.intellij.java.language.psi.JavaPsiFacade;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlNSDescriptor;
import consulo.application.util.function.Processor;
import consulo.language.psi.PsiElement;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.project.Project;
import consulo.util.collection.ArrayUtil;
import consulo.xml.Validator;
import consulo.xml.psi.xml.XmlDocument;
import consulo.xml.psi.xml.XmlFile;
import consulo.xml.psi.xml.XmlTag;
import org.jetbrains.plugins.javaFX.fxml.descriptors.JavaFxClassBackedElementDescriptor;
import org.jetbrains.plugins.javaFX.fxml.descriptors.JavaFxDefaultPropertyElementDescriptor;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.ArrayList;

/**
* User: anna
* Date: 1/9/13
*/
public class JavaFXNSDescriptor implements XmlNSDescriptor, Validator<XmlDocument> {
  private XmlFile myFile;

  @Nullable
  @Override
  public XmlElementDescriptor getElementDescriptor(@Nonnull XmlTag tag) {
    final String name = tag.getName();

    if (tag.getName().equals(FxmlConstants.FX_ROOT)) {
      return new JavaFxDefaultPropertyElementDescriptor(name, tag);
    }
    final XmlTag parentTag = tag.getParentTag();
    if (parentTag != null) {
      final XmlElementDescriptor descriptor = parentTag.getDescriptor();
      if (descriptor != null) {
        return descriptor.getElementDescriptor(tag, parentTag);
      }
    }
    return new JavaFxClassBackedElementDescriptor(name, tag);
  }

  @Nonnull
  @Override
  public XmlElementDescriptor[] getRootElementsDescriptors(@Nullable XmlDocument document) {
    if (document != null) {
      final Project project = document.getProject();
      final PsiClass paneClass = JavaPsiFacade.getInstance(project).findClass(JavaFxCommonClassNames.JAVAFX_SCENE_LAYOUT_PANE, GlobalSearchScope.allScope(project));
      if (paneClass != null) {
        final ArrayList<XmlElementDescriptor> result = new ArrayList<XmlElementDescriptor>();
        ClassInheritorsSearch.search(paneClass).forEach(new Processor<PsiClass>() {
          @Override
          public boolean process(PsiClass psiClass) {
            result.add(new JavaFxClassBackedElementDescriptor(psiClass.getName(), psiClass));
            return true;
          }
        });
        return result.toArray(new XmlElementDescriptor[result.size()]);
      }
    }
    return new XmlElementDescriptor[0];
  }

  @Nullable
   public XmlFile getDescriptorFile() {
     return myFile;
   }
 
   public boolean isHierarhyEnabled() {
     return false;
   }
 
   public PsiElement getDeclaration() {
     return myFile;
   }

  @Override
  public String getName(PsiElement context) {
    return null;
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public void init(PsiElement element) {
    XmlDocument document = (XmlDocument) element;
    myFile = ((XmlFile)document.getContainingFile());
  }

  @Override
  public Object[] getDependences() {
    return ArrayUtil.EMPTY_OBJECT_ARRAY;
  }

  @Override
  public void validate(@Nonnull XmlDocument context, @Nonnull ValidationHost host) {}
}
