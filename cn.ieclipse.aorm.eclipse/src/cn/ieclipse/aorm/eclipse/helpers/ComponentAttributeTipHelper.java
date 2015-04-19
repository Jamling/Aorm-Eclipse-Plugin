/**
 * 
 */
package cn.ieclipse.aorm.eclipse.helpers;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cn.ieclipse.aorm.eclipse.AormPlugin;

/**
 * @author Jamling
 * 
 */
public class ComponentAttributeTipHelper {

    private static final String NS = "android:";
    private static final String FN_EXT = ".tip";
    private static ComponentAttributeTipHelper instance;

    private Map<String, String> activityMap;
    private Map<String, String> serviceMap;
    private Map<String, String> providerMap;
    private Map<String, String> receiverMap;

    public static ComponentAttributeTipHelper getInstance() {
        if (instance == null) {
            synchronized (ComponentAttributeTipHelper.class) {
                instance = new ComponentAttributeTipHelper();
            }
        }
        return instance;
    }

    private ComponentAttributeTipHelper() {
        // activityMap = load(AdtConstants.ACTIVITY_NODE);
        // serviceMap = load(AdtConstants.SERVICE_NODE);
        // providerMap = load(AdtConstants.PROVIDER_NODE);
        // receiverMap = load(AdtConstants.RECEIVER_NODE);
    }

    public static Map<String, String> load(String nodeName) {

        HashMap<String, String> map = new HashMap<String, String>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    AormPlugin.class.getResourceAsStream("tip/" + nodeName
                            + FN_EXT)));
            String name = "";
            StringBuilder val = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                if (line.startsWith(NS)) {
                    if (val.length() > 0) {//
                        map.put(name, val.toString());
                        val.delete(0, val.length());
                    }
                    name = line.substring(NS.length());
                } else {
                    val.append(line);
                }
                line = br.readLine();
            }
            map.put(name, val.toString());
        } catch (Exception e) {
            // e.printStackTrace();
        }
        return map;
    }

    public static Map<String, String> loadHtml(String nodeName,
            ComponentElement ce) {
        HashMap<String, String> map = new HashMap<String, String>();
        try {
            DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
            fac.setIgnoringElementContentWhitespace(true);
            fac.setIgnoringComments(true);
            Document doc = fac.newDocumentBuilder().parse(
                    AormPlugin.class.getResourceAsStream("tip/" + nodeName
                            + ".xml"));
            Element e = doc.getDocumentElement();

            TransformerFactory transFactory = TransformerFactory.newInstance();
            Transformer transformer = transFactory.newTransformer();
            DOMSource source = new DOMSource();
            StreamResult result = new StreamResult();

            NodeList list = e.getElementsByTagName("dt");
            int len = list.getLength();
            for (int i = 0; i < len; i++) {
                Element dt = getDtElement(list, i);
                // dt not null;
                Element dd = getDdElement(dt);
                String achor = ((Element) dt.getFirstChild())
                        .getAttribute("name");
                String name = dt.getLastChild().getTextContent().trim();
                // System.out.println(dt.getTextContent() + "#" + achor + "#"
                // + name + " -> " + dd.getTextContent());
                ComponentAttribute attr = ce.findAttr(name);
                if (attr != null) {
                    attr.setAchor(achor);
                    source.setNode(dd);
                    ByteArrayOutputStream oos = new ByteArrayOutputStream();
                    result.setOutputStream(oos);
                    transformer.transform(source, result);
                    attr.setTip(oos.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    private static Element getDtElement(NodeList list, int index) {
        int len = list.getLength();
        for (int i = index; i < len; i++) {
            Node n = list.item(i);
            if (n instanceof Element) {
                Element e = (Element) n;
                if (e.getTagName().equals("dt")) {
                    return e;
                }
            }
        }
        return null;
    }

    private static Element getDdElement(Node dt) {
        if (dt != null) {
            Node n = dt.getNextSibling();
            if (n != null) {
                if (n instanceof Element) {
                    Element e = (Element) n;
                    if (e.getTagName().equals("dd")) {
                        return e;
                    } else if (e.getTagName().equals("dt")) {
                        return null;
                    } else {
                        return getDdElement(e);
                    }
                } else {
                    return getDdElement(n);
                }
            }
        }
        return null;
    }

    private static String getXmlText(Element e) {
        try {
            DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
            fac.setIgnoringElementContentWhitespace(true);
            DOMSource source = new DOMSource(e);

            TransformerFactory transFactory = TransformerFactory.newInstance();
            ByteArrayOutputStream oos = new ByteArrayOutputStream();
            Result result = new StreamResult(oos);
            // DOMResult result2 = new DOMResult();
            Transformer transformer = transFactory.newTransformer();
            transformer.transform(source, result);

            return oos.toString();
        } catch (Exception ex) {
            return "";
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        Map<String, String> map = ComponentAttributeTipHelper.getInstance()
                .load(AdtConstants.ACTIVITY_NODE);
        for (String key : map.keySet()) {
            // System.out.println(key + ":" + map.get(key));
        }
        ComponentAttributeTipHelper.getInstance().loadHtml(
                AdtConstants.ACTIVITY_NODE, null);
    }

}
