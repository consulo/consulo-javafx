/**
 * @author VISTALL
 * @since 23/01/2023
 */
module org.jetbrains.plugins.javaFX {
  requires transitive org.jetbrains.plugins.javaFX.api;

  requires consulo.application.api;
  requires consulo.application.content.api;
  requires consulo.code.editor.api;
  requires consulo.compiler.api;
  requires consulo.compiler.artifact.api;
  requires consulo.container.api;
  requires consulo.document.api;
  requires consulo.file.chooser.api;
  requires consulo.ide.api;
  requires consulo.index.io;
  requires consulo.language.api;
  requires consulo.language.code.style.api;
  requires consulo.language.editor.api;
  requires consulo.language.editor.refactoring.api;
  requires consulo.language.editor.ui.api;
  requires consulo.localize.api;
  requires consulo.logging.api;
  requires consulo.module.api;
  requires consulo.module.content.api;
  requires consulo.module.ui.api;
  requires consulo.process.api;
  requires consulo.project.api;
  requires consulo.project.content.api;
  requires consulo.ui.api;
  requires consulo.ui.ex.api;
  requires consulo.ui.ex.awt.api;
  requires consulo.util.collection;
  requires consulo.util.dataholder;
  requires consulo.util.io;
  requires consulo.util.lang;
  requires consulo.util.xml.fast.reader;
  requires consulo.util.xml.serializer;
  requires consulo.virtual.file.system.api;

  requires com.intellij.xml.api;
  requires com.intellij.xml.editor.api;
  requires com.intellij.xml;

  requires consulo.java;
  requires consulo.java.analysis.api;
  requires consulo.java.language.api;
  requires consulo.java.execution.impl;
  requires consulo.java.compiler.artifact.impl;

  requires java.scripting;

  // TODO remove in future
  requires java.desktop;
  requires forms.rt;

  opens org.jetbrains.plugins.javaFX.fxml to consulo.application.impl;
  opens org.jetbrains.plugins.javaFX.packaging to consulo.util.xml.serializer;
}
