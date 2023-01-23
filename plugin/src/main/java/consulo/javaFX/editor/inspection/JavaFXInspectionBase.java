package consulo.javaFX.editor.inspection;

import consulo.language.Language;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.xml.codeInspection.XmlSuppressableInspectionTool;
import consulo.xml.lang.xml.XMLLanguage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 23/01/2023
 */
public abstract class JavaFXInspectionBase extends XmlSuppressableInspectionTool {
  @Override
  public boolean isEnabledByDefault() {
    return true;
  }

  @Nonnull
  @Override
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.WARNING;
  }

  @Nullable
  @Override
  public Language getLanguage() {
    return XMLLanguage.INSTANCE;
  }

  @Nonnull
  @Override
  public String getGroupDisplayName() {
    return "JavaFX";
  }
}
