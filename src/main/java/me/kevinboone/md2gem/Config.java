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
  public static final int EMPH_STRIP = 0;
  public static final int EMPH_RETAIN = 1;
  public static final int EMPH_UNICODE = 2; // Not yet used

  private static boolean unicodeLineDrawing = true;
  private static boolean extraSpacing = true;
  private static int emphMode = EMPH_UNICODE; // TODO
  //private static int emphMode = EMPH_RETAIN; // TODO

  public int getEmphMode() { return emphMode; }
  public boolean getExtraSpacing () { return extraSpacing; }
  public boolean getUnicodeLineDrawing() { return unicodeLineDrawing; }
  public void setEmphMode (int n) { emphMode = n; }
  public void setExtraSpacing (boolean f) { extraSpacing = f; }
  public void setUnicodeLineDrawing (boolean f) { unicodeLineDrawing = f; }
  }

