/*
 * Copyright 2004-2005 Revolution Systems Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.io.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.springframework.util.StringUtils;

import com.revolsys.converter.string.StringConverterRegistry;

/**
 * <p>
 * The XmlWriter class is a subclass of {@link Writer} that provides additional
 * methods to write XML documents.
 * </p>
 * 
 * @author Paul Austin
 */
public class XmlWriter extends Writer {
  /**
   * The TagConfiguration class is used to record the XML Namespace URIs defined
   * for an XML element.
   * 
   * @author Paul Austin
   */
  private class TagConfiguration {
    /** The namespaces defined on the element. */
    private final List<String> attributeDefinedNamespaces = new ArrayList<String>();

    private String tagDefinedNamespace;

    /** The QName of the current element. */
    private final QName element;

    /**
     * Construct a new TagConfiguration
     * 
     * @param element The QName of the current element.
     */
    TagConfiguration(final QName element) {
      this.element = element;
    }

    /**
     * Add the namespace URI to the list of namespaces for the element.
     * 
     * @param namespaceUri The namespace URI to add.
     */
    public void addAttributeDefinedNamespace(final String namespaceUri) {
      attributeDefinedNamespaces.add(namespaceUri);
    }

    public void setTagDefinedNamespace(String tagDefinedNamespace) {
      this.tagDefinedNamespace = tagDefinedNamespace;
    }

    public String getTagDefinedNamespace() {
      return tagDefinedNamespace;
    }

    /**
     * Get the namespaces defined on the element.
     * 
     * @return The namespaces defined on the element.
     */
    public List<String> getAttributeDefinedNamespaces() {
      return attributeDefinedNamespaces;
    }

    /**
     * Get the current element.
     * 
     * @return The current element.
     */
    public QName getElement() {
      return element;
    }

    /**
     * Return a string representation.
     * 
     * @return The string representation.
     */
    @Override
    public String toString() {
      return element.toString();
    }
  }

  private static final NumberFormat FORMAT = new DecimalFormat(
    "#.#########################");

  /** True if an XML declaration can be written. */
  private boolean canWriteXmlDeclaration = true;

  /** Flag indicating if a DOCTYPE has been written. */
  private boolean docTypeWritten = false;

  /** Flag indicating if endDocument has been called. */
  private boolean documentFinished = false;

  /** Flag indicating content has been written for the the current element. */
  private boolean elementHasContent = false;

  /** Flag indicating if an element has been started. */
  private boolean elementsStarted = false;

  /** The stack of open XML elements. */
  private final LinkedList<TagConfiguration> elementStack = new LinkedList<TagConfiguration>();

  /** Flag indicating if endDocument is running has been called. */
  private boolean endingDocument = false;

  /** Flag indicating that the xml elements should be indented. */
  private boolean indent;

  private boolean writeNewLine = true;

  public void setWriteNewLine(boolean writeNewLine) {
    this.writeNewLine = writeNewLine;
  }

  public boolean isWriteNewLine() {
    return writeNewLine;
  }

  /** The map of XML Namespace URIs to prefixes. */
  private final Map<String, String> namespacePrefixMap = new LinkedHashMap<String, String>();

  private final Map<String, String> namespaceAliasMap = new LinkedHashMap<String, String>();

  /** The string of characters to use for a new line. */
  private final String newLine = "\n";

  /** The underlying writer to write to. */
  private final PrintWriter out;

  /** Flag indicating that XML namespaces should be written to the output. */
  private final boolean useNamespaces;

  /** Flag indicating that a start tag has been written by not closed. */
  private boolean writingStartTag = false;

  /** Flag indicating that an XML declaration has been written. */
  private boolean xmlDeclarationWritten = false;

  private int prefixNum;

  /**
   * Construct a new XmlWriter.
   * 
   * @param out The output stream to write to.
   */
  public XmlWriter(final OutputStream out) {
    this(out, true);
  }

  /**
   * Construct a new XmlWriter that optionally ignores namespaces.
   * 
   * @param out The output stream to write to.
   * @param useNamespaces True if namespaces should be written, false if they
   *          should be ignored.
   */
  public XmlWriter(final OutputStream out, final boolean useNamespaces) {
    this(new OutputStreamWriter(out), useNamespaces);
  }

  /**
   * Construct a new XmlWriter.
   * 
   * @param out The output stream to write to.
   * @param charsetName The character set to output using.
   * @throws UnsupportedEncodingException
   */
  public XmlWriter(final OutputStream out, final String charsetName)
    throws UnsupportedEncodingException {
    this(out, charsetName, true);
  }

  /**
   * Construct a new XmlWriter.
   * 
   * @param out The output stream to write to.
   * @param charsetName The character set to output using.
   * @param useNamespaces True if namespaces should be written, false if they
   *          should be ignored.
   * @throws UnsupportedEncodingException
   */
  public XmlWriter(final OutputStream out, final String charsetName,
    final boolean useNamespaces) throws UnsupportedEncodingException {
    this(new OutputStreamWriter(out, charsetName), useNamespaces);
  }

  /**
   * Construct a new XmlWriter.
   * 
   * @param out The writer to write to.
   */
  public XmlWriter(final Writer out) {
    this(out, true);
  }

  /**
   * Construct a new XmlWriter that optionally ignores namespaces.
   * 
   * @param out The writer to write to.
   * @param useNamespaces True if namespaces should be written, false if they
   *          should be ignored.
   */
  public XmlWriter(final Writer out, final boolean useNamespaces) {
    this.out = new PrintWriter(out);
    this.useNamespaces = useNamespaces;
  }

  /**
   * Add an attribute to the current open tag.
   * 
   * @param attribute The QName of the attribute.
   * @param value The attribute value.
   * @throws IOException If there was an error writing.
   * @throws IllegalStateException If a start tag is not open.
   */
  public void attribute(final QName attribute, final boolean value) {
    attribute(attribute, String.valueOf(value));
  }

  /**
   * Add an attribute to the current open tag.
   * 
   * @param attribute The QName of the attribute.
   * @param value The attribute value.
   * @throws IOException If there was an error writing.
   * @throws IllegalStateException If a start tag is not open.
   */
  public void attribute(final QName attribute, final byte value) {
    attribute(attribute, String.valueOf(value));
  }

  /**
   * Add an attribute to the current open tag.
   * 
   * @param attribute The QName of the attribute.
   * @param value The attribute value.
   * @throws IOException If there was an error writing.
   * @throws IllegalStateException If a start tag is not open.
   */
  public void attribute(final QName attribute, final double value) {
    attribute(attribute, String.valueOf(value));
  }

  /**
   * Add an attribute to the current open tag.
   * 
   * @param attribute The QName of the attribute.
   * @param value The attribute value.
   * @throws IOException If there was an error writing.
   * @throws IllegalStateException If a start tag is not open.
   */
  public void attribute(final QName attribute, final float value) {
    attribute(attribute, String.valueOf(value));
  }

  /**
   * Add an attribute to the current open tag.
   * 
   * @param attribute The QName of the attribute.
   * @param value The attribute value.
   * @throws IOException If there was an error writing.
   * @throws IllegalStateException If a start tag is not open.
   */
  public void attribute(final QName attribute, final int value) {
    attribute(attribute, String.valueOf(value));
  }

  /**
   * Add an attribute to the current open tag.
   * 
   * @param attribute The QName of the attribute.
   * @param value The attribute value.
   * @throws IOException If there was an error writing.
   * @throws IllegalStateException If a start tag is not open.
   */
  public void attribute(final QName attribute, final long value) {
    attribute(attribute, String.valueOf(value));
  }

  /**
   * Add an attribute to the current open tag.
   * 
   * @param attribute The QName of the attribute.
   * @param value The attribute value.
   * @throws IOException If there was an error writing.
   * @throws IllegalStateException If a start tag is not open.
   */
  public void attribute(final QName attribute, final Object value) {
    if (value != null) {
      final String string = StringConverterRegistry.toString(value);
      attribute(attribute, string);
    }
  }

  /**
   * Add an attribute to the current open tag.
   * 
   * @param attribute The QName of the attribute.
   * @param value The attribute value.
   * @throws IOException If there was an error writing.
   * @throws IllegalStateException If a start tag is not open.
   */
  public void attribute(final QName attribute, final String value) {
    if (value != null) {
      checkWriteAttribute();

      final String namespaceUri = attribute.getNamespaceURI();
      if (namespaceUri.length() > 0) {
        String prefix = namespacePrefixMap.get(namespaceUri);
        if (prefix == null) {
          prefix = attribute.getPrefix();
          if (prefix == null || namespacePrefixMap.containsValue(prefix)) {
            prefix = "p" + ++prefixNum;
          }
          namespacePrefixMap.put(namespaceUri, prefix);
          elementStack.getFirst().addAttributeDefinedNamespace(namespaceUri);
          writeNamespaceAttribute(namespaceUri, prefix);
        }
      }
      out.write(' ');
      writeName(attribute, true);
      out.write("=\"");
      writeAttributeValue(value);
      out.write('"');
    }
  }

  public void attribute(final String name, final Object value) {
    if (value != null) {
      final String string = StringConverterRegistry.toString(value);
      attribute(name, string);
    }

  }

  public void attribute(final String name, final String value) {
    if (StringUtils.hasText(value)) {
      checkWriteAttribute();
      out.write(' ');
      out.write(name);
      out.write("=\"");
      writeAttributeValue(value);
      out.write('"');
    }
  }

  /**
   * Write the contents in a CDATA section. No escaping will be done on the data
   * the text must not contain "]]>".
   * 
   * @param text The text to wrap in a CDATA section
   * @throws IOException If there was a problem writing the text
   */
  public void cdata(final String text) {
    closeStartTag();
    out.write("<![CDATA[");
    out.write(text);
    out.write("]]>");
    setElementHasContent();
  }

  /**
   * Check that the document is not finished and can be written to.
   */
  private void checkNotFinished() {
    if (documentFinished) {
      throw new IllegalStateException(
        "Cannot write to a document after it has been finished");
    }
  }

  /**
   * Check that it is valid to write an attribute. A attribute declaration can
   * only be written if there is a start tag that is open.
   * 
   * @throws IllegalStateException If a start tag is not open.
   */
  private void checkWriteAttribute() {
    checkNotFinished();
    if (!writingStartTag) {
      throw new IllegalStateException(
        "A start tag must be open to write an attribute");
    }
  }

  /**
   * Check that it is valid to write a DOCTYPE declaration. A DOCTYPE
   * declaration can only be written if there has not been a DOCTYPE declaraion,
   * an element has not been written and the document has not been finished.
   * 
   * @throws IllegalStateException If a DOCTYPE cannot be written.
   */
  private void checkWriteDocType() {
    checkNotFinished();
    if (elementsStarted) {
      throw new IllegalStateException(
        "Cannot create doc type after elements have been created");
    }
    if (docTypeWritten) {
      throw new IllegalStateException(
        "A document can only have one DOCTYPE declaration");
    }
  }

  /**
   * Check that it is valid to write an XML declaration. An XML declaration can
   * only be written if there has not been a DOCTYPE declaraion or an element
   * written.
   * 
   * @throws IllegalStateException If an XML declaration cannot be written.
   */
  private void checkWriteXmlDeclaration() {
    checkNotFinished();
    if (!canWriteXmlDeclaration) {
      throw new IllegalStateException(
        "An XML declaration must be the first item in a document");
    }
    if (xmlDeclarationWritten) {
      throw new IllegalStateException(
        "A document can only have one XML declaration");
    }
  }

  /**
   * Close the underlying output stream or writer.
   * 
   * @throws IOException If the output stream or writer could not be closed.
   */
  @Override
  public void close() {
    out.flush();
    out.close();
  }

  /**
   * Close the current open start tag. If a start tag is not open then no action
   * will be taken.
   * 
   * @throws IOException If there was an error writing.
   */
  public void closeStartTag() {
    if (writingStartTag) {
      checkNotFinished();
      writeNamespaces();
      out.write('>');
      writingStartTag = false;
    }
  }

  /**
   * Write an XML comment. The comment should not contain the string '--'.
   * 
   * @param comment The comment to write
   * @throws IOException If there was a problem writing the comment
   */
  public void comment(final String comment) {
    closeStartTag();
    out.write("<!--");
    writeIndent();
    out.write(comment);
    writeEndIndent();
    out.write("-->");
    canWriteXmlDeclaration = false;
    setElementHasContent();
  }

  /**
   * Write a DTD section. This string represents the entire doctypedecl
   * production from the XML 1.0 specification.
   * 
   * @param dtd The DTD to be written
   * @throws IOException If there was a problem writing the declaration
   */
  public void docType(final String dtd) {
    checkWriteDocType();
    out.write(dtd);
    canWriteXmlDeclaration = false;
    docTypeWritten = true;
  }

  /**
   * Write a DOCTYPE declaration using a SYSTEM identifier.
   * 
   * @param name The root element name
   * @param systemId The system id
   * @throws IOException If there was a problem writing the declaration
   */
  public void docType(final String name, final String systemId) {
    checkWriteDocType();
    out.write("<!DOCTYPE ");
    out.write(name);
    if (systemId != null) {
      out.write(" SYSTEM \"");
      out.write(systemId);
      out.write("\"");
    }
    out.write(">");
    newLine();
    canWriteXmlDeclaration = false;
    docTypeWritten = true;
  }

  /**
   * Write a DOCTYPE declaration using a PUBLIC identifier.
   * 
   * @param name The root element name.
   * @param publicId The public id.
   * @param systemId The system id.
   * @throws IOException If there was a problem writing the declaration.
   */
  public void docType(final String name, final String publicId,
    final String systemId) {
    checkWriteDocType();
    out.write("<!DOCTYPE ");
    out.write(name);
    out.write(" PUBLIC \"");
    out.write(publicId);
    out.write("\" \"");
    out.write(systemId);
    out.write("\">");
    canWriteXmlDeclaration = false;
    docTypeWritten = true;
  }

  /**
   * Write the element with the specified content.
   * 
   * @param element The QName of the tag.
   * @param content The body context for the element.
   * @throws IOException If there was a problem writing the element.
   */
  public void element(final QName element, final Object content) {
    startTag(element);
    if (content != null) {
      text(content.toString());
    }
    endTag(element);
  }

  /**
   * Write an empty tag.
   * 
   * @param element The QName of the tag.
   * @throws IOException If there was a problem writing the tag.
   */
  public void emptyTag(final QName element) {
    startTag(element);
    endTag(element);
  }

  /**
   * End a document, closing all tags and flushing the output.
   * 
   * @throws IOException If the document could not be ended
   */
  public void endDocument() {
    endingDocument = true;
    for (final Iterator<TagConfiguration> elements = elementStack.iterator(); elements.hasNext();) {
      final TagConfiguration tag = elements.next();
      final QName element = tag.getElement();
      endTag(element);
      elements.remove();
    }
    documentFinished = true;
    flush();
  }

  /**
   * Write the end tag for the current element. If the element has no content it
   * will be written as an empty tag.
   * 
   * @throws IOException If there was a problem writing the element.
   */
  public void endTag() {
    endTag(getCurrentTag().getElement());
  }

  /**
   * Write the end tag for an element. If the element has no content it will be
   * written as an empty tag.
   * 
   * @param element The QName of the tag.
   * @throws IOException If there was a problem writing the element.
   */
  public void endTag(final QName element) {
    checkNotFinished();
    final TagConfiguration currentTag = getCurrentTag();
    if (currentTag == null) {
      throw new IllegalArgumentException("Cannot end tag " + element
        + " no open tag");
    } else {
      final QName currentElement = currentTag.getElement();
      if (!element.equals(currentElement)) {
        throw new IllegalArgumentException("Cannot end tag " + element
          + " expecting " + currentElement);
      }
      if (writingStartTag) {
        writeNamespaces();
        out.write(" />");
        writingStartTag = false;
      } else {
        writeEndIndent();
        out.write("</");
        writeName(element, false);
        out.write('>');
      }
      removeCurrentTag();
      elementHasContent = false;
    }
  }

  public void endTagLn(final QName element) {
    endTag(element);
    newLine();
  }

  public void startTagLn(final QName element) {
    startTag(element);
    newLine();
  }

  /**
   * Write the entity reference for the specified character.
   * 
   * @param ch The character to write the entity for
   * @throws IOException If there was a problem writing the entity
   */
  public void entityRef(final char ch) {
    closeStartTag();
    out.write("&#");
    out.write(ch);
    out.write(';');
    setElementHasContent();
  }

  /**
   * Write the entity reference with the specified text.
   * 
   * @param name The name of the entity
   * @throws IOException If there was a problem writing the entity
   */
  public void entityRef(final String name) {
    closeStartTag();
    out.write('&');
    out.write(name);
    out.write(';');
    setElementHasContent();
  }

  /**
   * Flush the output closing the current start tag.
   * 
   * @throws IOException If there was an exception flushing the writer.
   */
  @Override
  public void flush() {
    if (!documentFinished) {
      closeStartTag();
    }
    out.flush();
  }

  /**
   * Get the TagConfiguration of the current open tag.
   * 
   * @return The TagConfiguration of the current open tag.
   */
  private TagConfiguration getCurrentTag() {
    if (elementStack.isEmpty()) {
      return null;
    } else {
      return elementStack.getFirst();
    }
  }

  /**
   * Get the prefix for the XML Namespace URI.
   * 
   * @param namespaceUri The XML Namespace URI.
   * @return The prefix.
   */
  public String getPrefix(final String namespaceUri) {
    if (namespaceUri == null || namespaceUri.equals("")) {
      return null;
    } else {
      return namespacePrefixMap.get(namespaceUri);
    }
  }

  /**
   * Get a new QName for the specified QName with the defined prefix for that
   * XML Namespace.
   * 
   * @param qName The QName without the prefix.
   * @param attribute
   * @return The QName with the prefix.
   */
  private QName getQNameWithPrefix(final QName qName, boolean attribute) {
    final String namespaceUri = qName.getNamespaceURI();
    if (namespaceUri.equals("")) {
      return new QName(qName.getLocalPart());

    }
    String prefix = namespacePrefixMap.get(namespaceUri);
    if (prefix == null) {
      prefix = qName.getPrefix();
      if (!attribute) {
        getCurrentTag().setTagDefinedNamespace(namespaceUri);
      }
      namespacePrefixMap.put(namespaceUri, prefix);
      return new QName(namespaceUri, qName.getLocalPart(), prefix);
    }
    if (prefix == qName.getPrefix()) {
      return qName;
    } else {
      return new QName(namespaceUri, qName.getLocalPart(), prefix);
    }
  }

  /**
   * Get the flag indicating that the xml elements should be indented.
   * 
   * @return The flag indicating that the xml elements should be indented.
   */
  public boolean isIndent() {
    return indent;
  }

  /**
   * Write a newLine to the writer. If an XML start tag is open it will be
   * closed before the new line is written.
   * 
   * @throws IOException If there was an exception writing the new line.
   */
  public void newLine() {
    closeStartTag();
    out.write(newLine);
  }

  /**
   * Write the element with the specified content, if null xsi:nil attribute
   * will be set.
   * 
   * @param element The QName of the tag.
   * @param content The body context for the element.
   * @throws IOException If there was a problem writing the element.
   */
  public void nillableElement(final QName element, final Object content) {
    startTag(element);
    if (content == null) {
      attribute(XsiConstants.NIL, "true");
    } else {
      text(content.toString());
    }
    endTag(element);
  }

  /**
   * Write an XML processing instruction.
   * 
   * @param target The PI Target (must not be xml)
   * @param value The value of the processing instruction
   * @throws IOException If there was a problem writing the comment
   */
  public void processingInstruction(final String target, final String value) {
    closeStartTag();
    out.write("<?");
    out.write(target);
    if (value != null) {
      out.write(" ");
      out.write(value);
    }
    out.write("?>");
  }

  /**
   * Remove the current tag from the stack of open tags.
   */
  private void removeCurrentTag() {
    if (!endingDocument) {
      final TagConfiguration tag = elementStack.removeFirst();
      final Iterator<String> namespaceUris = tag.getAttributeDefinedNamespaces()
        .iterator();
      while (namespaceUris.hasNext()) {
        final String namespaceUri = namespaceUris.next();
        namespacePrefixMap.remove(namespaceUri);
      }
    }
  }

  /**
   * @param element
   */
  private void setCurrentTag(final QName element) {
    elementStack.addFirst(new TagConfiguration(element));
  }

  public void setElementHasContent() {
    elementHasContent = true;
  }

  /**
   * Set the flag indicating that the xml elements should be indented.
   * 
   * @param indent The flag indicating that the xml elements should be indented.
   */
  public void setIndent(final boolean indent) {
    this.indent = indent;
  }

  public void setNamespaceAlias(final String namespaceUri, final String alias) {
    this.namespaceAliasMap.put(namespaceUri, alias);
  }

  public void setPrefix(final QName typePath) {
    setPrefix(typePath.getPrefix(), typePath.getNamespaceURI());
  }

  /**
   * Set the prefix for the XML Namespace URI.
   * 
   * @param prefix The prefix.
   * @param namespaceUri The XML Namespace URI.
   */
  public void setPrefix(final String prefix, final String namespaceUri) {
    if (getPrefix(namespaceUri) == null) {
      namespacePrefixMap.put(namespaceUri, prefix);
    }
    final TagConfiguration currentTag = getCurrentTag();
    if (currentTag != null) {
      currentTag.addAttributeDefinedNamespace(namespaceUri);
    }
  }

  /**
   * Start a document with an empty XML Declaration.
   * 
   * @throws IOException If there was a problem writing the XML Declaration.
   */
  public void startDocument() {
    startDocument(null, null, null);
  }

  /**
   * Start a document with an XML Declaration for the specified encoding.
   * 
   * @param encoding The encoding for the document
   * @throws IOException If there was a problem writing the XML Declaration.
   */
  public void startDocument(final String encoding) {
    startDocument(encoding, null, null);
  }

  /**
   * Start a document with an XML Declaration for the specified encoding and the
   * standalone flag.
   * 
   * @param encoding The encoding for the document
   * @param standalone The standalone flag
   * @throws IOException If there was a problem writing the XML Declaration.
   */
  public void startDocument(final String encoding, final boolean standalone) {
    startDocument(encoding, Boolean.valueOf(standalone));
  }

  /**
   * Start a document with an XML Declaration for the specified encoding and the
   * standalone flag (null will omit the flag).
   * 
   * @param encoding The encoding for the document
   * @param standalone The standalone flag
   * @throws IOException If there was a problem writing the XML Declaration.
   */
  public void startDocument(final String encoding, final Boolean standalone) {
  }

  /**
   * Start a document with an XML Declaration for the specified encoding.
   * 
   * @param encoding The encoding for the document
   * @param version The XML version.
   * @throws IOException If there was a problem writing the XML Declaration.
   */
  public void startDocument(final String encoding, final String version) {
    startDocument(encoding, version, null);
  }

  /**
   * Start a document with an XML Declaration for the specified encoding and the
   * standalone flag (null will omit the flag).
   * 
   * @param encoding The encoding for the document
   * @param version The XML version.
   * @param standalone The standalone flag
   * @throws IOException If there was a problem writing the XML Declaration.
   */
  public void startDocument(final String encoding, final String version,
    final Boolean standalone) {
    checkWriteXmlDeclaration();
    if (version == null) {
      out.write("<?xml version=\"1.0\"");
    } else {
      out.write("<?xml version=\"" + version + "\"");

    }
    if (encoding != null) {
      out.write(" encoding=\"");
      out.write(encoding);
      out.write('"');
    }
    if (standalone != null) {
      if (standalone.booleanValue()) {
        out.write(" standalone=\"yes\"");
      } else {
        out.write(" standalone=\"no\"");
      }
    }
    out.write("?>\n");
    xmlDeclarationWritten = true;
    canWriteXmlDeclaration = false;
  }

  /**
   * Write the start tag for an element.
   * 
   * @param element The QName of the tag.
   * @throws IOException If there was a problem writing the element.
   */
  public void startTag(final QName element) {
    checkNotFinished();
    closeStartTag();
    writeIndent();
    writingStartTag = true;
    setCurrentTag(element);
    out.write('<');
    writeName(element, false);
    elementHasContent = false;
  }

  /**
   * Write the start tag for an element.
   * 
   * @param namespaceUri The namespace URI.
   * @param localPart The local name.
   * @throws IOException If there was a problem writing the element.
   */
  public void startTag(final String namespaceUri, final String localPart) {
    startTag(new QName(namespaceUri, localPart));
  }

  /**
   * Write the boolean value as the content of a tag with special characters
   * escaped.
   * 
   * @param value The value.
   * @throws IOException If there was a problem writing the text.
   */
  public void text(final boolean value) {
    text(String.valueOf(value));
  }

  /**
   * Write a portion of the character buffer to the outpu, escaping special
   * characters.
   * 
   * @param buffer The buffer to write.
   * @param offset The starting offset in the buffer.
   * @param length The number of characters to write.
   * @throws IOException If there was a problem writing the text.
   */
  public void text(final char[] buffer, final int offset, final int length) {
    closeStartTag();
    writeElementContent(buffer, offset, length);
    setElementHasContent();
  }

  /**
   * Write the double value as the content of a tag with special characters
   * escaped.
   * 
   * @param value The value.
   * @throws IOException If there was a problem writing the text.
   */
  public void text(final double value) {
    final String text = FORMAT.format(value);
    text(text);
  }

  /**
   * Write the float value as the content of a tag with special characters
   * escaped.
   * 
   * @param value The value.
   * @throws IOException If there was a problem writing the text.
   */
  public void text(final float value) {
    final String text = FORMAT.format(value);
    text(text);
  }

  /**
   * Write the int value as the content of a tag with special characters
   * escaped.
   * 
   * @param value The value.
   * @throws IOException If there was a problem writing the text.
   */
  public void text(final int value) {
    text(String.valueOf(value));
  }

  /**
   * Write the long value as the content of a tag with special characters
   * escaped.
   * 
   * @param value The value.
   * @throws IOException If there was a problem writing the text.
   */
  public void text(final long value) {
    text(String.valueOf(value));
  }

  /**
   * Write the object value as the content of a tag with special characters
   * escaped. If the value is null it will not be written.
   * 
   * @param value The value.
   * @throws IOException If there was a problem writing the text.
   */
  public void text(final Object value) {
    if (value != null) {
      if (value instanceof Number) {
        final Number number = (Number)value;
        final String text = FORMAT.format(number);
        text(text);
      } else {
        text(value.toString());
      }
    }
  }

  /**
   * Write the text string to the output, escaping special characters.
   * 
   * @param text The text to write
   * @throws IOException If there was a problem writing the text
   */
  public void text(final String text) {
    if (text != null) {
      text(text.toCharArray(), 0, text.length());
    }
  }

  /**
   * Write an array of characters.
   * 
   * @param buffer The character buffer to write.
   * @throws IOException If an I/O exception occurs.
   */
  @Override
  public void write(final char[] buffer) {
    write(buffer, 0, buffer.length);
  }

  /**
   * Write a portion of an array of characters.
   * 
   * @param buffer The character buffer to write.
   * @param offset The starting offset in the buffer.
   * @param length The number of characters to write.
   * @throws IOException If an I/O exception occurs.
   */
  @Override
  public void write(final char[] buffer, final int offset, final int length) {
    closeStartTag();
    out.write(buffer, offset, length);
    setElementHasContent();
  }

  /**
   * Write a single character.
   * 
   * @param character The character to write.
   * @throws IOException If an I/O exception occurs.
   */
  @Override
  public void write(final int character) {
    closeStartTag();
    out.write(character);
    setElementHasContent();
  }

  /**
   * Write a String.
   * 
   * @param string The String to write.
   * @throws IOException If an I/O exception occurs.
   */
  @Override
  public void write(final String string) {
    write(string.toCharArray(), 0, string.length());
  }

  /**
   * Write a portion of a String.
   * 
   * @param string The String to write.
   * @param offset The starting offset in the buffer.
   * @param length The number of characters to write.
   * @throws IOException If an I/O exception occurs.
   */
  @Override
  public void write(final String string, final int offset, final int length) {
    write(string.toCharArray(), 0, length);
  }

/**
         * Write content for an attribute value to the output, escaping the characters
         * that are used within markup. This method will escape characters ' <', '>',
         * '&', 9, 10, 13 and '"'. Note the XML 1.0 standard does allow '>' to be used
         * unless it is part of "]]>" for simplicity it is allways escaped in this
         * implementation.
         * 
         * @param buffer The character buffer to write
         * @param offset The offset in the character data to write
         * @param length The number of characters to write.
         * @throws IOException If an I/O exception occurs.
         */
  protected void writeAttributeContent(final char[] buffer, final int offset,
    final int length) {
    final int lastIndex = offset + length;
    int index = offset;
    String escapeString = null;
    for (int i = offset; i < lastIndex; i++) {
      final char ch = buffer[i];
      switch (ch) {
        case '&':
          escapeString = "&amp;";
        break;
        case '<':
          escapeString = "&lt;";
        break;
        case '>':
          escapeString = "&gt;";
        break;
        case '"':
          escapeString = "&quot;";
        break;
        case 9:
          escapeString = "&#9;";
        break;
        case 10:
          escapeString = "&#10;";
        break;
        case 13:
          escapeString = "&#13;";
        break;
        default:
          // Reject all other control characters
          if (ch < 32) {
            throw new IllegalStateException("character " + Integer.toString(ch)
              + " is not allowed in output");
          }
        break;
      }
      if (escapeString != null) {
        if (i > index) {
          out.write(buffer, index, i - index);
        }
        out.write(escapeString);
        escapeString = null;
        index = i + 1;
      }
    }
    if (lastIndex > index) {
      out.write(buffer, index, lastIndex - index);
    }
  }

  /**
   * Write the value of an attribute.
   * 
   * @param value The value to write.
   * @throws IOException If an I/O exception occurs.
   */
  protected void writeAttributeValue(final String value) {
    writeAttributeContent(value.toCharArray(), 0, value.length());
  }

/**
         * Write content for an element to the output, escaping the characters that
         * are used within markup. This method will escape characters ' <', '>' and
         * '&'. Note the XML 1.0 standard does allow '>' to be used unless it is part
         * of "]]>" for simplicity it is allways escaped in this implementation.
         * 
         * @param buffer The character buffer to write
         * @param offest The offset in the character data to write
         * @param length The number of characters to write.
         * @throws IOException If an I/O exception occurs.
         */
  protected void writeElementContent(final char[] buffer, final int offest,
    final int length) {
    int index = offest;
    final int lastIndex = index + length;
    String escapeString = null;
    for (int i = index; i < lastIndex; i++) {
      final char ch = buffer[i];
      switch (ch) {
        case '&':
          escapeString = "&amp;";
        break;
        case '<':
          escapeString = "&lt;";
        break;
        case '>':
          escapeString = "&gt;";
        break;
        case 9:
        case 10:
        case 13:
        // Accept these control characters
        break;
        default:
          // Reject all other control characters
          if (ch < 32) {
            throw new IllegalStateException("character " + Integer.toString(ch)
              + " is not allowed in output");
          }
        break;
      }
      if (escapeString != null) {
        if (i > index) {
          out.write(buffer, index, i - index);
        }
        out.write(escapeString);
        escapeString = null;
        index = i + 1;
      }
    }
    if (lastIndex > index) {
      out.write(buffer, index, lastIndex - index);
    }
  }

  /**
   * Write the indent for the end of an element.
   * 
   * @throws IOException If an I/O exception occurs.
   */
  private void writeEndIndent() {
    if (!elementHasContent) {
      if (writeNewLine) {
        out.write(newLine);
      }
      if (indent) {
        final int depth = elementStack.size() - 1;
        for (int i = 0; i < depth; i++) {
          out.write("  ");
        }
      }
    }
  }

  /**
   * Write the indent for a child of an element.
   * 
   * @throws IOException If an I/O exception occurs.
   */
  private void writeIndent() {
    if (elementsStarted) {
      if (writeNewLine) {
        out.write(newLine);
      }
      if (indent) {
        final int depth = elementStack.size();
        for (int i = 0; i < depth; i++) {
          out.write("  ");
        }
      }
    } else {
      elementsStarted = true;
    }
  }

  /**
   * Write out a QName, if namespaces are used the name will be written with the
   * prefix of the namespace.
   * 
   * @param qName The QName to write
   * @param attribute TODO
   * @throws IOException If an I/O exception occurs.
   */
  private void writeName(final QName qName, boolean attribute) {
    if (useNamespaces) {
      final String namespaceUri = qName.getNamespaceURI();
      String prefix = namespacePrefixMap.get(namespaceUri);
      final QName prefixedQName = getQNameWithPrefix(qName, attribute);
      prefix = prefixedQName.getPrefix();
      if (prefix.length() != 0) {
        out.write(prefix);
        out.write(':');
      }
    }
    final String name = qName.getLocalPart();
    out.write(name);
  }

  public void writeNamespaceAttribute(final String namespaceUri,
    final String prefix) {
    if (prefix.length() == 0) {
      out.write(" xmlns");

    } else {
      out.write(" xmlns:");
      out.write(prefix);
    }
    out.write("=\"");
    writeAttributeValue(namespaceUri);
    out.write("\"");
  }

  /**
   * Write the XML namespace declarations for an element.
   * 
   * @throws IOException If an I/O exception occurs.
   */
  private void writeNamespaces() {
    if (useNamespaces) {
      final TagConfiguration tag = getCurrentTag();

      final Collection<String> namespaceUris;
      if (elementStack.size() == 1) {
        namespaceUris = namespacePrefixMap.keySet();
      } else {
        String tagNamespace = tag.getTagDefinedNamespace();
        if (tagNamespace == null) {
          namespaceUris = Collections.emptyList();
        } else {
          namespaceUris = Collections.singletonList(tagNamespace);
        }
      }
      for (final String namespaceUri : namespaceUris) {
        final String prefix = namespacePrefixMap.get(namespaceUri);
        final String alias = namespaceAliasMap.get(namespaceUri);
        if (alias == null) {
          writeNamespaceAttribute(namespaceUri, prefix);
        } else {
          writeNamespaceAttribute(alias, prefix);
        }
      }
    }
  }

  public void xsiTypeAttribute(final QName xsiTagName) {
    final String namespaceUri = xsiTagName.getNamespaceURI();
    final String xsiName = xsiTagName.getLocalPart();
    if (namespaceUri.length() > 0) {
      String prefix = namespacePrefixMap.get(namespaceUri);
      if (prefix == null) {
        prefix = xsiTagName.getPrefix();
        if (prefix == null || namespacePrefixMap.containsValue(prefix)) {
          prefix = "p" + ++prefixNum;
        }
        namespacePrefixMap.put(namespaceUri, prefix);
        writeNamespaceAttribute(namespaceUri, prefix);
      }
      attribute(XsiConstants.TYPE, prefix + ":" + xsiName);
    } else {
      attribute(XsiConstants.TYPE, xsiName);
    }
  }
}
