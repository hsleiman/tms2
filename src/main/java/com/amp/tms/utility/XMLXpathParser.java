/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.amp.tms.utility;

import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author raine.cabal
 */
public class XMLXpathParser {

    private final Document doc;
    private final XPath xpath;

    private static final Logger log = LoggerFactory.getLogger(XMLXpathParser.class);

    public static XMLXpathParser Parser(String xml) {      
        XMLXpathParser parser = null;
        try {
            parser = new XMLXpathParser(xml);
        } catch (SAXException ex) {
            log.error(ex.getMessage());
        } catch (IOException | ParserConfigurationException ex) {
            log.error(ex.getMessage());
        }
        return parser;
    }


    public XMLXpathParser(String xml) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        doc = builder.parse(new InputSource(new StringReader(xml)));
        doc.getDocumentElement().normalize();
        XPathFactory xpathfactory = XPathFactory.newInstance();
        xpath = xpathfactory.newXPath();
    }

    public Double getCount(String expression) throws XPathExpressionException {
        return (Double) xpath.compile(expression).evaluate(doc, XPathConstants.NUMBER);

    }

    public Boolean isExpressionTrue(String expression) throws XPathExpressionException {
        return (Boolean) xpath.compile(expression).evaluate(doc, XPathConstants.BOOLEAN);
    }

    public NodeList getNodeList(String expression) throws XPathExpressionException {
        return (NodeList) xpath.compile(expression).evaluate(doc, XPathConstants.NODESET);
    }

    public Node getNode(String expression) throws XPathExpressionException {
        return (Node) xpath.compile(expression).evaluate(doc, XPathConstants.NODE);
    }

    public String getExpressionValue(String expression) throws XPathExpressionException {
        return (String) xpath.compile(expression).evaluate(doc, XPathConstants.STRING);
    }

    public void printXpathResult(Object result) {
        NodeList nodes = (NodeList) result;
        for (int i = 0; i < nodes.getLength(); i++) {
            System.out.println(nodes.item(i).getNodeValue());
        }
    }

}
