/*============================================================================

  md2gem

  Config 

  Copyright (c)2026 Kevin Boone, GPL3

============================================================================*/
package me.kevinboone.md2gem;

/** A handy repository for settings made using command-line
    switches.
*/
public class Config
  {
  private static boolean unicodeLineDrawing = true;
  private static boolean retainEmphasis = true;
  private static boolean extraSpacing = true;

  public boolean getExtraSpacing () { return extraSpacing; }
  public boolean getRetainEmphasis () { return retainEmphasis; }
  public boolean getUnicodeLineDrawing() { return unicodeLineDrawing; }
  public void setExtraSpacing (boolean f) { extraSpacing = f; }
  public void setRetainEmphasis (boolean f) { retainEmphasis = f; }
  public void setUnicodeLineDrawing (boolean f) { unicodeLineDrawing = f; }
  }

