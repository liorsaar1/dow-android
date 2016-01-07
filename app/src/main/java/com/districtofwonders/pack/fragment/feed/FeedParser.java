package com.districtofwonders.pack.fragment.feed;

import com.districtofwonders.pack.util.DomParser;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by liorsaar on 2015-12-17
 */
public class FeedParser extends DomParser {

    private static List<String> requiredTags = new ArrayList<>(
            Arrays.asList(Tags.TITLE, Tags.LINK, Tags.PUB_DATE, Tags.DURATION, Tags.CONTENT_ENCODED));

    public FeedParser(String xmlString) throws ParserConfigurationException, IOException, SAXException {
        super(xmlString);
    }

    public List<Map<String, String>> getItems() {
        List<Map<String, String>> itemsList = new ArrayList<>();

        NodeList itemsNodeList = getRoot().getElementsByTagName(Elements.ITEM);
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
            if (node.getNodeName().equals(Elements.ENCLOSURE)) {
                NamedNodeMap urlNNP = node.getAttributes();
                String url = urlNNP.getNamedItem(Tags.URL).getNodeValue();
                map.put(Keys.ENCLOSURE_URL, url);
            }
        }
        return map;
    }

    class Elements {
        public static final String ITEM = "item";
        public static final String ENCLOSURE = "enclosure";
    }

    class Tags {
        public static final String TITLE = "title";
        public static final String LINK = "link";
        public static final String PUB_DATE = "pubDate";
        public static final String DURATION = "itunes:duration";
        public static final String URL = "url";
        public static final String CONTENT_ENCODED = "content:encoded";
    }

    class Keys {
        public static final String ENCLOSURE_URL = "enclosure.url";
    }
}
