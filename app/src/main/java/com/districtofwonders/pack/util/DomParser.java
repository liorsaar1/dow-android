package com.districtofwonders.pack.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by liorsaar on 2015-12-17
 */
public class DomParser {
    private Document document;
    private Element root;

    public DomParser(String xmlString) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        ByteArrayInputStream input = new ByteArrayInputStream(xmlString.getBytes("UTF-8"));
        document = builder.parse(input);
        root = document.getDocumentElement();
    }

    public Element getRoot() {
        return root;
    }
}
