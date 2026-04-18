/**
 * @author VISTALL
 * @since 23/01/2023
 */
module org.jetbrains.plugins.javaFX.desktop.impl {
  requires consulo.application.api;
  requires consulo.component.api;
  requires consulo.configurable.api;
  requires consulo.disposer.api;
  requires consulo.file.chooser.api;
  requires consulo.ide.api;
  requires consulo.language.api;
  requires consulo.language.editor.api;
  requires consulo.localize.api;
  requires consulo.logging.api;
  requires consulo.module.api;
  requires consulo.process.api;
  requires consulo.project.api;
  requires consulo.ui.api;
  requires consulo.ui.ex.api;
  requires consulo.ui.ex.awt.api;
  requires consulo.util.collection;
  requires consulo.util.io;
  requires consulo.util.lang;
  requires consulo.util.xml.serializer;
  requires consulo.virtual.file.system.api;

  requires org.jetbrains.plugins.javaFX.api;
  requires consulo.java.execution.api;

  opens consulo.javaFX.desktop to consulo.util.xml.serializer;
}
