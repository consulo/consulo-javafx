package org.jetbrains.plugins.javaFX.fxml.refs;

import com.intellij.xml.XmlElementDescriptor;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.completion.*;
import consulo.language.editor.completion.lookup.InsertionContext;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.psi.PsiReference;
import consulo.language.util.ProcessingContext;
import consulo.xml.codeInsight.completion.XmlTagInsertHandler;
import consulo.xml.lang.xml.XMLLanguage;
import consulo.xml.patterns.XmlPatterns;
import consulo.xml.psi.impl.source.xml.TagNameVariantCollector;
import consulo.xml.psi.xml.XmlFile;
import consulo.xml.psi.xml.XmlTag;
import org.jetbrains.plugins.javaFX.fxml.JavaFxPsiUtil;
import org.jetbrains.plugins.javaFX.fxml.descriptors.JavaFxClassBackedElementDescriptor;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

import static consulo.language.pattern.PlatformPatterns.psiElement;

/**
 * @author yole
 */
@ExtensionImpl(order = "before xmlNonFirst")
public class JavaFxCompletionContributor extends CompletionContributor {
  public JavaFxCompletionContributor() {
    extend(CompletionType.BASIC, psiElement().inside(XmlPatterns.xmlTag()), new JavaFxTagCompletionContributor());
  }

  @Nonnull
  @Override
  public Language getLanguage() {
    return XMLLanguage.INSTANCE;
  }

  private static class JavaFxTagCompletionContributor implements CompletionProvider
  {
    @Override
    public void addCompletions(@Nonnull CompletionParameters parameters,
                                  ProcessingContext context,
                                  @Nonnull CompletionResultSet result) {
      PsiReference reference = parameters.getPosition().getContainingFile().findReferenceAt(parameters.getOffset());
      if (reference instanceof JavaFxTagNameReference) {
        addJavaFxTagVariants((JavaFxTagNameReference)reference, result);
        result.stopHere();
      }
    }

    private static void addJavaFxTagVariants(JavaFxTagNameReference reference, CompletionResultSet result) {
      final XmlTag xmlTag = (XmlTag)reference.getElement();

      List<String> namespaces = Arrays.asList(xmlTag.knownNamespaces());
      final List<XmlElementDescriptor> variants = TagNameVariantCollector.getTagDescriptors(xmlTag, namespaces, null);
      for (XmlElementDescriptor descriptor : variants) {
        final String descriptorName = descriptor.getName(reference.getElement());
        if (descriptorName != null) {
          LookupElementBuilder lookupElement = LookupElementBuilder.create(descriptor, descriptorName);
          result.addElement(lookupElement.withInsertHandler(JavaFxTagInsertHandler.INSTANCE));
        }
      }
    }
  }

  private static class JavaFxTagInsertHandler extends XmlTagInsertHandler {
    public static final JavaFxTagInsertHandler INSTANCE = new JavaFxTagInsertHandler();

    @Override
    public void handleInsert(InsertionContext context, LookupElement item) {
      super.handleInsert(context, item);
      final Object object = item.getObject();
      if (object instanceof JavaFxClassBackedElementDescriptor) {
        final XmlFile xmlFile = (XmlFile)context.getFile();
        final String shortName = ((JavaFxClassBackedElementDescriptor)object).getName();
        context.commitDocument();
        JavaFxPsiUtil.insertImportWhenNeeded(xmlFile, shortName, ((JavaFxClassBackedElementDescriptor)object).getQualifiedName());
      }
    }
  }
}
