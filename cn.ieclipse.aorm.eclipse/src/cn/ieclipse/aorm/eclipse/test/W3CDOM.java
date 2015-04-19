/**
 * 
 */
package cn.ieclipse.aorm.eclipse.test;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * @author Jamling
 * 
 */
public class W3CDOM {

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
        fac.setIgnoringElementContentWhitespace(true);
        Document doc = fac.newDocumentBuilder().parse(
                new InputSource(new StringReader("<a><b a=\"d\">bb</b> dd</a>")));
        Element e = doc.getDocumentElement();
        System.out.println(e.getTextContent());

        DOMSource source = new DOMSource(e);

        TransformerFactory transFactory = TransformerFactory.newInstance();
        ByteArrayOutputStream oos = new ByteArrayOutputStream();
        Result result = new StreamResult(oos);
        // DOMResult result2 = new DOMResult();
        Transformer transformer = transFactory.newTransformer();
        transformer.transform(source, result);
        System.out.println(oos.toString());
    }
}
