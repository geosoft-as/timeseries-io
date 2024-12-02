package no.geosoft.jtimeseries.util;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.CharacterIterator;
import java.text.ParseException;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Convenience methods that makes up for the terrible API of the
 * standard JDK XML package.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class XmlUtil
{
  /**
   * Private constructor to prevent client instantiation.
   */
  private XmlUtil()
  {
    assert false : "This constructor should never be called";
  }

  /**
   * Escape characters for text appearing as XML data, between tags.
   *
   * <P>
   * The following characters are replaced with corresponding character
   * entities:
   * <table border='1' cellpadding='3' cellspacing='0'>
   *   <tr><th> Character </th><th> Encoding </th></tr>
   *   <tr><td> &lt; </td><td> &amp;lt; </td></tr>
   *   <tr><td> &gt; </td><td> &amp;gt; </td></tr>
   *   <tr><td> &amp; </td><td> &amp;amp; </td></tr>
   *   <tr><td> " </td><td> &amp;quot;</td></tr>
   *   <tr><td> ' </td><td> &amp;#039;</td></tr>
   * </table>
   *
   * @param s  String to encode. Non-null.
   * @return   XML encoded version of s. Never null.
   * @throws IllegalArgumentException  If s is null.
   */
  public static String xmlEncode(String s)
  {
    if (s == null)
      throw new IllegalArgumentException("s cannot be null");

    StringBuilder xml = new StringBuilder();

    StringCharacterIterator iterator = new StringCharacterIterator(s);
    char c = iterator.current();

    while (c != CharacterIterator.DONE) {
      if (c == '<')
        xml.append("&lt;");
      else if (c == '>')
        xml.append("&gt;");
      else if (c == '\"') // "
        xml.append("&quot;");
      else if (c == '\'')
        xml.append("&#039;");
      else if (c == '&')
        xml.append("&amp;");
      else
        xml.append(c);

      c = iterator.next();
    }

    return xml.toString();
  }

  /**
   * Write the specified XML document to the given file.
   *
   * @param document  Document to write. Non-null.
   * @param file      File to write to. Non-null.
   * @throws IllegalArgumentException  If document or file is null.
   * @throws IOException  If the write operation fails for some reason.
   */
  public static void writeToFile(Document document, File file)
    throws IOException
  {
    if (document == null)
      throw new IllegalArgumentException("document cannot be null");

    if (file == null)
      throw new IllegalArgumentException("file cannot be null");

    DOMSource source = new DOMSource(document);
    StreamResult destination = new StreamResult(file);

    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    transformerFactory.setAttribute("indent-number", 2);

    try {
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
      transformer.transform(source, destination);
    }
    catch (TransformerConfigurationException exception) {
      throw new IOException("Unable to save: " + file, exception);
    }
    catch (TransformerException exception) {
      throw new IOException("Unable to save: " + file, exception);
    }
  }

  /**
   * Create a new empty XML document.
   *
   * @return  Created document. Never null.
   * @throws IllegalStateException  If parser is not configured right.
   */
  public static Document newDocument()
  {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    try {
      DocumentBuilder documentBuilder = factory.newDocumentBuilder();
      return documentBuilder.newDocument();
    }
    catch (ParserConfigurationException exception) {
      // Convert the checked exception to an unchecked one as this
      // will never occur in practice anyway
      throw new IllegalStateException(exception);
    }
  }

  /**
   * Create an XML document from the specified XML string.
   *
   * @param xml  XML string to create document from. Non-null.
   * @return     The requested document. Never null.
   * @throws IllegalArgumentException  If xml is null.
   * @throws SAXException  If the string is not a proper XML document.
   */
  public static Document newDocument(String xml)
    throws SAXException
  {
    if (xml == null)
      throw new IllegalArgumentException("xml cannot be null");

    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

    try {
      DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

      // Suppress error messages written to stdout
      documentBuilder.setErrorHandler(null);

      return documentBuilder.parse(new InputSource(new StringReader(xml)));
    }
    catch (ParserConfigurationException exception) {
      throw new SAXException(xml, exception);
    }
    catch (IOException exception) {
      throw new SAXException(xml, exception);
    }
  }

  /**
   * Create an XML document from the specified file.
   *
   * @param file  File to create document from. Non-null.
   * @return      The requested document. Never null.
   * @throws IllegalArgumentException  If file is null.
   * @throws IOException   If the file cannot be accessed for some reason.
   * @throws SAXException  If the file doesn't contain a proper XML document.
   */
  public static Document newDocument(File file)
    throws IOException, SAXException
  {
    if (file == null)
      throw new IllegalArgumentException("file cannot be null");

    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

    try {
      DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
      return documentBuilder.parse(file);
    }
    catch (ParserConfigurationException exception) {
      throw new SAXException(exception);
    }
  }

  /**
   * Create an XML document from the specified input stream.
   *
   * @param inputStream  Input stream to create document from. Non-null.
   * @return             The requested document. Never null.
   * @throws IllegalArgumentException  If inputStream is null.
   * @throws IOException   If the input stream cannot be accessed for some reason.
   * @throws SAXException  If the input stream doesn't contain a proper XML document.
   */
  public static Document newDocument(InputStream inputStream)
    throws IOException, SAXException
  {
    if (inputStream == null)
      throw new IllegalArgumentException("inputStream cannot be null");

    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

    try {
      DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
      return documentBuilder.parse(inputStream);
    }
    catch (ParserConfigurationException exception) {
      throw new SAXException(exception);
    }
  }

  /**
   * Check if the specified XML is well-formed.
   *
   * @param xml  XML to check. Non-null.
   * @return     True if the XML is well-formed, false otherwise.
   * @throws IllegalArgumentException  If xml is null.
   */
  public static boolean isWellFormed(String xml)
  {
    if (xml == null)
      throw new IllegalArgumentException("xml cannot be null");

    try {
      newDocument(xml);
      return true;
    }
    catch (SAXException exception) {
      return false;
    }
  }

  /**
   * Pretty print the specified XML string.
   *
   * @param xml  XML string to pretty print. Non-null.
   * @return     A pretty printed version of the input, or the string
   *             itself if it doesn't form a well-formed XML.
   * @throws IllegalArgumentException  If xml is null.
   */
  public static String prettyPrint(String xml)
  {
    if (xml == null)
      throw new IllegalArgumentException("xml cannot be null");

    if (xml.trim().isEmpty())
      return "";

    try {
      DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document document = documentBuilder.parse(new InputSource(new StringReader(xml)));

      // Remove any whitespace between tags as it will
      // clutter up the prettyness
      XPath xPath = XPathFactory.newInstance().newXPath();
      NodeList emptyTextNodes = (NodeList) xPath.evaluate("//text()[normalize-space()='']", document, XPathConstants.NODESET);
      for (int i = 0; i < emptyTextNodes.getLength(); i++) {
        Node emptyTextNode = emptyTextNodes.item(i);
        emptyTextNode.getParentNode().removeChild(emptyTextNode);
      }

      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      transformerFactory.setAttribute("indent-number", 2);

      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty(OutputKeys.METHOD, "xml");
      transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

      StringWriter writer = new StringWriter();
      transformer.transform(new DOMSource(document), new StreamResult(writer));

      return writer.toString();
    }
    catch (ParserConfigurationException exception) {
      return xml;
    }
    catch (TransformerConfigurationException exception) {
      return xml;
    }
    catch (TransformerException exception) {
      return xml;
    }
    catch (SAXException exception) {
      return xml;
    }
    catch (XPathExpressionException exception) {
      return xml;
    }
    catch (IOException exception) {
      return xml;
    }
  }

  /**
   * Pretty print the specified XML sub tree.
   *
   * @param node  Root of sub tree to pretty print. Typically a document
   *              or an element.
   * @return      A pretty printed version of the input.
   * @throws IllegalArgumentException  If node is null.
   */
  public static String prettyPrint(Node node)
  {
    if (node == null)
      throw new IllegalArgumentException("node cannot be null");

    String xml = toString(node);
    return prettyPrint(xml);
  }

  /**
   * Return the parent element of the specified element.
   *
   * @param element  Element to get parent of. Non-null.
   * @return         The parent element. Null if none.
   * @throws IllegalArgumentException  If element is null.
   */
  public static Element getParent(Element element)
  {
    if (element == null)
      throw new IllegalArgumentException("element cannot be null");

    Node parent = element.getParentNode();
    return parent instanceof Element ? (Element) parent : null;
  }

  /**
   * Return all intermediate element children of the specified node.
   *
   * @param element  Element to get children of. Non-null
   * @return         Requested list of children. Never null.
   */
  public static List<Element> getChildren(Element element)
  {
    if (element == null)
      throw new IllegalArgumentException("element cannot be null");

    List<Element> children = new ArrayList<>();

    NodeList nodes = element.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      Node node = nodes.item(i);
      if (node instanceof Element)
        children.add((Element) node);
    }

    return children;
  }

  /**
   * Return a deep clone of the specified element.
   *
   * @param element  Element to clone. Non-null
   * @return         The new clone. Never null.
   * @throws IllegalArgumentException  If element is null.
   */
  public static Element clone(Element element)
  {
    if (element == null)
      throw new IllegalArgumentException("element cannot be null");

    return (Element) element.cloneNode(true);
  }


  /**
   * Add an element to the root of the specified document.
   * A document can only have one root, so if there is a root already
   * this is first removed.
   *
   * @param document     Document to add root to. Non-null.
   * @param elementName  Name of element to add. Non-null.
   * @return             The element added. Never null.
   * @throws IllegalArgumentException  If document or elementName is null.
   */
  public static Element addElement(Document document, String elementName)
  {
    if (document == null)
      throw new IllegalArgumentException("document cannot be null");

    if (elementName == null)
      throw new IllegalArgumentException("elementName cannot be null");

    // Remove any existing root node
    Element rootElement = document.getDocumentElement();
    if (rootElement != null)
      document.removeChild(rootElement);

    // Create the new root element and append
    rootElement = document.createElement(elementName);
    document.appendChild(rootElement);

    return rootElement;
  }

  /**
   * Add the specified element to the end of the list of children of the
   * given parent element,
   *
   * @param parentElement  Element to add to. Non-null.
   * @param element        Element to add. Non-null.
   * @throws IllegalArgumentException  If parentElement or element is null.
   */
  public static void addElement(Element parentElement, Element element)
  {
    if (parentElement == null)
      throw new IllegalArgumentException("parentElement cannot be null");

    if (element == null)
      throw new IllegalArgumentException("element cannot be null");

    parentElement.appendChild(element);
  }

  /**
   * Add an element with text content to the specified parent element.
   *
   * @param parentElement  Parent element to add to. Non-null.
   * @param elementName    Name of element to add. Non-null.
   * @param elementValue   Value of the added element. Null if empty.
   * @return               The added element. Never null.
   * @throws IllegalArgumentException  If parentElement or elementName is null.
   */
  public static Element addElement(Element parentElement, String elementName, Object elementValue)
  {
    if (parentElement == null)
      throw new IllegalArgumentException("parentElement cannot be null");

    if (elementName == null)
      throw new IllegalArgumentException("elementName cannot be null");

    Document document = parentElement.getOwnerDocument();
    if (document == null)
      throw new IllegalArgumentException("parentElement cannot be added to: " + parentElement);

    // Create the new element and append to its parent
    Element element = document.createElement(elementName);
    addElement(parentElement, element);

    // Add content
    if (elementValue != null)
      element.appendChild(document.createTextNode(elementValue.toString()));

    return element;
  }

  /**
   * Add a color element to the specified parent element.
   *
   * @param parentElement  Parent element to add to. Non-null.
   * @param elementName    Name of element to add. Non-null.
   * @param color          Color to add. Non-null.
   * @return               The added element. Never null.
   * @throws IllegalArgumentException  If parentElement, elementName or color is null.
   */
  public static Element addElement(Element parentElement, String elementName, Color color)
  {
    if (parentElement == null)
      throw new IllegalArgumentException("parentElement cannot be null");

    if (elementName == null)
      throw new IllegalArgumentException("elementName cannot be null");

    if (color == null)
      throw new IllegalArgumentException("color cannot be null");

    String text = color.getRed() + "," +
                  color.getGreen() + "," +
                  color.getBlue() + "," +
                  color.getAlpha();
    return addElement(parentElement, elementName, text);
  }

  /**
   * Add an empty element to the specified parent element.
   *
   * @param parentElement  Parent element to add to. Non-null.
   * @param elementName    Name of element to add. Non-null.
   * @return               The added element. Never null.
   * @throws IllegalArgumentException  If parentElement or elementName is null.
   */
  public static Element addElement(Element parentElement, String elementName)
  {
    if (parentElement == null)
      throw new IllegalArgumentException("parentElement cannot be null");

    if (elementName == null)
      throw new IllegalArgumentException("elementName cannot be null");

    return addElement(parentElement, elementName, (String) null);
  }

  /**
   * Insert an element as child of the specified parent element and in the
   * given index position.
   *
   * @param parentElement  Element to add to. Non-null.
   * @param element        Element to insert. Non-null.
   * @param index          Insertion index. 0 as first. If index is larger than the
   *                       current number of elements, it is added last. [0,&gt;.
   * @throws IllegalArgumentException  If parentElement or element is null, or
   *                       if index &lt; 0.
   */
  public static void insertElement(Element parentElement, Element element, int index)
  {
    if (parentElement == null)
      throw new IllegalArgumentException("parentElement cannot be null");

    if (element == null)
      throw new IllegalArgumentException("element cannot be null");

    if (index < 0)
      throw new IllegalArgumentException("Invalid index: " + index);

    List<Element> children = getChildren(parentElement);

    if (index >= children.size())
      parentElement.appendChild(element);
    else
      parentElement.insertBefore(element, children.get(index));
  }

  /**
   * Remove the specified element from its parent.
   *
   * @param element  Element to remove. Non-null.
   * @throws IllegalArgumentException  If element is null.
   */
  public static void removeElement(Element element)
  {
    if (element == null)
      throw new IllegalArgumentException("element cannot be null");

    Node parent = element.getParentNode();

    if (parent != null)
      parent.removeChild(element);
  }

  /**
   * Remove all children form the specified element.
   *
   * @param element  Element to remove from. Non-null.
   * @throws IllegalArgumentException  If element is null.
   */
  public static void removeChildren(Element element)
  {
    List<Element> children = getChildren(element);
    for (Element child : children)
      element.removeChild(child);
  }

  /**
   * Return a specified child element from the given element.
   * Only intermediate children are considered.
   *
   * @param element    Element to search. Non-null.
   * @param childName  Name of child element to find. Non-null.
   * @return           The requested child element. If there are more than one child elements
   *                   with the same name, the first one encountered is returned.
   *                   Null if not found.
   * @throws IllegalArgumentException  If element or childName is null.
   */
  public static Element getChild(Element element, String childName)
  {
    if (element == null)
      throw new IllegalArgumentException("element cannot be null");

    if (childName == null)
      throw new IllegalArgumentException("childName cannot be null");

    for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling())
      if (child instanceof Element && childName.equals(child.getNodeName()))
        return (Element) child;

    // Not found
    return null;
  }

  /**
   * Return a specific child element from the given element.
   * Search full depth, return the first one encountered.
   *
   * @param element    Root element of the search. Non-null.
   * @param childName  Name of child element to find. Non-null.
   * @return           The requested child element.
   *                   Null if not found.
   * @throws IllegalArgumentException  If element or childName is null.
   */
  public static Element findChild(Element element, String childName)
  {
    if (element == null)
      throw new IllegalArgumentException("element cannot be null");

    if (childName == null)
      throw new IllegalArgumentException("childName cannot be null");

    NodeList nodeList = element.getElementsByTagName(childName);
    return nodeList.getLength() == 0 ? null : (Element) nodeList.item(0);
  }

  /**
   * Return a specific child element from the given element.
   * Search full depth, return the last one encountered.
   *
   * @param element    Root element of the search. Non-null.
   * @param childName  Name of child element to find. Non-null.
   * @return           The requested child element.
   *                   Null if not found.
   * @throws IllegalArgumentException  If element or childName is null.
   */
  public static Element findLastChild(Element element, String childName)
  {
    if (element == null)
      throw new IllegalArgumentException("element cannot be null");

    if (childName == null)
      throw new IllegalArgumentException("childName cannot be null");

    NodeList nodeList = element.getElementsByTagName(childName);
    int nNodes = nodeList.getLength();

    return nNodes == 0 ? null : (Element) nodeList.item(nNodes - 1);
  }

  /**
   * Return the index of the specified child element in the given element.
   *
   * @param element       Parent element. Non-null.
   * @param childElement  Child element to get index of. Non-null.
   * @return              The requested index, or -1 if childElement is not
   *                      child of element.
   */
  public static int indexOf(Element element, Element childElement)
  {
    List<Element> children = getChildren(element);
    return children.indexOf(childElement);
  }

  /**
   * Return all children elements of the given name from the specified element.
   * Search full depth.
   *
   * @param element    Element to search. Non-null.
   * @param childName  Name of child elements to find. Non-null.
   * @return           The requested child elements. Never null.
   * @throws IllegalArgumentException  If element or childName is null.
   */
  public static List<Element> findChildren(Element element, String childName)
  {
    if (element == null)
      throw new IllegalArgumentException("element cannot be null");

    if (childName == null)
      throw new IllegalArgumentException("childName cannot be null");

    List<Element> elements = new ArrayList<>();

    NodeList nodeList = element.getElementsByTagName(childName);
    for (int i = 0; i < nodeList.getLength(); i++)
      elements.add((Element) nodeList.item(i));

    return elements;
  }

  /**
   * Return all children (full depth) of the specified element
   * in document order.
   *
   * @param element  Root element to find children of. Non-null.
   * @return         All children elements. Never null
   */
  public static List<Element> findChildren(Element element)
  {
    if (element == null)
      throw new IllegalArgumentException("element cannot be null");

    return findChildren(element, "*");
  }

  /**
   * Set the specified value in the given element.
   *
   * @param element  Element to set. Non-null.
   * @param value    Value to set. May be null, in case an empty string is set.
   * @throws IllegalArgumentException  If element is null.
   */
  public static void setValue(Element element, String value)
  {
    if (element == null)
      throw new IllegalArgumentException("element cannot be null");

    String text = value != null ? value : "";
    element.setTextContent(text);
  }

  /**
   * Return the value of the specified element.
   *
   * @param element       Element to get content from. Non-null.
   * @param defaultValue  Default value to return if content is absent. May be null.
   * @return              The requested content. May be null if defaultValue is null.
   * @throws IllegalArgumentException  If element is null.
   */
  public static String getValue(Element element, String defaultValue)
  {
    if (element == null)
      throw new IllegalArgumentException("element cannot be null");

    String content = element.getTextContent().trim();
    return !content.isEmpty() ? content : defaultValue;
  }

  /**
   * Return the value of the specified element.
   *
   * @param element  Element to get content from. Non-null.
   * @return         The requested content. Null if absent.
   * @throws IllegalArgumentException  If element is null.
   */
  public static String getValue(Element element)
  {
    if (element == null)
      throw new IllegalArgumentException("element cannot be null");

    return getValue(element, null);
  }

  /**
   * Return the text content of the child of the specified element.
   *
   * @param element       Parent element of child. Non-null.
   * @param childName     Name of child element to get text content of. Non-null.
   * @param defaultValue  Value to return if the child element is not found. May be null.
   * @return              The requested value, or the default value if not found.
   * @throws IllegalArgumentException  If element or childName is null.
   */
  public static String getChildValue(Element element, String childName, String defaultValue)
  {
    if (element == null)
      throw new IllegalArgumentException("element cannot be null");

    if (childName == null)
      throw new IllegalArgumentException("childName cannot be null");

    Element childElement = getChild(element, childName);
    return childElement != null ? childElement.getTextContent().trim() : defaultValue;
  }

  /**
   * Return the text content of the child of the specified element.
   *
   * @param element       Parent element of child. Non-null.
   * @param childName     Name of child element to get text content of. Non-null.
   * @return              The requested value, or null if element not found.
   * @throws IllegalArgumentException  If element or childName is null.
   */
  public static String getChildValue(Element element, String childName)
  {
    if (element == null)
      throw new IllegalArgumentException("element cannot be null");

    if (childName == null)
      throw new IllegalArgumentException("childName cannot be null");

    return getChildValue(element, childName, (String) null);
  }

  /**
   * Return the text content of the child of the specified element as a double.
   *
   * @param element       Parent element of child. Non-null.
   * @param childName     Name of child element to get text content of. Non-null.
   * @param defaultValue  Value to return if the child element is not found or
   *                      not of floating point format. May be null.
   * @return              The requested value, or the default value if not found
   *                      or not of floating point format.
   * @throws IllegalArgumentException  If element or childName is null.
   */
  public static Double getChildValue(Element element, String childName, Double defaultValue)
  {
    if (element == null)
      throw new IllegalArgumentException("element cannot be null");

    if (childName == null)
      throw new IllegalArgumentException("childName cannot be null");

    String text = getChildValue(element, childName);
    if (text == null || text.trim().isEmpty())
      return defaultValue;

    try {
      return Double.parseDouble(text);
    }
    catch (NumberFormatException exception) {
      return defaultValue;
    }
  }

  /**
   * Return the text content of the child of the specified element as an integer.
   *
   * @param element       Parent element of child. Non-null.
   * @param childName     Name of child element to get text content of. Non-null.
   * @param defaultValue  Value to return if the child element is not found
   *                      or not of integral format. May be null.
   * @return              The requested value, or the default value if not found
   *                      or not of integral format.
   * @throws IllegalArgumentException  If element or childName is null.
   */
  public static Integer getChildValue(Element element, String childName, Integer defaultValue)
  {
    if (element == null)
      throw new IllegalArgumentException("element cannot be null");

    if (childName == null)
      throw new IllegalArgumentException("childName cannot be null");

    String text = getChildValue(element, childName);
    if (text == null || text.trim().isEmpty())
      return defaultValue;

    try {
      return Integer.parseInt(text);
    }
    catch (NumberFormatException exception) {
      return defaultValue;
    }
  }

  /**
   * Return the text content of the child of the specified element as a long.
   *
   * @param element       Parent element of child. Non-null.
   * @param childName     Name of child element to get text content of. Non-null.
   * @param defaultValue  Value to return if the child element is not found
   *                      or not of integral format. May be null.
   * @return              The requested value, or the default value if not found
   *                      or not of integral format.
   * @throws IllegalArgumentException  If element or childName is null.
   */
  public static Long getChildValue(Element element, String childName, Long defaultValue)
  {
    if (element == null)
      throw new IllegalArgumentException("element cannot be null");

    if (childName == null)
      throw new IllegalArgumentException("childName cannot be null");

    String text = getChildValue(element, childName);
    if (text == null || text.trim().isEmpty())
      return defaultValue;

    try {
      return Long.parseLong(text);
    }
    catch (NumberFormatException exception) {
      return defaultValue;
    }
  }

  /**
   * Return the text content of the child of the specified element as an
   * ISO 8601 date.
   *
   * @param element       Parent element of child. Non-null.
   * @param childName     Name of child element to get text content of. Non-null.
   * @param defaultValue  Value to return if the child element is not found
   *                      or not of date format. May be null.
   * @return              The requested value, or the default value if not found
   *                      or not of date format.
   * @throws IllegalArgumentException  If element or childName is null.
   */
  public static Date getChildValue(Element element, String childName, Date defaultValue)
  {
    if (element == null)
      throw new IllegalArgumentException("element cannot be null");

    if (childName == null)
      throw new IllegalArgumentException("childName cannot be null");

    String text = getChildValue(element, childName);
    if (text == null || text.trim().isEmpty())
      return defaultValue;

    try {
      return ISO8601DateParser.parse(text);
    }
    catch (ParseException exception) {
      return defaultValue;
    }
  }

  /**
   * Return the text content of the child of the specified element as a string.
   *
   * @param element       Parent element of child. Non-null.
   * @param childName     Name of child element to get content of. Non-null.
   * @param defaultValue  Value to return if the child element is not found
   *                      or is not of correct type. May be null.
   * @return              The requested value, or the default value if not found
   *                      or not of correct value.
   * @throws IllegalArgumentException  If element or childName is null.
   */
  public static String getChildValue(Element element, String childName, Object defaultValue)
  {
    String text = getChildValue(element, childName);
    return text != null ? text : (defaultValue != null ? defaultValue.toString() : null);
  }

  /**
   * Return the text content of the child of the specified element as a boolean.
   *
   * @param element       Parent element of child. Non-null.
   * @param childName     Name of child element to get text content of. Non-null.
   * @param defaultValue  Value to return if the child element is not found.
   * @return              The requested value, or the default value if not found.
   * @throws IllegalArgumentException  If element or childName is null.
   */
  public static Boolean getChildValue(Element element, String childName, Boolean defaultValue)
  {
    if (element == null)
      throw new IllegalArgumentException("element cannot be null");

    if (childName == null)
      throw new IllegalArgumentException("childName cannot be null");

    String text = getChildValue(element, childName);
    if (text == null || text.trim().isEmpty())
      return defaultValue;

    return text.toLowerCase().equals("trie") || text.equals("1");
  }

  /**
   * Return the text content of the child of the specified element as a color.
   * The element is assumed to have the following format: "r,g,b[,a]" where
   * r, g, b, and a are [0,255].
   *
   * @param element       Parent element of child. Non-null.
   * @param childName     Name of child element to get text content of. Non-null.
   * @param defaultValue  Value to return if the child element is not found or not
   *                      of the expected format. May be null.
   * @return              The requested value, or the default value if not found
   *                      or not of the expected format.
   * @throws IllegalArgumentException  If element or childName is null.
   */
  public static Color getChildValue(Element element, String childName, Color defaultValue)
  {
    if (element == null)
      throw new IllegalArgumentException("element cannot be null");

    if (childName == null)
      throw new IllegalArgumentException("childName cannot be null");

    String text = getChildValue(element, childName);
    if (text == null || text.trim().isEmpty())
      return defaultValue;

    String[] tokens = text.split(",");
    if (tokens.length != 3 && tokens.length != 4) {
      return defaultValue;
    }

    String rString = tokens[0].trim();
    String gString = tokens[1].trim();
    String bString = tokens[2].trim();
    String aString = tokens.length == 4 ? tokens[3].trim() : "255";

    try {
      int r = Integer.parseInt(rString);
      int g = Integer.parseInt(gString);
      int b = Integer.parseInt(bString);
      int a = Integer.parseInt(aString);

      r = Math.min(Math.max(r, 0), 255);
      g = Math.min(Math.max(g, 0), 255);
      b = Math.min(Math.max(b, 0), 255);
      a = Math.min(Math.max(a, 0), 255);

      return new Color(r, g, b, a);
    }
    catch (NumberFormatException exception) {
      return defaultValue;
    }
  }

  /**
   * Add the specified attribute to the given element. If the attribute already
   * exists, its value is updated.
   *
   * @param element        Element of attribute to set. Non-null.
   * @param attributeName  Name of attribute to set. Non-null.
   * @param value          Value to set. May be null, in case an empty string is set.
   * @throws IllegalArgumentException  If element or attributeName is null.
   */
  public static void addAttribute(Element element, String attributeName, Object value)
  {
    if (element == null)
      throw new IllegalArgumentException("element cannot be null");

    if (attributeName == null)
      throw new IllegalArgumentException("attributeName cannot be null");

    String text = "";
    if (value == null)
      text = "";
    else if (value instanceof Color) {
      Color color = (Color) value;
      text = color.getRed() + "," + color.getGreen() + "," + color.getBlue() + "," + color.getAlpha();
    }
    else {
      text = value.toString();
    }

    element.setAttribute(attributeName, text);
  }

  /**
   * Set value of the specified attribute. If the element does not have the
   * specified attribute, this method has no effect, i.e. the attribute is not
   * <em>created</em> in this case.
   *
   * @param element        Element of attribute to set. Non-null.
   * @param attributeName  Name of attribute to set. Non-null.
   * @param value          Value to set. May be null, in case an empty string is set.
   * @throws IllegalArgumentException  If element or attributeName is null.
   */
  public static void setAttribute(Element element, String attributeName, Object value)
  {
    if (element == null)
      throw new IllegalArgumentException("element cannot be null");

    if (attributeName == null)
      throw new IllegalArgumentException("attributeName cannot be null");

    if (element.getAttributeNode(attributeName) == null)
      return;

    String text = "";
    if (value == null)
      text = "";
    else if (value instanceof Color) {
      Color color = (Color) value;
      text = color.getRed() + "," + color.getGreen() + "," + color.getBlue() + "," + color.getAlpha();
    }
    else {
      text = value.toString();
    }

    element.setAttribute(attributeName, text);
  }

  /**
   * Return the attribute of a specified element as a string.
   *
   * @param element        Element to find attribute of. Non-null.
   * @param attributeName  Name of attribute to get value of.
   * @param defaultValue   Default value to report if attribute is not found.
   * @return               The requested value or defaultValue if not found.
   * @throws IllegalArgumentException  If element or attributeName is null.
   */
  public static String getAttribute(Element element, String attributeName, String defaultValue)
  {
    if (element == null)
      throw new IllegalArgumentException("element cannot be null");

    if (attributeName == null)
      throw new IllegalArgumentException("attributeName cannot be null");

    String text = element.getAttribute(attributeName);
    return text != null && !text.trim().isEmpty() ? text : defaultValue;
  }

  /**
   * Return the attribute of a specified element as a string.
   *
   * @param element        Element to find attribute of. Non-null.
   * @param attributeName  Name of attribute to get value of.
   * @return               The requested value or null if the element or attribute is not found.
   * @throws IllegalArgumentException  If element or attributeName is null.
   */
  public static String getAttribute(Element element, String attributeName)
  {
    if (element == null)
      throw new IllegalArgumentException("element cannot be null");

    if (attributeName == null)
      throw new IllegalArgumentException("attributeName cannot be null");

    return getAttribute(element, attributeName, (String) null);
  }

  /**
   * Return the attribute of a specified element as an integer.
   *
   * @param element        Element to find attribute of. Non-null.
   * @param attributeName  Name of attribute to get value of.
   * @param defaultValue   Default value to report if attribute is not found
   *                       or not convertible to an integer. May be null.
   * @return The requested value, or defaultValue if not found or not an integer.
   * @throws IllegalArgumentException  If element or attributeName is null.
   */
  public static Integer getAttribute(Element element, String attributeName, Integer defaultValue)
  {
    if (element == null)
      throw new IllegalArgumentException("element cannot be null");

    if (attributeName == null)
      throw new IllegalArgumentException("attributeName cannot be null");

    String text = element.getAttribute(attributeName);
    if (text == null || text.trim().isEmpty())
      return defaultValue;

    try {
      return Integer.parseInt(text);
    }
    catch (NumberFormatException exception) {
      return defaultValue;
    }
  }

  /**
   * Return the attribute of a specific element as a long.
   *
   * @param element        Element to find attribute of. Non-null.
   * @param attributeName  Name of attribute to get value of.
   * @param defaultValue   Default value to report if attribute is not found
   *                       or not convertible to an integer. May be null.
   * @return The requested value, or defaultValue if not found or not an integer.
   * @throws IllegalArgumentException  If element or attributeName is null.
   */
  public static Long getAttribute(Element element, String attributeName, Long defaultValue)
  {
    if (element == null)
      throw new IllegalArgumentException("element cannot be null");

    if (attributeName == null)
      throw new IllegalArgumentException("attributeName cannot be null");

    String text = element.getAttribute(attributeName);
    if (text == null || text.trim().isEmpty())
      return defaultValue;

    try {
      return Long.parseLong(text);
    }
    catch (NumberFormatException exception) {
      return defaultValue;
    }
  }

  /**
   * Return the attribute of a specific element as a boolean.
   *
   * @param element        Element to find attribute of. Non-null.
   * @param attributeName  Name of attribute to get value of.
   * @param defaultValue   Default value to report if attribute is not found
   *                       or not convertible to a boolean. May be null.
   * @return The requested value, or defaultValue if not found or not a boolean.
   * @throws IllegalArgumentException  If element or attributeName is null.
   */
  public static Boolean getAttribute(Element element, String attributeName, Boolean defaultValue)
  {
    if (element == null)
      throw new IllegalArgumentException("element cannot be null");

    if (attributeName == null)
      throw new IllegalArgumentException("attributeName cannot be null");

    String text = element.getAttribute(attributeName);
    if (text == null || text.trim().isEmpty())
      return defaultValue;

    return text.toLowerCase().equals("trie") || text.equals("1");
  }

  /**
   * Return the attribute of a specific element as a long.
   *
   * @param element        Element to find attribute of. Non-null.
   * @param attributeName  Name of attribute to get value of.
   * @param defaultValue   Default value to report if attribute is not found
   *                       or not convertible to an integer. May be null.
   * @return The requested value, or defaultValue if not found or not an integer.
   * @throws IllegalArgumentException  If element or attributeName is null.
   */
  public static Double getAttribute(Element element, String attributeName, Double defaultValue)
  {
    if (element == null)
      throw new IllegalArgumentException("element cannot be null");

    if (attributeName == null)
      throw new IllegalArgumentException("attributeName cannot be null");

    String text = element.getAttribute(attributeName);
    if (text == null || text.trim().isEmpty())
      return defaultValue;

    try {
      return Double.parseDouble(text);
    }
    catch (NumberFormatException exception) {
      return defaultValue;
    }
  }

  /**
   * Return the attribute of a specific element as a string.
   *
   * @param element        Element to find attribute of. Non-null.
   * @param attributeName  Name of attribute to get value of.
   * @param defaultValue   Default value to report if attribute is not found.
   * @return The requested value, or defaultValue if not found.
   * @throws IllegalArgumentException  If element or attributeName is null.
   */
  public static String getAttribute(Element element, String attributeName, Object defaultValue)
  {
    if (element == null)
      throw new IllegalArgumentException("element cannot be null");

    if (attributeName == null)
      throw new IllegalArgumentException("attributeName cannot be null");

    String text = element.getAttribute(attributeName);
    return text != null ? text : (defaultValue != null ? defaultValue.toString() : null);
  }

  /**
   * Return the attribute of a specific element as a color.
   * Expected format: &lt;element color="r,g,b,a"&gt;. a is optional.
   *
   * @param element        Element to find attribute of. Non-null.
   * @param attributeName  Name of attribute to get value of.
   * @param defaultValue   Default value to report if attribute is not found
   *                       or not convertible to an integer. May be null.
   * @return The requested value, or defaultValue if not found or not in correct format.
   * @throws IllegalArgumentException  If element or attributeName is null.
   */
  public static Color getAttribute(Element element, String attributeName, Color defaultValue)
  {
    if (element == null)
      throw new IllegalArgumentException("element cannot be null");

    if (attributeName == null)
      throw new IllegalArgumentException("attributeName cannot be null");

    String text = element.getAttribute(attributeName);
    if (text == null || text.trim().isEmpty())
      return defaultValue;

    String[] rgba = text.split(",");
    if (rgba.length != 3 && rgba.length != 4) {
      return defaultValue;
    }

    //
    // Red
    //
    String rText = rgba[0];
    int r = 0;
    try {
      r = Integer.parseInt(rText);
      if (r < 0 || r > 255) {
        return defaultValue;
      }
    }
    catch (NumberFormatException exception) {
      return defaultValue;
    }

    //
    // Green
    //
    String gText = rgba[1];
    int g = 0;
    try {
      g = Integer.parseInt(gText);
      if (g < 0 || g > 255) {
        return defaultValue;
      }
    }
    catch (NumberFormatException exception) {
      return defaultValue;
    }

    //
    // Blue
    //
    String bText = rgba[2];
    int b = 0;
    try {
      b = Integer.parseInt(bText);
      if (b < 0 || b > 255) {
        return defaultValue;
      }
    }
    catch (NumberFormatException exception) {
      return defaultValue;
    }

    //
    // Alpha
    //
    String aText = rgba.length == 4 ? rgba[3] : "255";
    int a = 0;
    try {
      a = Integer.parseInt(aText);
      if (a < 0 || a > 255) {
        return defaultValue;
      }
    }
    catch (NumberFormatException exception) {
      return defaultValue;
    }

    return new Color(r, g, b, a);
  }

  /**
   * Return the attribute of a specified child element as a string.
   *
   * @param rootElement    Element to search from. Non-null. Non-null.
   * @param elementName    Name of element to look for. Non-null.
   * @param attributeName  Name of attribute to get value of. Non-null.
   * @param defaultValue   Default value to report if attribute is not found.
   * @return The requested value, or defaultValue if not found.
   * @throws IllegalArgumentException  If rootElement, elementName or attributeName is null.
   */
  public static String getChildAttribute(Element rootElement, String elementName, String attributeName, String defaultValue)
  {
    if (rootElement == null)
      throw new IllegalArgumentException("rootElement cannot be null");

    if (elementName == null)
      throw new IllegalArgumentException("elementName cannot be null");

    if (attributeName == null)
      throw new IllegalArgumentException("attributeName cannot be null");

    Element element = getChild(rootElement, elementName);
    return element != null ? getAttribute(element, attributeName, defaultValue) : defaultValue;
  }

  /**
   * Return the attribute of a specified child element as a string.
   *
   * @param rootElement    Element to search from. Non-null. Non-null.
   * @param elementName    Name of element to look for. Non-null.
   * @param attributeName  Name of attribute to get value of. Non-null.
   * @return The requested value, or null if not found.
   * @throws IllegalArgumentException  If rootElement, elementName or attributeName is null.
   */
  public static String getChildAttribute(Element rootElement, String elementName, String attributeName)
  {
    if (rootElement == null)
      throw new IllegalArgumentException("rootElement cannot be null");

    if (elementName == null)
      throw new IllegalArgumentException("elementName cannot be null");

    if (attributeName == null)
      throw new IllegalArgumentException("attributeName cannot be null");

    return getChildAttribute(rootElement, elementName, attributeName, (String) null);
  }

  /**
   * Return the attribute of a specified element as an integer.
   *
   * @param rootElement    Element to search from. Non-null. Non-null.
   * @param elementName    Name of element to look for. Non-null.
   * @param attributeName  Name of attribute to get value of. Non-null.
   * @param defaultValue   Default value to report if attribute is not found.
   * @return The requested value, or defaultValue if not found or of wrong type.
   * @throws IllegalArgumentException  If rootElement, elementName or attributeName is null.
   */
  public static Integer getChildAttribute(Element rootElement, String elementName, String attributeName, Integer defaultValue)
  {
    if (rootElement == null)
      throw new IllegalArgumentException("rootElement cannot be null");

    if (elementName == null)
      throw new IllegalArgumentException("elementName cannot be null");

    if (attributeName == null)
      throw new IllegalArgumentException("attributeName cannot be null");

    Element element = getChild(rootElement, elementName);
    return element != null ? getAttribute(element, attributeName, defaultValue) : defaultValue;
  }

  /**
   * Return the attribute of a specified element as a long integer.
   *
   * @param rootElement    Element to search from. Non-null. Non-null.
   * @param elementName    Name of element to look for. Non-null.
   * @param attributeName  Name of attribute to get value of. Non-null.
   * @param defaultValue   Default value to report if attribute is not found.
   * @return The requested value, or defaultValue if not found or of wrong type.
   * @throws IllegalArgumentException  If rootElement, elementName or attributeName is null.
   */
  public static Long getChildAttribute(Element rootElement, String elementName, String attributeName, Long defaultValue)
  {
    if (rootElement == null)
      throw new IllegalArgumentException("rootElement cannot be null");

    if (elementName == null)
      throw new IllegalArgumentException("elementName cannot be null");

    if (attributeName == null)
      throw new IllegalArgumentException("attributeName cannot be null");

    Element element = getChild(rootElement, elementName);
    return element != null ? getAttribute(element, attributeName, defaultValue) : defaultValue;
  }

  /**
   * Return the attribute of a specified element as a double.
   *
   * @param rootElement    Element to search from. Non-null. Non-null.
   * @param elementName    Name of element to look for. Non-null.
   * @param attributeName  Name of attribute to get value of. Non-null.
   * @param defaultValue   Default value to report if attribute is not found.
   * @return The requested value, or defaultValue if not found or of wrong type.
   * @throws IllegalArgumentException  If rootElement, elementName or attributeName is null.
   */
  public static Double getChildAttribute(Element rootElement, String elementName, String attributeName, Double defaultValue)
  {
    if (rootElement == null)
      throw new IllegalArgumentException("rootElement cannot be null");

    if (elementName == null)
      throw new IllegalArgumentException("elementName cannot be null");

    if (attributeName == null)
      throw new IllegalArgumentException("attributeName cannot be null");

    Element element = getChild(rootElement, elementName);
    return element != null ? getAttribute(element, attributeName, defaultValue) : defaultValue;
  }

  /**
   * Return the attribute of a specified element as a string.
   *
   * @param rootElement    Element to search from. Non-null. Non-null.
   * @param elementName    Name of element to look for. Non-null.
   * @param attributeName  Name of attribute to get value of. Non-null.
   * @param defaultValue   Default value to report if attribute is not found.
   * @return The requested value, or defaultValue if not found or of wrong type.
   * @throws IllegalArgumentException  If rootElement, elementName or attributeName is null.
   */
  public static String getChildAttribute(Element rootElement, String elementName, String attributeName, Object defaultValue)
  {
    if (rootElement == null)
      throw new IllegalArgumentException("rootElement cannot be null");

    if (elementName == null)
      throw new IllegalArgumentException("elementName cannot be null");

    if (attributeName == null)
      throw new IllegalArgumentException("attributeName cannot be null");

    Element element = getChild(rootElement, elementName);
    return element != null ? getAttribute(element, attributeName, defaultValue) : (defaultValue != null ? defaultValue.toString() : null);
  }

  /**
   * Return the attribute of a specified element as a color.
   *
   * @param rootElement    Element to search from. Non-null. Non-null.
   * @param elementName    Name of element to look for. Non-null.
   * @param attributeName  Name of attribute to get value of. Non-null.
   * @param defaultValue   Default value to report if attribute is not found.
   * @return The requested value, or defaultValue if not found or not a color.
   * @throws IllegalArgumentException  If rootElement, elementName or attributeName is null.
   */
  public static Color getChildAttribute(Element rootElement, String elementName, String attributeName, Color defaultValue)
  {
    if (rootElement == null)
      throw new IllegalArgumentException("rootElement cannot be null");

    if (elementName == null)
      throw new IllegalArgumentException("elementName cannot be null");

    if (attributeName == null)
      throw new IllegalArgumentException("attributeName cannot be null");

    Element element = getChild(rootElement, elementName);
    return element != null ? getAttribute(element, attributeName, defaultValue) : defaultValue;
  }

  /**
   * Transform an XML node (document or element typically) to its equivalent
   * XML string.
   *
   * @param node  Node to get as string. Non-null.
   * @return      The associated string. Never null.
   * @throws IllegalArgumentException  If node is null.
   */
  public static String toString(Node node)
  {
    if (node == null)
      throw new IllegalArgumentException("node cannot be null");

    TransformerFactory transformerFactory = TransformerFactory.newInstance();

    try {
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      StringWriter writer = new StringWriter();
      transformer.transform(new DOMSource(node), new StreamResult(writer));

      return writer.getBuffer().toString();
    }
    catch (TransformerConfigurationException exception) {
      return "";
    }
    catch (TransformerException exception) {
      return "";
    }
  }
}
