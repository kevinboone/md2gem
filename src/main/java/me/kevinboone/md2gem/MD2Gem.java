/*============================================================================

  md2gem

  MD2Gem 

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
import org.apache.commons.cli.Option;  
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;  
import org.apache.commons.cli.CommandLineParser;  
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;  
 
/** This is the class that does most of the work.
*/
public class MD2Gem
  {
  /** This method dumps a node tree, for debugging purposes.
  */
  private static void dumpNode (Node node, int level)
    {
    for (int i = 0; i < level * 3; i++) System.out.print ("   ");
    System.out.println (node);
    Node n = node.getFirstChild();
    while (n != null) 
      {
      Node next = n.getNext();
      dumpNode (n, level + 1);
      n = next;
      }
    }
  
  /** Convert the Markdown file `input`, writing to the stream `out`. 
  */
  public static String convert (String input, PrintStream out, Config config)
    {
    // Table support is an extension to the basic commonmark-java library
    List<Extension> extensions = List.of (TablesExtension.create());

    // Parse
    Parser parser = Parser.builder().extensions (extensions).build();
    Node document = parser.parse (input);

    // Modify the node tree
    MyVisitor visitor = new MyVisitor();
    document.accept (visitor);

    //dumpNode (document, 0);   

    // Render the tree
    // Not that we're using a completely custom renderer, not a modifiation
    //   of one of commonmark-java's built-in renderers.
    GemtextRenderer renderer = new GemtextRenderer (config);
    String result = renderer.render (document);

    return result;
    }
  }

/** This visitor class processes in-line links. We have to take the link
    out of the paragraph, and write it to a list of links that are active
    for the paragraph. We do this by adding GemLink objects to a list.
    At the end of each paragraph we'll check for these links, and add
    then to the Paragraph node as new Text elements. It's slightly 
    easier to do this at the level of the parse tree than during 
    rendering, although both are ugly.
*/
class MyVisitor extends AbstractVisitor 
  {
  Vector<GemLink> links = new Vector<GemLink>();

  /** Descend the node tree, picking out any text elements as
      we go. This should capture any text associated with the
      link.
  */
  private String extractText (Node node) 
    {
    String ret = "";
    Node n = node.getFirstChild();
    while (n != null) 
      {
      if (n instanceof Text)
        {
        Text nt = (Text)n;
        String text = nt.getLiteral();
        if (ret.length() > 0 && text.length() > 0) ret += " ";
        ret += text;
        }
      else
        {
        String text = extractText (n);
        if (ret.length() > 0 && text.length() > 0) ret += " ";
        ret += text;
        }
      Node next = n.getNext();
      n = next;
      }
    return ret;
    }

  /* For each paragraph node, check whether any links are outstanding,
     and add them as new Text nodes. This only works because any links
     are direct children of the Paragraph node where they're used. If
     the node tree visitor worked even slightly differently, the crude
     method I use here to store the link information would fail. 
  */
  @Override
  public void visit (Paragraph para) 
    {
    putLinksOnEnd (para, para);
    }

  private void extractLinks (Node node, Vector<GemLink> links)
    {
    Node n = node.getFirstChild();
    while (n != null) 
      {
      if (n instanceof Link)
        {
        Link link = (Link)n;
	String href = link.getDestination();
	String text = extractText (link);
	if (text.length() == 0) text = href;
	GemLink gemLink = new GemLink (href, text); 
	links.addElement (gemLink);
        }
      else
        {
        extractLinks (n, links);
        }
      Node next = n.getNext();
      n = next;
      }
    }

  @Override
  public void visit (BulletList list) 
    {
    putLinksOnEnd (list, list.getParent());
    }

  @Override
  public void visit (OrderedList list) 
    {
    putLinksOnEnd (list, list.getParent());
    }

  @Override
  public void visit (CustomBlock table) 
    {
    putLinksOnEnd (table, table.getParent());
    }

  /** Find all the links below this node, and put them on the end of the 
      parent node (probably the top-level Document node. 
  */
  public void putLinksOnEnd (Node n, Node target) 
    {
    Vector<GemLink> links = new Vector<GemLink>();
    extractLinks (n, links);
    if (links.size() > 0)
      {
      target.appendChild (new HardLineBreak());
      if (n != null)
        {
	for (GemLink link : links)
	  {
	  String text = link.toString(); 
	  target.appendChild (new Text (text));
	  target.appendChild (new HardLineBreak());
	  }
        }
      }
    }
  }



