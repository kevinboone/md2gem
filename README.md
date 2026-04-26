# md2gem

Version 0.1, April 2026

## What is this?

`md2gem` is a Java-based, cross-platform command-line utility for making a
best-effort conversion from Commonmark Markdown to Gemini Gemtext. Since
Gemtext is so limited, an exact conversion is impossible, but it should be
possible to extract the main elements of a Markdown document.

## Installation

You'll need a Java JVM -- anything from the last twenty years should be OK. To
install, just copy the `md2gem` JAR file to any convenient place, and run it using
`java`.

## Usage

    java -jar md2gem.jar [options] [input_file [output_file]]

    Options:
      -e,--no-emphasis   Don't retain Markdown emphasis
      -h,--help          Show help
      -n,--no-unicode    Disable Unicode line drawing
      -s,--spacing       Add extra vertical spacing
      -v,--version       Show version 

Either or both of `input_file` and `output_file` may be "`-`", indicating
standard input and standard output respectively. Omitting one or both
these arguments will have the same effect.

## How `md2gem` handles hyperlinks

Markdown supports in-line links, but Gemtext does not. So, when `md2gem`
encounters links, it adds them to the document some point below where they
are found. The text part of the link, within the paragraph, it highlights with
`[` and `]` characters, so the reader can match up the text with the link.

This is far from ideal, but the alternative is to turn all links into
footnotes, which would be no better.

For well-defined block structures, like lists and tables, `md2gen` appends
links to the end of the block. It doesn't look very nice to break up a list or
table with links. For regular paragraphs, however, it just appends links to the
paragraph -- there really isn't a better place to put them, where there's a
natural break in the document. 

`m2gem` handles links best when the text of the link is capable of 
standing on its own. Then, when the link is re-positioned, its text will
make some kind of sense. So this would be a rather unhelpful 
formulation:

```
You can find this [here](https://lasers.acme.com).
```

But this would be better:

```
You can find this on the [ACME Laser Cannon](https://lasers.acme.com) site.
```

## How `md2gem` handles emphasis and in-line formatting

Since Gemtext has no support for in-line formatting, the options here are to
pass it straight through to the Gemtext file (the default) or strip it (using
the `-e` option).

Markdown formatting is pretty readable, so passing it through to the Gemtext is
usually OK.

## Table support

`md2gem` supports GitHub-style table markup, with the following limitations:

* tables must have a header row, and
* there must be the same number of columns in each row.

Table borders are rendered using Unicode box-drawing characters, unless you
give the `-n` ("no unicode drawing") command-line option. In that case,
`md2gem` will use ordinary ASCII symbols.

Tables should be laid out like this:

```
| Header row 1 | Header row 2|
| ------------ | ----------- |
| row 1 col 1  | row 1 col 2 |
| row 2 col 1  | row 2 col 2 |
| row 3 col 1  | row 3 col 2 |
```

`md2gen` will size the columns automatically, such that the largest item will
fit with one space before and after.

## Vertical spacing

By default, `md2gem` writes a fairly compact Gemtext file, with no additional
vertical spacing (that is, no blank lines). The `-s` switch makes the utility
add a blank line between many block display elements -- images, pre-formatted
blocks, and so on -- which can improve the appearance of complicated pages.

## Things to watch out for

### Nested items

In general, `md2gem` won't do very well with nested Markdown elements.  It does
support the nesting of links within images, tables, and lists but, other than
these, there's really no way to represent the structure in Gemtext. Markdown
supports nested links, for example, but there's no way to show such a thing in
Gemtext. `md2gem` flattens nested lists, leaving all but the top-level link
character (`*`) in place, so it's still possible to see something of the
structure. In general, though, it's best to avoid nesting other than links.

### Markdown image captions

When a Gemtext file contains images, Gemini clients almost always display
images with captions, whether or not the image itself is visible in-line. This
approach is not for Markdown, which is really intended to produce
always-visible, in-line images, with no text unless the image can't be
displayed.

`md2gem` gets an image caption for the Gemtext output from the "title" part of
the Markdown image specification if there is one, and the "alt text" part if
not.

Here's how to write a title in a markdown image

~~~
![](my_image.jpg "This is the title")
~~~

This is how to write alt text:

~~~
![This is the alt text](my_image.jpg)
~~~

`md2gem` accepts either format, because there's no consistency in this area.
If there's neither a title nor alt text, `md2gem` will use the image
URI as the caption.

### Markdown image links 

Markdown allows images to be used as the displayable part of a hyperlink.  This
doesn't sit well with Gemtext, which accepts only text as a link description.

`md2gem` _will_ write a link when the displayable part is an image, but it has
to write the link below the image on a separate line, as it does for ordinary
hyperlinks. However, it's difficult to know what to use as the link _text_ --
it has to be extracted from the image description.  So `md2gem` uses the alt
text as the link text in this situation.

All in all, it's probably better to avoid using images as displayable links,
because there's really no nice way to render them in Gemtext.

### Tables are monospaced

Rendering tabular data in Gemtext is only possible using a monospaced font, so
`md2gen` puts the whole table in a preformatted block. Most Gemini clients will
use a monospace font in such a block.

This looks a little odd when there's lots of tabular data but, for a few rows
and columns, it seems OK.

### Ordered lists

Gemtext does have a specific representation for ordered (numbered) lists.
`md2gem` will output the list items as ordinary paragraphs, with numbers.

## Author and legal

`md2gem` is copyright (c)2026 Kevin Boone, and distributed under the terms
of the GNU Public Licence, v3.0. There is no warranty of any kind.

The binary distribution contains the following open-source libraries.

* Apache Commons CLI, which is distributed under the terms of the 
[Apache licence](https://github.com/apache/commons-cli/blob/master/LICENSE.txt)
* commonmark-java, which has its own 
[open-source licence](https://github.com/commonmark/commonmark-java/blob/main/LICENSE.txt)

Please report bugs through GitHub.

