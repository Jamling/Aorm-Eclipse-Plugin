/*
 * Copyright 2010 Android ORM projects.
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
package cn.ieclipse.aorm.eclipse.helpers;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaProject;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import cn.ieclipse.aorm.eclipse.AormPlugin;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

/**
 * AndroidManifest.xml modification class.
 * 
 * @author Jamling
 * 
 */
public class AndroidManifest {
    private static final String ATTR_NAME = "android:name";
    private Document doc;
    private Element root;
    private String path;
    private String pkg;
    
    private IJavaProject javaProject;
    
    public AndroidManifest(String path, IJavaProject project) throws Exception {
        this.path = path;
        this.javaProject = project;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        doc = builder.parse(path);
        root = doc.getDocumentElement();
        pkg = root.getAttribute("package");
    }
    
    private Node getNextNode(String nodeName) {
        NodeList actList = (NodeList) selectNodes("application/" + nodeName,
                root);
        if (actList == null || actList.getLength() == 0) {
            return null;
        }
        Node node = actList.item(actList.getLength() - 1).getNextSibling();
        while (node != null) {
            if (node instanceof Text) {
                node = node.getNextSibling();
            }
            else {
                break;
            }
        }
        return node;
    }
    
    private Element createActivity(Element activity, String name,
            String[] actions, String[] categories) {
        if (pkg != null && name.startsWith(pkg)) {
            activity.setAttribute(ATTR_NAME, name.substring(pkg.length()));
        }
        else {
            activity.setAttribute(ATTR_NAME, name);
        }
        if ((actions != null && actions.length > 0)
                || (categories != null && categories.length > 0)) {
            Element intentFilter = doc.createElement("intent-filter");
            activity.appendChild(intentFilter);
            if (actions != null) {
                Element actionEle = null;
                for (String action : actions) {
                    actionEle = doc.createElement("action");
                    actionEle.setAttribute(ATTR_NAME, action);
                    intentFilter.appendChild(actionEle);
                }
            }
            if (categories != null) {
                Element categoryEle = null;
                for (String category : categories) {
                    categoryEle = doc.createElement("category");
                    categoryEle.setAttribute(ATTR_NAME, category);
                    intentFilter.appendChild(categoryEle);
                }
            }
        }
        return activity;
    }
    
    private void createProvider(Element activity, String name, String authority) {
        
        activity.setAttribute("android:authorities", authority);
        if (pkg != null && name.startsWith(pkg)) {
            activity.setAttribute(ATTR_NAME, name.substring(pkg.length()));
        }
        else {
            activity.setAttribute(ATTR_NAME, name);
        }
    }
    
    private Comment createComment() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateStr = format.format(new java.util.Date());
        String user = System.getProperty("user.name");
        if (user == null) {
            user = "adt-ext";
        }
        user = "AORM plugin";
        return doc.createComment("created by " + user + " at " + dateStr);
    }
    
    public void addActivity(String superName, String name, String[] actions,
            String[] categories) {
        String nodeName = null;
        List<String> superNames = javaProject == null ? ProjectHelper
                .getSuperTypeName(superName) : ProjectHelper.getSuperTypeName(
                javaProject, superName, false);
        if (superNames.contains(AdtConstants.ACTIVITY_QNAME)) {
            nodeName = AdtConstants.ACTIVITY_NODE;
        }
        else if (superNames.contains(AdtConstants.SERVICE_QNAME)) {
            nodeName = AdtConstants.SERVICE_NODE;
        }
        else if (superNames.contains(AdtConstants.RECEIVER_QNAME)) {
            nodeName = AdtConstants.RECEIVER_NODE;
        }
        else {
            return;
        }
        if (nodeName == null) {
            return;
        }
        
        Element activity = doc.createElement(nodeName);
        Node next = getNextNode(nodeName);
        Element app = (Element) selectSingleNode("application", root);
        if (app != null) {
            Comment comment = createComment();
            // Text end = doc.createTextNode(" ");
            app.insertBefore(comment, next);
            // app.insertBefore(end, next);
            app.insertBefore(activity, next);
        }
        createActivity(activity, name, actions, categories);
    }
    
    private Element getComponentNode(String nodeName, String componentName) {
        String xpath = "application/" + nodeName + "[ @name='%s' ]";
        Element comp = (Element) selectSingleNode(
                String.format(xpath, componentName), root);
        if (comp == null && componentName.startsWith(pkg)) {
            comp = (Element) selectSingleNode(
                    String.format(xpath, componentName.substring(pkg.length())),
                    root);
        }
        return comp;
    }
    
    /**
     * @param nodeName
     *            activity, service, provider or receiver
     * @param componentName
     *            component full name
     * @return
     */
    public ArrayList<ComponentAttribute> getComponentAttribute(String nodeName,
            String componentName) {
        ArrayList<ComponentAttribute> res = new ArrayList<ComponentAttribute>();
        Element comp = getComponentNode(nodeName, componentName);
        if (comp != null) {
            NamedNodeMap attrs = comp.getAttributes();
            for (int i = 0; i < attrs.getLength(); i++) {
                Node n = attrs.item(i);
                ComponentAttribute item = new ComponentAttribute();
                item.setName(n.getNodeName());
                item.setValue(n.getNodeValue());
                res.add(item);
            }
        }
        return res;
    }
    
    /**
     * @param nodeName
     *            activity, service, provider or receiver
     * @param componentName
     *            component full name
     * @return
     */
    public List<ComponentIntentFilter> getComponentIntentFilter(
            String nodeName, String componentName) {
        ArrayList<ComponentIntentFilter> res = new ArrayList<ComponentIntentFilter>();
        Element comp = getComponentNode(nodeName, componentName);
        if (comp != null) {
            NodeList filters = selectNodes("inter-filter", comp);
            if (filters != null) {
                for (int i = 0; i < filters.getLength(); i++) {
                    ComponentIntentFilter temp = new ComponentIntentFilter();
                    Node filter = filters.item(i);
                    NodeList actions = selectNodes("action", filter);
                    if (actions != null) {
                        for (int j = 0; j < actions.getLength(); j++) {
                            Element action = (Element) actions.item(j);
                            temp.getActions().add(action.getAttribute("name"));
                        }
                    }
                    NodeList categories = selectNodes("category", filter);
                    if (categories != null) {
                        for (int j = 0; j < categories.getLength(); j++) {
                            Element action = (Element) categories.item(j);
                            temp.getCategories().add(
                                    action.getAttribute("name"));
                        }
                    }
                }
            }
        }
        return res;
    }
    
    public void setComponentAttribute(String type, String componentName,
            List<ComponentAttribute> attrs) {
        Element comp = getComponentNode(type, componentName);
        if (comp == null) {
            comp = doc.createElement(type);
            Node next = getNextNode(type);
            Element app = (Element) selectSingleNode("application", root);
            if (app != null) {
                Comment comment = createComment();
                // Text end = doc.createTextNode(" ");
                app.insertBefore(comment, next);
                // app.insertBefore(end, next);
                app.insertBefore(comp, next);
            }
        }
        if (comp != null) {
            for (ComponentAttribute attr : attrs) {
                String key = attr.getName();
                if (attr.getValue() == null
                        || "".equals(attr.getValue().trim())) {
                    if (comp.getAttribute(key) != null) {
                        comp.removeAttribute(key);
                    }
                    else {
                        continue;
                    }
                }
                else {
                    comp.setAttribute(key, attr.getValue().trim());
                }
            }
        }
    }
    
    public void addProvider(String superName, String name, String authority) {
        if ("android.content.ContentProvider".equals(superName)) {
            Element activity = doc.createElement("provider");
            Node next = getNextNode("provider");
            Element app = (Element) selectSingleNode("application", root);
            if (app != null) {
                Comment comment = createComment();
                // Text end = doc.createTextNode(" ");
                app.insertBefore(comment, next);
                // app.insertBefore(end, next);
                app.insertBefore(activity, next);
            }
            createProvider(activity, name, authority);
        }
    }
    
    public void save() throws Exception {
        TransformerFactory transFactory = TransformerFactory.newInstance();
        try {
            Transformer transformer = transFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(
                    "{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
                    "yes");
            
            DOMSource source = new DOMSource();
            source.setNode(doc);
            StreamResult result = new StreamResult();
            result.setOutputStream(new FileOutputStream(path));
            
            transformer.transform(source, result);
        } catch (TransformerConfigurationException e) {
            throw e;
        } catch (TransformerException e) {
            throw e;
        } catch (FileNotFoundException e) {
            throw e;
        }
    }
    
    public void save2() throws Exception {
        // OutputFormat format = new OutputFormat(doc);
        // // format.setLineWidth(65);
        // format.setIndenting(true);
        // // format.setIndent(2);
        // FileOutputStream fos = new FileOutputStream(path);
        // XMLSerializer serializer = new XMLSerializer(fos, format);
        // serializer.serialize(doc);
        // fos.close();
        
        OutputFormat format = new OutputFormat(doc);
        // format.setLineWidth(65);
        format.setIndenting(true);
        // format.setIndent(2);
        XMLSerializer serializer = new XMLSerializer(
                new FileOutputStream(path), format);
        serializer.serialize(doc);
    }
    
    public static Node selectSingleNode(String express, Object source) {
        Node result = null;
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        // xpath.setNamespaceContext(new NamespaceContext() {
        //
        // public Iterator getPrefixes(String namespaceURI) {
        // // TODO Auto-generated method stub
        // return null;
        // }
        //
        // public String getPrefix(String namespaceURI) {
        // // TODO Auto-generated method stub
        // return null;
        // }
        //
        // public String getNamespaceURI(String prefix) {
        // if (null == prefix) {
        // throw new NullPointerException("Null prefix");
        // } else {
        // if ("xml".equals(prefix)) {
        // return XMLConstants.XML_NS_URI;
        // } else if ("android".equals(prefix)) {
        // return "http://schemas.android.com/apk/res/android";
        // }
        // }
        // return XMLConstants.NULL_NS_URI;
        // }
        // });
        try {
            result = (Node) xpath
                    .evaluate(express, source, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            AormPlugin.log(IStatus.WARNING, "can't select node by path {0}",
                    express);
        }
        
        return result;
    }
    
    public static NodeList selectNodes(String express, Object source) {
        NodeList result = null;
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        try {
            result = (NodeList) xpath.evaluate(express, source,
                    XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            // node will be null;
            AormPlugin.log(IStatus.WARNING, "can't select node by path {0}",
                    express);
        }
        
        return result;
    }
    
    public static void main(String[] args) throws Exception {
        // AndroidManifest manifest = new
        // AndroidManifest("AndroidManifest.xml");
        // manifest.addActivity("android.content.BroadcastReceiver",
        // "com.compal.helloandroid.m.TR", new String[] { "abc" }, null);
        
        // ArrayList<ComponentAttribute> list = manifest.getComponentAttribute(
        // "receiver", "com.example.m.TR");
        //
        // System.out.println(list);
        // ComponentAttribute newAttr = new ComponentAttribute();
        // newAttr.setName("android:icon");
        // newAttr.setValue("iconvalue");
        // list.add(newAttr);
        // list.get(0).setValue(null);
        // manifest.setComponentAttribute("receiver", "com.example.m.TR", list);
        // manifest.save2();
        // getNodeName("java.util.ArrayList");
    }
}
