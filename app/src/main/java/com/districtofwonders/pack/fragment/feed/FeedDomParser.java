package com.districtofwonders.pack.fragment.feed;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by liorsaar on 2015-12-17
 */
public class FeedDomParser {
    enum FeedTags { item };

    private static List<String> requiredTags = new ArrayList<>(
            Arrays.asList("title", "link", "pubDate", "itunes:duration"));


    Document document;
    Element root;

    public FeedDomParser(String xmlString) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        ByteArrayInputStream input =  new ByteArrayInputStream(xmlString.getBytes("UTF-8"));
        document = builder.parse(input);
        root = document.getDocumentElement();
    }

    public List<Map<String, String>> getItems() {
        List<Map<String, String>> itemsList = new ArrayList<>();

        String x = String.valueOf(FeedTags.item);
        NodeList itemsNodeList = root.getElementsByTagName("item");
        for (int i = 0; i < itemsNodeList.getLength(); i++) {
            Node itemNode = itemsNodeList.item(i);
            Map<String, String> itemMap = getItem(itemNode);
            itemsList.add(itemMap);
        }
        return itemsList;
    }

    private Map<String, String> getItem(Node itemNode) {
        Map<String, String> map = new HashMap<>();
        NodeList itemChildNodes = itemNode.getChildNodes();
        for (int i = 0; i < itemChildNodes.getLength(); i++) {
            Node node = itemChildNodes.item(i);
            if (requiredTags.contains(node.getNodeName())) {
                map.put(node.getNodeName(), node.getTextContent());
            }
            if (node.getNodeName().equals("enclosure")) {
                NamedNodeMap urlNNP = node.getAttributes();
                String url = urlNNP.getNamedItem("url").getNodeValue();
                map.put( "enclosure.url", url);
            }
        }
        return map;
    }
}
