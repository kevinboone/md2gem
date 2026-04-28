/*============================================================================

  md2gem

  GemtextRenderer 

  Copyright (c)2026 Kevin Boone, GPL3

============================================================================*/
package me.kevinboone.md2gem;

import org.commonmark.Extension;
import org.commonmark.node.*;
import org.commonmark.parser.*;
import org.commonmark.renderer.*;
import org.commonmark.renderer.text.*;
import org.commonmark.ext.gfm.tables.*;
import java.io.*;
import java.util.*;

/** Gemtext renderer takes a Document node from the commonmark-java
    parser, and expands it recursively, generating Gemtext as it 
    does. I though originally that I could use the text renderer in
    commonmark-java with a few modifications, but I couldn't really
    figure out how to use it. So I fear I may have re-implemented it.
*/ 
class GemtextRenderer implements Renderer 
  {
  private String para = ""; // The paragraph we are working on
  /** The prefix stack holds prefixes (like "*") that apply 
      for the life of the current paragraph. */
  private Stack<String> prefixes = new Stack<String>();
  private Config config;
  private static final int MAX_COLS = 50; // Need to specify _something_ 
  private boolean spaceTables = false; 
  private boolean spaceQuoted = false;
  private boolean spacePreformat = false;
  private boolean spaceParas = false;
  private boolean unicodeLineDrawing = true;
  private String tableColSep; // Column separator 
  private String linkPreamble; // Write this before link text in the para
  private String linkPostamble; // ...and this after 

  /** Constructor.
  */
  GemtextRenderer (Config config)
    {
    this.config = config;
    unicodeLineDrawing = config.getUnicodeLineDrawing();
    if (unicodeLineDrawing)
      tableColSep = "│"; 
    else
      tableColSep = "|"; 
    linkPreamble = "[";
    linkPostamble = "]";
    if (config.getExtraSpacing())
      {
      spaceTables = false; 
      spaceQuoted = false;
      spacePreformat = false;
      spaceParas = true;
      }
    else
      {
      spaceTables = false; 
      spaceQuoted = false;
      spacePreformat = false;
      spaceParas = false;
      }
    }

  /** Add some text to the current paragraph. In practice, we just add
      the input String to the existing paragraph.
  */
  private void addToPara (String s)
    {
    para = concatenate (para, s);
    }

  /** There's no need for this method -- it's here in case I one day 
      need to do something more complicated the "+" to concatenate
      text elements. 
  */
  private String concatenate (String s1, String s2)
    {
    return s1 + s2;
    }

  /** Recursively extract all the text from the specified node.
  */
  private String extractText (Node node) 
    {
    String ret = "";
    if (node == null) return ret;
    Node n = node.getFirstChild();
    while (n != null) 
      {
      if (n instanceof Text)
        {
        Text nt = (Text)n;
        String text = nt.getLiteral();
        ret = concatenate (ret, text);
        }
      if (n instanceof Link)
        {
        String text = linkPreamble + extractText (n) + linkPostamble;
        ret = concatenate (ret, text);
        }
      else
        {
        String text = extractText (n);
        ret = concatenate (ret, text);
        }
      Node next = n.getNext();
      n = next;
      }
    return ret;
    }


  /** Add the current paragraph to the output buffer, then clear the
      paragraph buffer for the next paragraph. 
  */
  private void flushPara (Appendable a, boolean space)
    {
    write (para, a);
    newline (a);
    if (space)
      newline (a);
    para = "";
    }
  
  /** Given a TableHead or TableBody node, enumerate the rows contained, 
      and fill in the lengths[] array with the longest text in each column. 
  */
  private int getRowCellLengths (Node node, int lengths[]) // TableHead or TableBody
    {
    int row = 0;
    int col = 0;
    int maxCols = 0;
    if (node instanceof TableHead || node instanceof TableBody)
      {
      Node n = node.getFirstChild();
      while (n != null) 
	{
	if (n instanceof TableRow)
	  { 
          Node n2 = n.getFirstChild();
	  while (n2 != null && col < MAX_COLS) 
	    {
            if (n2 instanceof TableCell)
              {
              Node n3 = n2.getFirstChild();
              if (n3 instanceof Text)
                {
                int len = ((Text)n3).getLiteral().length();
                if (len > lengths[col]) lengths[col] = len;
                } 
              else
                {
                String text = extractText (n2);
                int len = text.length(); 
                if (len > lengths[col]) lengths[col] = len;
                }
              }
	    Node next = n2.getNext();
	    n2 = next;
            col++;
            if (col > maxCols) maxCols = col;
            }
	  } 
        else
          {
          // Should never happen
          }
	Node next = n.getNext();
	n = next;
        row++;
        col = 0;
	}
      }
    return maxCols;
    }

  /** Given a TableBlock node, enumerate the headers and bodies (usually 0-1 of
     each) and return the maximum lengths of each column. This method will fail
     in an unhelpful way if the rows do not contain the same number of cells. 
  */
  private int[] getTableCellLengths (TableBlock node)
    {
    int lengths[] = new int[MAX_COLS];
    for (int i = 0; i < MAX_COLS; i++) lengths[i] = 0;
    Node n = node.getFirstChild();
    int cols = 0;
    while (n != null) 
      {
      if (n instanceof TableHead)
	{ 
	// TODO
	cols = getRowCellLengths (n, lengths);
	} 
      else if (n instanceof TableBody)
	{
	int cols2 = getRowCellLengths (n, lengths);
        if (cols2 > cols) cols = cols2;
	}

      Node next = n.getNext();
      n = next;
      }
    int[] lengths2 = new int[cols];
    for (int i = 0; i < cols; i++) lengths2[i] = lengths[i];
    return lengths2;
    }

  /** Enumerate the prefixStack, putting any prefixes into a single
      line of text. I'm not sure we'll ever end up in situation where
      there are multiple prefixes, because this would imply nested elements,
      and these will never translate well to Gemtext. */
  private String makePrefixes()
    {
    String ret = "";
    for (String prefix : prefixes)
      ret += prefix;
    return ret;
    }

  private void newline (Appendable a)
    {
    write ("\n", a);
    }

  /** Pad out a string to length with spaces. 
  */
  private String pad (String in, int len)
    {
    int l = in.length();
    int pad = (len - l);
    if (pad <= 0) return in;
    StringBuffer s = new StringBuffer (in);
    for (int i = 0; i < pad; i++)
      s.append (" ");
    return new String (s);
    }

  /** Pad out a string to length with a specified character. 
  */
  private String pad (String in, int len, char c)
    {
    int l = in.length();
    int pad = (len - l);
    if (pad <= 0) return in;
    StringBuffer s = new StringBuffer (in);
    for (int i = 0; i < pad; i++)
      s.append (c);
    return new String (s);
    }

  /** Remove the most recently-added prefix (which almost certainly means
      emptying the prefix stack.
  */
  private void popPrefix()
    {
    prefixes.pop();
    }

  /** Add a prefix (like "*" or ">") so that it will be added to all lines
      in the current paragraph.
  */
  private void pushPrefix (String prefix)
    {
    prefixes.push (prefix);
    }

  @Override
  public String render (Node node)
    {
    StringBuffer sb = new StringBuffer();
    render (node, sb);
    return new String (sb);
    }

  /** Start rendering here.
  */
  @Override
  public void render (Node node, Appendable a)
    {
    if (node instanceof BlockQuote)
      renderBlockQuote ((BlockQuote)node, a);
    else if (node instanceof BulletList)
      renderBulletList ((BulletList)node, a);
    else if (node instanceof Code)
      renderCode ((Code)node, a);
    else if (node instanceof Document)
      renderChildren (node, a);
    else if (node instanceof Emphasis)
      renderEmphasis (node, (Delimited)node, a);
    else if (node instanceof FencedCodeBlock)
      renderFencedCodeBlock ((FencedCodeBlock) node, a);
    else if (node instanceof HardLineBreak)
      renderHardLineBreak ((HardLineBreak) node, a);
    else if (node instanceof Heading)
      renderHeading ((Heading) node, a);
    else if (node instanceof Image)
      renderImage ((Image) node, a);
    else if (node instanceof IndentedCodeBlock)
      renderIndentedCodeBlock ((IndentedCodeBlock) node, a);
    else if (node instanceof Link)
      renderLink ((Link)node, a);
    else if (node instanceof ListItem)
      renderChildren (node, a);
    else if (node instanceof OrderedList)
      renderOrderedList ((OrderedList) node, a);
    else if (node instanceof Paragraph)
      renderParagraph ((Paragraph) node, a);
    else if (node instanceof SoftLineBreak)
      { addToPara (" "); } 
    else if (node instanceof StrongEmphasis)
      renderEmphasis (node, (Delimited)node, a);
    else if (node instanceof TableBlock)
      renderTableBlock ((TableBlock) node, a);
    else if (node instanceof Text)
      renderText ((Text) node, a);
    else if (node instanceof ThematicBreak)
      { /* ignore */ } 
    else
      System.err.println ("Unknown node: " + node);
    }

  private void renderBlockQuote (BlockQuote node, Appendable a)
    {
    if (spaceQuoted)
      newline (a); 
    pushPrefix ("> ");
    renderChildren (node, a);
    popPrefix();
    if (spaceQuoted)
      newline (a); 
    }

  private void renderBulletList (BulletList node, Appendable a)
    {
    pushPrefix ("* ");
    renderChildren (node, a);
    popPrefix();
    }
 
  /** Recursively descend the node tree, rendering as we go. 
  */
  private void renderChildren (Node parent, Appendable a)
    {
    Node node = parent.getFirstChild();
    while (node != null) 
      {
      Node next = node.getNext();
      render (node, a);
      node = next;
      }
    }

  /** Handle bold, italic, etc
  */
  private void renderEmphasis (Node node, Delimited d, Appendable a)
    {
    int emphMode = config.getEmphMode();
    if (emphMode == Config.EMPH_STRIP)
      {
      String s = extractText (node);
      addToPara (s);
      }
    else if (emphMode == Config.EMPH_RETAIN)
      {
      String s = extractText (node);
      addToPara (d.getOpeningDelimiter());
      addToPara (s);
      addToPara (d.getClosingDelimiter());
      }
    else
      {
      /* NOT YET IMPLEMENTED */
      /*
      String s = d.getOpeningDelimiter();
      UnicodeEmphasizer ue;
      if ("**".equals (s))
        ue = new UnicodeEmphasizer (UnicodeEmphasizer.BOLD);
      else if ("___".equals (s))
        ue = new UnicodeEmphasizer (UnicodeEmphasizer.BOLDITALIC);
      else  
        ue = new UnicodeEmphasizer (UnicodeEmphasizer.ITALIC);
      String text = extractText (node);
      String ss = ue.emphasize (text);
      addToPara (ss);
      */
      }
    }

  /** Handle `code` elements inline. I don't really understand why 
      commonmark-java doesn't just treat these the same as emphasis.
      But it doesn't.
  */
  private void renderCode (Code node, Appendable a)
    {
    if (config.getEmphMode() == Config.EMPH_RETAIN)
      addToPara ("`"); 
    addToPara (node.getLiteral()); 
    if (config.getEmphMode() == Config.EMPH_RETAIN)
      addToPara ("`");
    }

  /** Handle pre-formatted blocks that are introduced by a specific
      marker. */
  private void renderFencedCodeBlock (FencedCodeBlock node, Appendable a)
    {
    if (spacePreformat)
      newline (a); 
    writeln ("```", a);
    write (node.getLiteral(), a);
    writeln ("```", a);
    if (spacePreformat)
      newline (a); 
    }

  /** In a hard break, just flush the current paragraph and put
      a newline on the end. A hard break _can_ appear in a block
      like preformatted text or a list. */
  private void renderHardLineBreak (HardLineBreak node, Appendable a)
    {
    flushPara (a, false);
    }

  /** Handle "#" headings, which is easy, because they're the same in
      Gemtext as Markdown. */
  private void renderHeading (Heading node, Appendable a)
    {
    String s = "";
    for (int i = 0; i < node.getLevel(); i++)
       s += "#";
    s += " ";
    pushPrefix (s);
    renderChildren (node, a);
    flushPara (a, false);
    popPrefix();
    }

  /** Render a markdown image, adding the caption/alt-text as appropriate.
  */
  private void renderImage (Image node, Appendable a)
    {
    String title = node.getTitle();
    addToPara ("=> ");
    addToPara (node.getDestination());
    addToPara (" ");
    if (title == null)
      {
      Node n2 = node.getFirstChild();
      if (n2 instanceof Text)
	{
	Text n2t = (Text)n2;
	String s = n2t.getLiteral();
	if (s == null || s.length() == 0)
	  addToPara (node.getDestination());
	else
	  addToPara (s);
	}
      else
        {
        addToPara (node.getDestination());
        }
      }
    else
      {
      addToPara (title);
      }
    }

  /** Handle pre-formatted blocks that are signalled just by indentation.
  */
  private void renderIndentedCodeBlock (IndentedCodeBlock node, Appendable a)
    {
    if (spacePreformat)
      newline (a); 
    writeln ("```", a);
    write (node.getLiteral(), a);
    writeln ("```", a);
    if (spacePreformat)
      newline (a); 
    }

  /** Handle a hyperlink. This is a bit ugly because, in theory, the link
      text _might_ be an image, not text at all. 
  */
  private void renderLink (Link node, Appendable a)
    {
    boolean isText = false;
    Node n = node.getFirstChild();
    if (n instanceof Text) isText = true;
    if (isText)
      addToPara (linkPreamble);
    renderChildren (node, a);
    if (isText)
      addToPara (linkPostamble);
    }

  /** I'm not really sure how to handle this in Gemtext. It doesn't
      have a specific numbered list format, but it doesn't really
      need one; we'll just write our own numbers. 
  */
  private void renderOrderedList (OrderedList node, Appendable a)
    {
    Node n = node.getFirstChild();
    int start = node.getStartNumber();
    int num = start;
    while (n != null) 
      {
      Node next = n.getNext();
      write ("" + num + ". ", a);
      renderChildren (n, a);
      n = next;
      num++;
      }
    }

  /** This is where most of the actual output happens. We just render
      everything below the Paragraph node, and then flush the
      changes to the output. */
  private void renderParagraph (Paragraph node, Appendable a)
    {
    renderChildren (node, a);
    boolean space = (spaceParas) && (node.getParent() instanceof Document);
    Node n = node.getLastChild();
    if (n instanceof HardLineBreak)
      flushPara (a, false);
    else
      flushPara (a, space);
    }

  /** This is the top level of the table hierarchy. Below this we
      have TableHead and TableBody each of which is made of
      TableRow nodes. 
  */
  private void renderTableBlock (TableBlock node, Appendable a)
    {
    int lengths[] = getTableCellLengths (node);
    renderTableBlock ((TableBlock)node, lengths, a);
    }

  /** We need to know the lengths of each column, which we'll have got
      through an earlier call to getTableCellLengths. */
  private void renderTableBlock (TableBlock node, int[] lengths, Appendable a)
    {
    writeln ("```", a);
    if (spaceTables)
      newline (a); 
    StringBuffer s = new StringBuffer();
    if (unicodeLineDrawing)
      {
      s.append ("┌");
      for (int i = 0; i < lengths.length; i++)
        {
        s.append (pad ("", lengths[i] + 2, '─'));
        if (i < lengths.length - 1)
          s.append ("┬");
        }
      s.append ("┐");
      }
    else 
      {
      s.append ("+");
      for (int i = 0; i < lengths.length; i++)
        {
        s.append (pad ("", lengths[i] + 2, '-'));
        if (i < lengths.length - 1)
          s.append ("+");
        }
      s.append ("+");
      }
    writeln (new String (s), a);

    Node n = node.getFirstChild();
    while (n != null) 
      {
      if (n instanceof TableHead)
	{ 
	renderTableHeader ((TableHead)n, lengths, a);
	} 
      else if (n instanceof TableBody)
	{
	renderTableBody ((TableBody)n, lengths, a);
	}

      Node next = n.getNext();
      n = next;
      }

    s = new StringBuffer();
    if (unicodeLineDrawing)
      {
      s.append ("└");
      for (int i = 0; i < lengths.length; i++)
        {
        s.append (pad ("", lengths[i] + 2, '─'));
        if (i < lengths.length - 1)
          s.append ("┴");
        }
      s.append ("┘");
      }
    else 
      {
      s.append ("+");
      for (int i = 0; i < lengths.length; i++)
        {
        s.append (pad ("", lengths[i] + 2, '-'));
        if (i < lengths.length - 1)
          s.append ("+");
        }
      s.append ("+");
      }
    writeln (new String (s), a);

    writeln ("```", a);
    if (spaceTables)
      newline (a); 
    }

  /** Render table body. All we have to do here is to render the
      rows -- there's no header stuff to draw. */
  private void renderTableBody (TableBody node, int[] lengths, Appendable a)
    {
    Node n = node.getFirstChild();
    while (n != null) 
      {
      if (n instanceof TableRow)
	{ 
        renderTableRow ((TableRow)n, lengths, a);
	} 
      Node next = n.getNext();
      n = next;
      }
    }

  /** Render a table header. We render the column data, then the separator
      between head and body. The first line header will have been drawn
      already in renderTable. */
  private void renderTableHeader (TableHead node, int[] lengths, Appendable a)
    {
    Node n = node.getFirstChild();
    while (n != null) 
      {
      if (n instanceof TableRow)
	{ 
        renderTableRow ((TableRow)n, lengths, a);
	} 
      Node next = n.getNext();
      n = next;
      }
    StringBuffer s = new StringBuffer();
    if (unicodeLineDrawing)
      {
      s.append ("├");
      for (int i = 0; i < lengths.length; i++)
        {
        s.append (pad ("", lengths[i] + 2, '─'));
        if (i < lengths.length - 1)
          s.append ("┼");
        }
      s.append ("┤");
      }
    else 
      {
      s.append ("+");
      for (int i = 0; i < lengths.length; i++)
        {
        s.append (pad ("", lengths[i] + 2, '-'));
        if (i < lengths.length - 1)
          s.append ("+");
        }
      s.append ("+");
      }
    writeln (new String (s), a);
    }

  /** Render the specified row, which may be in a table header or body. The
      lengths[] array contains the padding sizes for each column. */
  private void renderTableRow (TableRow n, int[] lengths, Appendable a)
    {
    int col = 0;
    write (tableColSep, a);
    Node n2 = n.getFirstChild();
    while (n2 != null) 
      {
      if (n2 instanceof TableCell)
	{
	Node n3 = n2.getFirstChild();
	if (n3 instanceof Text)
	  {
	  String literal = ((Text)n3).getLiteral();
	  literal = pad (literal, lengths[col]);
	  write (" " + literal, a);
	  write (" " + tableColSep, a);
	  } 
	else
	  {
          String text = extractText (n2);
	  String blank = pad (text, lengths[col]);
	  write (" " + blank, a);
	  write (" " + tableColSep, a);
	  } 
	}
      Node next = n2.getNext();
      n2 = next;
      col++;
      }
    newline (a);
    }

  /** Text is the leaf node of the Node tree. It has no child nodes, just
      a literal value. However, the real text in the document might be
      split into multiple Text nodes, which we have to concatenate.
  */
  private void renderText (Text node, Appendable a)
    {
    addToPara (node.getLiteral());
    }

  /** Write the text to the output appender, preceded
      by any prefixes that are currently in play. 
  */
  private void write (String s, Appendable a)
    {
    try
      {
      if (s.length() > 1)
        {
        if (!s.startsWith ("=>"))
          a.append (makePrefixes ());
        }
      a.append (s);
      }
    catch (IOException e){} // Shouldn't happen
    }

  private void writeln (String s, Appendable a)
    {
    write (s, a);
    newline (a);
    }
  }


