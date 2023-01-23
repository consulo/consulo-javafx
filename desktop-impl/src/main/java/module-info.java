/**
 * @author VISTALL
 * @since 23/01/2023
 */
module org.jetbrains.plugins.javaFX.desktop.impl {
  requires consulo.ide.api;
  requires org.jetbrains.plugins.javaFX.api;

  requires consulo.java.execution.api;

  opens consulo.javaFX.desktop to consulo.util.xml.serializer;
}