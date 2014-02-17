package ldif.modules.sieve.fusion.policylearner;

/*
 * Copyright 2013 University of Mannheim
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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/* Collection of methods for working with xml */
public class XMLUtils
{
    /* --- XML UTILS --- */
    // Get value of the first node on a list by path
    public static String getAttrValue(Document doc, String path, String attrName)
    {
        return getAttrValue(getNodeList(doc, path + getAttrPath(attrName)).item(0), attrName);
    }
    // Get attribute value of the first node on a list by path
    public static String getValue(Document doc, String path)
    {
        return getNodeList(doc,path).item(0).getTextContent();
    }
    // Get attribute value within a node
    public static String getAttrValue(Node node, String attrName)
    {
        if (node == null) return null;
        if (node.getAttributes() == null) return null;
        if (node.getAttributes().getNamedItem(attrName) == null) return null;

        return node.getAttributes().getNamedItem(attrName).getTextContent();
    }
    // Get node list by path (within node) from a node of a loaded document
    public static NodeList getNodeList(Node node, String path)
    {
        try
        {
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xpath = xPathFactory.newXPath();
            XPathExpression expr;
            expr = xpath.compile(path);
            NodeList list = (NodeList) expr.evaluate(node, XPathConstants.NODESET);

            return list;
        }
        catch (XPathExpressionException e)
        {
            e.printStackTrace();
        }
        return null;
    }
    public static NodeList getDocNodeList(Document xmlDoc, String path)
    {
        return getNodeList(xmlDoc, path);
    }
    public static String getAttrPath(String attrName)
    {
        return "[@"+attrName+"]";
    }
    // Print a node to a file (fn is a path); if fn is null, print into System.out
    // can be called with node being the whole xml document
    public static void printNode(Node node)
    {
        printNode(node, null);
    }
    public static void printNode(Node node, String fn)
    {
        try
        {
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource source = new DOMSource(node);
            PrintStream out = System.out;
            if (fn != null)
            {
                File file = new File(fn);
                if(!file.getParentFile().exists()) file.getParentFile().mkdirs();

                out = new PrintStream(new FileOutputStream(fn));
            }
            StreamResult result = new StreamResult(out);
            transformer.transform(source, result);
            out.close();
      }
        catch (TransformerConfigurationException e)
        {
            e.printStackTrace();
        }
        catch (TransformerException e)
        {

            e.printStackTrace();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static String printNodeToStr(Node node)
    {
        String output = null;
        try
        {
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(node), new StreamResult(writer));

            output = writer.getBuffer().toString().replaceAll("\n|\r", "");
        }
        catch (TransformerConfigurationException e)
        {
            e.printStackTrace();
        }
        catch (TransformerException e)
        {

            e.printStackTrace();
        }
        return output;
    }
    // Create a new document document; returns root node
    public static Node createDocument(String rootElementName)
    {
        try
        {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder;
            docBuilder = docFactory.newDocumentBuilder();

            Document doc = docBuilder.newDocument();

            Element rootElement = doc.createElement(rootElementName);
            doc.appendChild(rootElement);

            return rootElement;
        }
        catch (ParserConfigurationException e)
        {
            e.printStackTrace();
        }
        return null;
    }
    // Add a new element to a node, with one or two attributes
    public static Node addNewElementWithAttribute(Node node, String elementName, String attrName1, String attrValue1, String attrName2, String attrValue2)
    {
        Document xmlDoc = node.getOwnerDocument();
        // element
        Node newVal = xmlDoc.createElement(elementName);
        // attribute
        addAttribute(xmlDoc, newVal, attrName1, attrValue1);
        if (attrName2 != null) addAttribute(xmlDoc, newVal, attrName2, attrValue2);
        // add to node
        node.appendChild(newVal);

        return newVal;
    }
    // Add a new element to a node, with one or two attributes
    public static Node addNewElementWithAttribute(Node node, String elementName, String attrName, String attrValue)
    {
        return addNewElementWithAttribute(node, elementName, attrName, attrValue, null, null);
    }

    // Add an attribute to a node
    public static void addAttribute(Document xmlDoc, Node node, String attrName, String attrValue)
    {
        Attr newAtt = xmlDoc.createAttribute(attrName);
        newAtt.setNodeValue(attrValue);
        NamedNodeMap attrs = node.getAttributes();
        attrs.setNamedItem(newAtt);
    }
    // Add an attribute to a node
    public static void addAttribute(Node node, String attrName, String attrValue)
    {
        Document xmlDoc = node.getOwnerDocument();
        addAttribute(xmlDoc, node, attrName, attrValue);
    }

    // Add a new element to a node, with text value
    public static void addNewElementWithValue(Node node, String elementName, String elementValue)
    {
        Document xmlDoc = node.getOwnerDocument();
        // element
        Node newVal = xmlDoc.createElement(elementName);
        Node newValText = xmlDoc.createTextNode(elementValue);
        newVal.appendChild(newValText);
        // add to node
        node.appendChild(newVal);
    }

    // Add a new element to a node, with text value, one intermediate level present
    public static void addNewElementWithValue2(Node node, String elementName1, String elementName2, String elementValue)
    {
        Document xmlDoc = node.getOwnerDocument();
        // element
        Node newVal = xmlDoc.createElement(elementName1);
        Node n = node.appendChild(newVal);
        Node newVal2 = xmlDoc.createElement(elementName2);
        Node n2 = n.appendChild(newVal2);
        Node newValText = xmlDoc.createTextNode(elementValue);
        newVal2.appendChild(newValText);
        // add to node
        node.appendChild(newVal);
    }
    // Add a new element to a node, with text value, two intermediate levels present
    public static void addNewElementWithValue3(Node node, String elementName1, String elementName2, String elementName3, String elementValue)
    {
        Document xmlDoc = node.getOwnerDocument();
        // element
        Node newVal = xmlDoc.createElement(elementName1);
        Node n = node.appendChild(newVal);
        Node newVal2 = xmlDoc.createElement(elementName2);
        Node n2 = n.appendChild(newVal2);
        Node newVal3 = xmlDoc.createElement(elementName3);
        Node n3 = n2.appendChild(newVal3);
        Node newValText = xmlDoc.createTextNode(elementValue);
        newVal3.appendChild(newValText);
        // add to node
        node.appendChild(newVal);
    }
}
