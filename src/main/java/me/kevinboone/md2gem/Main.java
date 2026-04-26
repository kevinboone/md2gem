/*============================================================================

  md2gem

  Main

  Copyright (c)2026 Kevin Boone, GPL3

============================================================================*/
package me.kevinboone.md2gem;

import java.io.*;
import java.util.*;
import org.apache.commons.cli.Option;  
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;  
import org.apache.commons.cli.CommandLineParser;  
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;  
 
/** This is the starting class for md2gem.
*/
public class Main
  {
  public static void main (String[] args)
      throws Exception
    {
    Config config = new Config();

    // Define and parse the command line

    Options options = new Options();
    Option noUnicodeOption = new Option ("n", "no-unicode", false, 
      "Disable Unicode line drawing");  
    options.addOption (noUnicodeOption); 
    Option noEmphasisOption = new Option ("e", "no-emphasis", false, 
      "Don't retain Markdown emphasis");  
    options.addOption (noEmphasisOption); 
    Option spacingOption = new Option ("s", "spacing", false, 
      "Add extra vertical spacing");  
    options.addOption (spacingOption); 
    Option versionOption = new Option ("v", "version", false, 
      "Show version");  
    options.addOption (versionOption); 
    Option helpOption = new Option ("h", "help", false, "Show help");  
    options.addOption (helpOption);  
    CommandLineParser clparser = new DefaultParser();  

    CommandLine cmd = null;
    try
      {
      cmd = clparser.parse (options, args);
      }
    catch (Exception e)
      {
      System.out.println (e.getMessage());
      printHelp (options);  
      System.exit (0);  
      }

    String[] bareArgs = cmd.getArgs(); 

    if (cmd.hasOption("h")) 
      {  
      printHelp (options);  
      System.exit (0);  
      } 

    if (cmd.hasOption("v")) 
      {  
      printVersion();  
      System.exit (0);  
      } 

    if (cmd.hasOption("n")) 
      config.setUnicodeLineDrawing (false);
    else
      config.setUnicodeLineDrawing (true);

    if (cmd.hasOption("e")) 
      config.setRetainEmphasis (false);
    else
      config.setRetainEmphasis (true);

    if (cmd.hasOption("s")) 
      config.setExtraSpacing (true);
    else
      config.setExtraSpacing (false);

    String inputFilename, outputFilename;

    if (bareArgs.length >= 1)
      inputFilename = bareArgs[0];
    else
      inputFilename = "-";

    if (bareArgs.length >= 2)
      outputFilename = bareArgs[1];
    else
      outputFilename = "-";

    String input;
    if ("-".equals (inputFilename))
      input = FileUtil.streamToString (System.in);
    else
      input = FileUtil.fileToString (inputFilename);

    // We'll check we can open the output file/stream before starting
    //   the processing, just to speed things up if there's a problem.
    
    PrintStream out; 
    if ("-".equals (outputFilename))
      out = System.out;
    else
      out = new PrintStream (new FileOutputStream (new File (outputFilename)));

    // Do the conversion

    String result = MD2Gem.convert (input, out, config);
    out.println (result);

    // Close the output stream if we opened it

    if (!"-".equals (outputFilename))
      out.close(); 
    }

  private static void printHelp (Options options) 
    {  
    HelpFormatter formatter = new HelpFormatter();  
    formatter.printHelp ("java -jar md2gem [options] {input_file} [output_file]", options);
    } 

  private static void printVersion() 
    {  
    System.out.print (Version.APP_NAME);
    System.out.print (" version ");
    System.out.println (Version.VERSION);
    System.out.println (Version.COPY_MSG);
    } 

  }

