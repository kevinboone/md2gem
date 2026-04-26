/*============================================================================

  md2gem

  FileUtil 

  Copyright (c)2026 Kevin Boone, GPL3

============================================================================*/
package me.kevinboone.md2gem;
import java.io.*;
import java.nio.file.*;

/** General purpose utility methods for manipulating local files.
*/
public class FileUtil
  {
  /** Read an entire file into a String.
  */
  public static String fileToString (String filename)
      throws IOException
    {
    byte[] b = Files.readAllBytes(Paths.get (filename));
    return new String (b); // Platform encoding
    }

  /** Read an entire stream into a String.
  */
  public static String streamToString (InputStream in)
      throws IOException
    {
    ByteArrayOutputStream result = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    for (int length; (length = in.read(buffer)) != -1;)  
      {
      result.write(buffer, 0, length);
      }
    return result.toString("UTF-8");
    }
  }

