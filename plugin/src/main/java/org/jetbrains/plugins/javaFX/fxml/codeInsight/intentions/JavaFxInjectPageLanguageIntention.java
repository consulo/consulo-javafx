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
package org.jetbrains.plugins.javaFX.fxml.codeInsight.intentions;

import consulo.codeEditor.Editor;
import consulo.codeEditor.EditorPopupHelper;
import consulo.language.editor.FileModificationService;
import consulo.language.editor.WriteCommandAction;
import consulo.language.editor.intention.PsiElementBaseIntentionAction;
import consulo.language.editor.intention.SyntheticIntentionAction;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFileFactory;
import consulo.language.psi.PsiParserFacade;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.IncorrectOperationException;
import consulo.logging.Logger;
import consulo.module.content.layer.OrderEnumerator;
import consulo.project.Project;
import consulo.ui.ex.popup.IPopupChooserBuilder;
import consulo.ui.ex.popup.JBPopup;
import consulo.ui.ex.popup.JBPopupFactory;
import consulo.util.io.FileUtil;
import consulo.xml.ide.highlighter.XmlFileType;
import consulo.xml.psi.xml.XmlDocument;
import consulo.xml.psi.xml.XmlFile;
import consulo.xml.psi.xml.XmlProcessingInstruction;
import consulo.xml.psi.xml.XmlProlog;

import javax.annotation.Nonnull;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
* User: anna
* Date: 4/8/13
*/
public class JavaFxInjectPageLanguageIntention extends PsiElementBaseIntentionAction implements SyntheticIntentionAction {
  public static final Logger LOG = Logger.getInstance(JavaFxInjectPageLanguageIntention.class);

  public JavaFxInjectPageLanguageIntention() {
    setText("Specify page language");
  }

  private static Set<String> getAvailableLanguages(Project project) {
    final List<ScriptEngineFactory> engineFactories = new ScriptEngineManager(composeUserClassLoader(project)).getEngineFactories();

    if (engineFactories != null) {
      final Set<String> availableNames = new TreeSet<String>();
      for (ScriptEngineFactory factory : engineFactories) {
        final String engineName = (String)factory.getParameter(ScriptEngine.NAME);
        availableNames.add(engineName);
      }
      return availableNames;
    }

    return null;
  }

  private static URLClassLoader composeUserClassLoader(Project project) {
    final List<URL> urls = new ArrayList<URL>();
    final List<String> list = OrderEnumerator.orderEntries(project).recursively().runtimeOnly().getPathsList().getPathList();
    for (String path : list) {
      try {
        urls.add(new File(FileUtil.toSystemIndependentName(path)).toURI().toURL());
      }
      catch (MalformedURLException e1) {
        LOG.info(e1);
      }
    }

    return new URLClassLoader(urls.toArray(URL[]::new));
  }

  @Override
  public void invoke(@Nonnull final Project project, Editor editor, @Nonnull PsiElement element) throws IncorrectOperationException
  {
    if (!FileModificationService.getInstance().preparePsiElementsForWrite(element)) return;
    final XmlFile containingFile = (XmlFile)element.getContainingFile();

    final Set<String> availableLanguages = getAvailableLanguages(project);
    if (availableLanguages.size() == 1) {
      registerPageLanguage(project, containingFile, availableLanguages.iterator().next());
    } else {
      IPopupChooserBuilder<String> builder = JBPopupFactory.getInstance().createPopupChooserBuilder(List.copyOf(availableLanguages));
      builder.setItemChosenCallback(s -> registerPageLanguage(project, containingFile, (String)s));
      JBPopup popup = builder.createPopup();

      EditorPopupHelper.getInstance().showPopupInBestPositionFor(editor, popup);
    }
  }

  private void registerPageLanguage(final Project project, final XmlFile containingFile, final String languageName) {
    new WriteCommandAction.Simple(project, getText()) {
      @Override
      protected void run() {
        final PsiFileFactory factory = PsiFileFactory.getInstance(project);
        final XmlFile dummyFile = (XmlFile)factory.createFileFromText("_Dummy_.fxml", XmlFileType.INSTANCE,
                                                                      "<?language " + languageName + "?>");
        final XmlDocument document = dummyFile.getDocument();
        if (document != null) {
          final XmlProlog prolog = document.getProlog();
          final Collection<XmlProcessingInstruction> instructions = PsiTreeUtil.findChildrenOfType(prolog, XmlProcessingInstruction.class);
          LOG.assertTrue(instructions.size() == 1);
          final XmlDocument xmlDocument = containingFile.getDocument();
          if (xmlDocument != null) {
            final XmlProlog xmlProlog = xmlDocument.getProlog();
            if (xmlProlog != null) {
              final PsiElement element = xmlProlog.addBefore(instructions.iterator().next(), xmlProlog.getFirstChild());
              xmlProlog.addAfter(PsiParserFacade.getInstance(project).createWhiteSpaceFromText("\n\n"), element);
            }
          }
        }
      }
    }.execute();
  }

  @Override
  public boolean isAvailable(@Nonnull Project project, Editor editor, @Nonnull PsiElement element) {
    return element.isValid();
  }

  @Override
  public boolean startInWriteAction() {
    return false;
  }
}
