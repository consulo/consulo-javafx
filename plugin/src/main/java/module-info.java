/**
 * @author VISTALL
 * @since 23/01/2023
 */
module org.jetbrains.plugins.javaFX {
  requires transitive org.jetbrains.plugins.javaFX.api;
  
  requires consulo.ide.api;
  requires com.intellij.xml;

  requires consulo.java;
  requires consulo.java.execution.impl;
  requires consulo.java.compiler.artifact.impl;

  requires java.scripting;

  requires consulo.util.xml.fast.reader;

  // TODO remove in future
  requires java.desktop;
  requires forms.rt;

  opens org.jetbrains.plugins.javaFX.fxml to consulo.application.impl;
  opens org.jetbrains.plugins.javaFX.packaging to consulo.util.xml.serializer;
}