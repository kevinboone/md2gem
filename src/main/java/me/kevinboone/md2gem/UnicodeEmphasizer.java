/*============================================================================

  md2gem

  UnicodeEmphasizer

  This is not used at present, owing to the difficulty of finding 
    Unicode code points that work nicely on all Gemini clients :/ 

  Copyright (c)2026 Kevin Boone, GPL3

============================================================================*/
package me.kevinboone.md2gem;

public class UnicodeEmphasizer
  {
  public static final int ITALIC = 0; 
  public static final int BOLD = 1; 
  public static final int BOLDITALIC = 2; 

  static final int ITALIC_UC = 0x1D608;
  static final int ITALIC_LC = 0x1D622;
  static final int ITALIC_NUM = 0x1D7EC;

  static final int BOLDITALIC_UC = 0x1D63C;
  static final int BOLDITALIC_LC = 0x1D656;
  static final int BOLDITALIC_NUM = 0x1D7EC;

  static final int BOLD_UC = 0x1D5D4;
  static final int BOLD_LC = 0x1D5EE;
  static final int BOLD_NUM = 0x1D7EC;

  private int offset_uc = ITALIC_UC;
  private int offset_lc = ITALIC_LC;
  private int offset_num = ITALIC_NUM;
  
  private int mode;

  UnicodeEmphasizer (int mode)
    {
    this.mode = mode;
    switch (mode)
      {
      case BOLDITALIC:
	offset_uc = BOLDITALIC_UC;
	offset_lc = BOLDITALIC_LC;
	offset_num = BOLDITALIC_NUM;
        break;
      case BOLD:
	offset_uc = BOLD_UC;
	offset_lc = BOLD_LC;
	offset_num = BOLD_NUM;
        break;
      default:
	offset_uc = ITALIC_UC;
	offset_lc = ITALIC_LC;
	offset_num = ITALIC_NUM;
        break;
      }
    }

  public String emphasize (String s)
    {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < s.length(); i++)
      {
      char c = s.charAt(i);
      if (c >= 'A' && c <= 'Z')
        {
        char[] newChar = Character.toChars (offset_uc + (c - 'A'));
        sb.append (newChar);
        }
      else if (c >= 'a' && c <= 'z')
        {
        char[] newChar = Character.toChars (offset_lc + (c - 'a'));
        sb.append (newChar);
        }
      else if (c >= '0' && c <= '9')
        {
        char[] newChar = Character.toChars (offset_num + (c - '0'));
        sb.append (newChar);
        }
      else
        sb.append (c);
      }
    return new String (sb);
    }
  }

