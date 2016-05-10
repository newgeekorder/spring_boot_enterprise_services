package umg;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class XmlHelper  {

    String xmlReferenceFile;
    protected DocumentBuilderFactory xmlFact;
    DocumentBuilder builder;
    protected XPathFactory xpathFact;
    protected XPath xpath;
    private Document doc;
    private static Log logger = LogFactory.getLog(XmlHelper.class);

    // Static constants
    public static String RMS = "RMS";
    public static String BSS = "BSS";

    // GET XML REFERENCE FILES
    public XmlHelper() {
    }

    public XmlHelper(String xmlPayload) {
        parse(xmlPayload);
    }

    public static String getStandardTestDate() {
        return "1970-01-01T00:00:00+01:00";
    }

    // BUILD XML
    protected void build(String xmlFileName) {
        try {
            xmlReferenceFile = xmlFileName;
            xmlFact = DocumentBuilderFactory.newInstance();
            xmlFact.setNamespaceAware(false);
            builder = xmlFact.newDocumentBuilder();
            xpathFact = XPathFactory.newInstance();
            xpath = xpathFact.newXPath();
        } catch (ParserConfigurationException e) {
            debug("HarpXmlHelper | Exception occured in build method ");
            e.printStackTrace();
        }
    }

    // PARSING FUNCTIONS
    public void parse(String xml) {
        InputStream inputStream = IOUtils.toInputStream(xml);
        parse(inputStream);
    }

    public void parse(InputStream xml) {
        try {
            xmlFact = DocumentBuilderFactory.newInstance();
            xmlFact.setNamespaceAware(false);
            builder = xmlFact.newDocumentBuilder();
            xpathFact = XPathFactory.newInstance();
            xpath = xpathFact.newXPath();
            doc = builder.parse(xml);
        } catch (Exception e) {
            e.printStackTrace();
            doc = null;
        }
    }

    // CHECK XPATH ITEMS
    // convert this to private
    public Boolean getXpathExists(String path, Document doc) {
        try {
            debug("HarpXmlHelper | Entered getXpathExists[String,Document] method");
            Boolean node = (Boolean) xpath.compile(path).evaluate(doc, XPathConstants.BOOLEAN);
            if (node != null) {
                return node;
            }
        } catch (Exception e) {
            debug("HarpXmlHelper | Exception occured in getXpathExists[String,Document] method ");
            logger.warn("--- Xpath not found -- " + e.getMessage());
        }
        return false;
    }

    /* The below 2 methods are same */
    public Boolean getXpathExists(String path) {
        debug("HarpXmlHelper | Entered getXpathExists[String] method");
        return getXpathExists(path, doc);
    }

    public Boolean checkXpathItem(String path) {
        debug("HarpXmlHelper | Entered checkXpathItem[String] method");
        String xpathValue = getXpathItem(path);
        if (StringUtils.isNotEmpty(xpathValue))
            return true;

        return false;
    }

    public int getXpathCount(String path){
        try{
            Double result = (Double) javax.xml.xpath.XPathFactory.newInstance().newXPath().evaluate("count(" + path + ")", doc, XPathConstants.NUMBER);
            return result.intValue();
        } catch (Exception e){
            System.err.println("Error occured");
            return 0;
        }

    }

    // GET XPATH ITEM value
    public String getXpathItem(String path) {
        List<String> values = getXpathList(path,doc);
        if (values.size() > 0)
            return StringEscapeUtils.unescapeXml(values.get(0)).trim();
        else
            return null;
    }

    public NodeList getXpathNodes(String path) {
        try {
//            org.w3c.dom.NodeList nodes = (NodeList) javax.xml.xpath.XPathFactory.newInstance().newXPath()
//                    .evaluate(path, doc, XPathConstants.NODESET);
            NodeList nodes = (NodeList) xpath.compile(path).evaluate(doc, XPathConstants.NODESET);
            return nodes;
        } catch (Exception e) {
            e.printStackTrace()
            return null;
        }
    }

    public ArrayList<String> getXpathList(String path) {
        return getXpathList(path, doc);
    }

    public ArrayList<String> getXpathList(String path, Document doc) {
        logger.debug("looking up: " + path);
        ArrayList<String> data = new ArrayList<String>();
        try {
            NodeList nodes = (NodeList) xpath.compile(path).evaluate(doc, XPathConstants.NODESET);
            if (nodes.getLength() > 0) {
                for (int i = 0; i < nodes.getLength(); i++) {
                    String value = nodes.item(i).getTextContent();
                    if (StringUtils.isNotEmpty(value)) {
                        data.add(value);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    // SET XPATH VALUE
    // public boolean setValue(String xpathStr, String value) {
    // return setValue(xpathStr, value);
    // }

    public boolean setValue(String path, String value) {
        try {
            NodeList nodes = (NodeList) xpath.compile(path).evaluate(doc, XPathConstants.NODESET);
            if (nodes != null && nodes.getLength() > 0) {
                nodes.item(0).setTextContent(value);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean setAllValues(String path, String value) {
        try {
            NodeList nodes = (NodeList) xpath.compile(path).evaluate(doc, XPathConstants.NODESET);
            if (nodes != null && nodes.getLength() > 0) {
                for (int i=0; i< nodes.getLength(); i++) {
                    Node item = nodes.item(i);
                    item.setTextContent(value);
                }
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // TODO - Delete Existing FUNCTION. Replace with two given below
    public void addCriteria(String elementName, String xPathValue, String attributeName, String attrValue) {
        try {
            debug("HarpXmlHelper | Entered addCriteria[String,String,String,String] method");
            Element e = doc.createElement(elementName);
            e.setAttribute(attributeName, attrValue);
            Node node = (Node) xpath.compile(xPathValue).evaluate(doc, XPathConstants.NODE);
            node.appendChild(e);
        } catch (Exception e) {
            debug("HarpXmlHelper | Exception occured in addCriteria[String,String,String,String] method ");
            e.printStackTrace();
        }
    }

    /**
     * This creates a new child node within the parentnode specified.
     *
     * @param elementName
     *            new child element name
     * @param elementValue
     *            new child element value (optional filed)
     * @param parentNodeName
     *            the parent node name (complete XPATH)
     * @param attributeNameList
     *            list of attributes to be added to the child node (optional
     *            field)
     * @param attributeValueList
     *            values of the attributes to be added to the child node
     * @return returns the parent node with the child node added
     */
    public Node addNode(String elementName, String elementValue, String parentNodeName, List attributeNameList,
                        List attributeValueList) {
        Element newChildNode = null;
        try {
            debug("HarpXmlHelper | Entered addNode[String,String,String,String[],String[]] method");
            newChildNode = doc.createElement(elementName);
            if (attributeNameList != null) {
                int i = 0;
                for (String attribute : attributeNameList) {
                    newChildNode.setAttribute(attribute, attributeValueList[i]);
                    i++;
                }
            }
            if (StringUtils.isNotEmpty(elementValue)) {
                newChildNode.setTextContent(elementValue);
            }
            addNewChildNode(newChildNode, parentNodeName);
        } catch (Exception e) {
            debug("HarpXmlHelper | Exception occured in addNode[String,String,String,String[],String[]] method ");
            e.printStackTrace();
        }
        return newChildNode;
    }

    // this is redundant .. use addNode method above
    public Node addNodeMultipleAttributes(String elementName, String parentNodeName, List attributeNameList,
                                          List attributeValueList) {
        debug("HarpXmlHelper | Entered addNodeMultipleAttributes[String,String,String[],String[]] method");
        return addNode(elementName, null, parentNodeName, attributeNameList, attributeValueList);
    }

    /**
     * This creates a new child node and sets the value within the parentnode
     * specified.
     *
     * @param elementName
     *            new child element name
     * @param elementValue
     *            new child element value (optional filed)
     * @param parentNodeName
     *            the parent node name (complete XPATH)
     * @return returns the parent node with the child node added
     */
    public Node addNode(String elementName, String elementValue, String parentNodeName) {
        debug("HarpXmlHelper | Entered addNode[String,String,String] method");
        return addNodeWithAttribute(elementName, elementValue, parentNodeName, null, null);
    }

    /**
     * This creates a new child node within the parentnode specified.
     *
     * @param elementName
     *            new child element name
     * @param elementValue
     *            new child element value (optional filed)
     * @param parentNodeName
     *            the parent node name (complete XPATH)
     * @param attributeName
     *            attribute to be added to the child node (optional field)
     * @param attributeValu
     *            value of the attribute to be added to the child node
     * @return returns the parent node with the child node added
     */
    public Node addNodeWithAttribute(String elementName, String elementValue, String parentNodeName,
                                     String attributeName, String attributeValue) {
        debug("HarpXmlHelper | Entered addNodeWithAttribute[String,String,String,String,String] method");
        String[] attrbuteList = null;
        String[] attrbuteValueList = null;
        if (StringUtils.isNotEmpty(attributeName)) {
            attrbuteList = [ attributeName ];
            attrbuteValueList =[ attributeValue ];
        }
        return addNode(elementName, elementValue, parentNodeName, attrbuteList, attrbuteValueList);
    }

    // this method should be removed
    public Node addNode(String elementName, String parentNodeName, String attributeName, String value) {
        Element ele = null;
        try {
            debug("HarpXmlHelper | Entered addNode[String,String,String,String] method");
            ele = doc.createElement(elementName);
            if (StringUtils.isNotEmpty(attributeName)) {
                ele.setAttribute(attributeName, value);
            } else if (StringUtils.isNotEmpty(value)) {
                ele.setTextContent(value);
            }
            Node node = (Node) xpath.compile(parentNodeName).evaluate(doc, XPathConstants.NODE);
            if (node != null)
                node.appendChild(ele);
        } catch (Exception e) {
            debug("HarpXmlHelper | Exception occured in addNode[String,String,String,String] method ");
            e.printStackTrace();
        }
        return ele;
    }

    /**
     * Creates a new element in the document. The positioning of this node in
     * the dom tree, should be handled separately.
     *
     * @param elementName
     * @return
     */
    public Element createElement(String elementName) {
        debug("HarpXmlHelper | Entered createElement[String] method");
        Element element = doc.createElement(elementName);
        return element;
    }

    /**
     * Creates a newnode within the parentnode, but before the reference child
     * node.
     *
     * @param newNode
     *            the new Node to be created
     * @param parentNodeName
     *            the parentNode name
     * @param referenceNodeName
     *            reference child node name (within parent node)
     * @return parent node with the new child node
     * @throws XPathExpressionException
     */
    public Node insertNewNodeBeforeReferenceNode(Node newNode, String parentNodeName, String referenceNodeName) {
        Node parentNode = null;
        try {
            debug("HarpXmlHelper | Entered insertNewNodeBeforeReferenceNode method");
            parentNode = (Node) xpath.compile(parentNodeName).evaluate(doc, XPathConstants.NODE);
            Node referenceNode = (Node) xpath.compile(referenceNodeName).evaluate(doc, XPathConstants.NODE);
            parentNode.insertBefore(newNode, referenceNode);
        } catch (XPathExpressionException e) {
            debug("HarpXmlHelper | Exception occured in insertNewNodeBeforeReferenceNode method ");
            e.printStackTrace();
        }
        return parentNode;
    }

    /**
     * Creates a newnode within the parentnode.
     *
     * @param newNode
     *            the name of the new Node to be created
     * @param parentNodeName
     *            the parentNode name
     * @return parent node with the new child node
     * @throws XPathExpressionException
     */
    public Node addNewChildNode(String newChildName, String parentNodeName) {
        debug("HarpXmlHelper | Entered addNewChildNode[String,String] method");
        return addNewChildNode(newChildName, parentNodeName, null);
    }

    /**
     * Creates a newnode within the parentnode ad sets the node value.
     *
     * @param newNode
     *            the name of the new Node to be created
     * @param parentNodeName
     *            the parentNode name
     * @param nodeValue
     *            value of the node
     * @return parent node with the new child node
     * @throws XPathExpressionException
     */
    public Node addNewChildNode(String newChildName, String parentNodeName, String nodeValue) {
        debug("HarpXmlHelper | Entered addNewChildNode[String,String,String] method");
        Node newChildNode = doc.createElement(newChildName);
        return addNewChildNode(newChildNode, parentNodeName, nodeValue);
    }

    /**
     * Creates a newnode within the parentnode.
     *
     * @param newNode
     *            the new Node to be created
     * @param parentNodeName
     *            the parentNode name (complete XPATH)
     * @return parent node with the new child node
     * @throws XPathExpressionException
     */
    public Node addNewChildNode(Node newChildNode, String parentNodeName) {
        debug("HarpXmlHelper | Entered addNewChildNode[Node,String] method");
        return addNewChildNode(newChildNode, parentNodeName, null);
    }

    /**
     * Creates a newnode within the parentnode and sets the value of the node.
     *
     * @param newNode
     *            the new Node to be created
     * @param parentNodeName
     *            the parentNode name (complete XPATH)
     * @param nodeValue
     *            value of the node
     * @return parent node with the new child node
     * @throws XPathExpressionException
     */
    public Node addNewChildNode(Node newChildNode, String parentNodeName, String nodeValue) {
        debug("HarpXmlHelper | Entered addNewChildNode[Node,String,String] method");
        if (StringUtils.isNotEmpty(nodeValue)) {
            newChildNode.setTextContent(nodeValue);
        }
        Node parentNode = null;
        try {
            parentNode = (Node) xpath.compile(parentNodeName).evaluate(doc, XPathConstants.NODE);
            if (parentNode != null)
                parentNode.appendChild(newChildNode);
        } catch (XPathExpressionException e) {
            debug("HarpXmlHelper | Exception occured in addNewChildNode[Node,String,String] method ");
            e.printStackTrace();
        }
        return parentNode;
    }

    /**
     * This method creates <ez:Code>somecode</ez:Code>
     * <ez:description>somemessage</ez:description>
     * <p/>
     * under <ez:message> node of AvailibilityCheckReponse.xml.
     *
     * @param attrNode
     *            The node
     * @param attrValue1
     *            The attribute value.
     */
    // this is a dupliate method of addNewChildNode --- remove this
    public void addCriteria(String attrNode, String attrValue1, String xPath) {
        debug("HarpXmlHelper | Entered addCriteria[String,String] method");
        addNewChildNode(attrNode, xPath, attrValue1);
    }

    protected InputStream getContents() {
        return this.getClass().getClassLoader().getResourceAsStream(xmlReferenceFile);
    }

    protected InputStream getContents(String xmlFilePath) {
        try {
            debug("HarpXmlHelper | Entered getContents[String] method");
            return IOUtils.toInputStream(FileUtils.readFileToString(new File(xmlFilePath)));
        } catch (Exception e) {
            debug("HarpXmlHelper | Exception occured in getContents[String] method ");
            e.printStackTrace();
            return null;
        }
    }

    public static String getDocumentToString(Document doc) {
        try {
            DOMSource domSource = new DOMSource(doc);
            StreamResult xmlOutput = new StreamResult(new StringWriter());

            // Configure transformer
            Transformer transformer = TransformerFactory.newInstance().newTransformer(); // An
            // identity
            // transformer
            // transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,
            // "testing.dtd");

            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(domSource, xmlOutput);

            return xmlOutput.getWriter().toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public String getDocumentToSingleLine() {
        try {

            String xml = getDocumentToString();
            xml = xml.replaceAll("[\n\r]", "");
            xml = xml.replaceAll("\\s+<", "");
            return xml;

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

    }

    public Document getDoc() {
        return doc;
    }

    public static String prettyPrint(Document doc) {
        System.out.println(getDocumentToString(doc));
        return getDocumentToString(doc);
    }

    public void prettyPrint() {
        System.out.println(getDocumentToString(doc));
    }

    /**
     * This method adds the code and description tags under message node.
     *
     * @param xPath
     *            The Xpath
     */
    public void dynamicWarningMsgBuilder(String xPath, Map<String, String> warningMessagesMap) {
        if (null != warningMessagesMap && !warningMessagesMap.isEmpty()) {
            List<String> keyList = new ArrayList<String>(warningMessagesMap.keySet());
            Node node = null;
            Element element = null;
            for (int i = 0; i < keyList.size(); i++) {
                node = addNewChildNode("ez:Message", xPath);
                element = getDoc().createElement("ez:Code");
                element.setTextContent(keyList.get(i));
                node.appendChild(element);
                element = getDoc().createElement("ez:Description");
                element.setTextContent(warningMessagesMap.get(keyList.get(i)));
                node.appendChild(element);
            }
        }
    }

    // remove this -- duplicate of getXpathList
    public NodeList getNodeList(String path) {
        debug("HarpXmlHelper | Entered getNodeList method");
        NodeList nodes = null;
        try {
            nodes = (NodeList) xpath.compile(path).evaluate(doc, XPathConstants.NODESET);
        } catch (Exception e) {
            debug("HarpXmlHelper | Exception occured in getNodeList method ");
            e.printStackTrace();
        }
        return nodes;
    }

    /**
     * Get the value of the child node
     *
     * @param path
     *            name of child node
     * @param parentNode
     *            parent node, the child belongs to
     * @return value of child node
     */
    public String getChildNodeValue(String path, Node parentNode) {
        debug("HarpXmlHelper | Entered getChildNodeValue method");
        String value = null;
        try {
            Node childNode = (Node) xpath.evaluate(path, parentNode, XPathConstants.NODE);
            if (childNode != null)
                value = childNode.getTextContent();
        } catch (Exception e) {
            debug("HarpXmlHelper | Exception occured in getChildNodeValue method");
            e.printStackTrace();
        }
        return StringUtils.defaultString(value);
    }

    public List<String> getChildNodeValues(String path, Node parentNode) {
        List values = [];
        try {
            ;
            NodeList childNodes = (NodeList) xpath.compile(path).evaluate(parentNode, XPathConstants.NODESET);
            if (childNodes != null){
                childNodes.each{
                    values.add(it.getTextContent())
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return values;
    }

    /**
     * Get the value of the child node
     *
     * @param path
     *            name of child node
     * @param parentNode
     *            parent node, the child belongs to
     * @return value of child node
     */
    public Boolean isChildNodeExists(String path, Node parentNode) {
        try {
            debug("HarpXmlHelper | Entered isChildNodeExists method");
            Boolean node = (Boolean) xpath.evaluate(path, parentNode, XPathConstants.BOOLEAN);
            if (node != null) {
                return true;
            }
        } catch (Exception e) {
            debug("HarpXmlHelper | Exception occured in isChildNodeExists method ");
            e.printStackTrace();
        }
        return false;
    }

    public String getDocumentToString() {
        return getDocumentToString(doc);
    }

    // TODO added this for the invalid resp sent by HRP 91, this should later be
    // used for other invlid responses too
    // remove this - duplicate method
    public void addCriteria(String elementName, String xPathValue, String attributeName, String attrValue,
                            String content) {
        try {
            debug("HarpXmlHelper | Entered addCriteria[String,String,String,String,String] method");
            Element e = doc.createElement(elementName);
            e.setAttribute(attributeName, attrValue);
            e.setTextContent(content);
            Node node = (Node) xpath.compile(xPathValue).evaluate(doc, XPathConstants.NODE);
            node.appendChild(e);
        } catch (Exception e) {
            debug("HarpXmlHelper | Exception occured in addCriteria[String,String,String,String,String] method ");
            e.printStackTrace();
        }
    }

    private void debug(String logMessage) {
        if (logger.isDebugEnabled())
            logger.debug(logMessage);
    }

    public static void main(String[] args) {
        System.err.println(XmlHelper.getStandardTestDate());
    }
}
