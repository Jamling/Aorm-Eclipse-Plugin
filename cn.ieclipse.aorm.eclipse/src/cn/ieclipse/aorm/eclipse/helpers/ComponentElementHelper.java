/**
 * 
 */
package cn.ieclipse.aorm.eclipse.helpers;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Jamling
 * 
 */
public class ComponentElementHelper {
    private InputStream is;
    private String tagName;
    private ComponentElement current;
    private ComponentElement root;

    public ComponentElementHelper(InputStream is) {
        this.is = is;
    }

    public ComponentElement parse() throws IOException {
        int c;
        while ((c = read()) != -1) {
            switch (c) {
            case '<':
                readTag();
                break;
            case '/':
                readEndTag();
                break;
            default:
                break;
            }
        }
        return root;
    }

    private int read() throws IOException {
        int c = is.read();
        // System.out.println("-->" + (char)c);
        return c;
    }

    private void readTag() throws IOException {
        int c;
        StringBuilder sb = new StringBuilder();
        while ((c = read()) != -1) {
            if (c == '\r' || c == '\n' || c == '\t' || c == ' ') {
                tagName = sb.toString().trim();
                startTag(tagName);
                readAttrList();
                break;
            } else {
                sb.append((char) c);
            }
        }
    }

    private void readEndTag() throws IOException {
        int c;
        StringBuilder sb = new StringBuilder();
        while ((c = read()) != -1) {
            if (c == '>') {
                endTag(sb.toString().trim());
                break;
            } else {
                sb.append((char) c);
            }
        }
    }

    private void readAttrList() throws IOException {
        int c;
        StringBuilder sb = new StringBuilder();
        String name = "";
        while ((c = read()) != -1) {
            if (c == '=') {
                name = sb.toString();
                sb.delete(0, sb.length());
            } else if (c == '[') {
                String val = readAttrMultiValue();
                startAttr(name.trim(), val.trim());
            } else if (c == '"') {
                String val = readAttrValue();
                startAttr(name.trim(), val.trim());
            } else if (c == '>') {
                break;
            } else if (c == '/') {
                if (read() == '>') {
                    endTag(tagName);
                    break;
                }
            } else {
                sb.append((char) c);
            }
        }
    }

    private String readAttrMultiValue() throws IOException {
        int c;
        StringBuilder sb = new StringBuilder();
        while ((c = read()) != -1) {
            if (c == ']') {
                break;
            } else {
                sb.append((char) c);
            }
        }
        return sb.toString();
    }

    private String readAttrValue() throws IOException {
        int c;
        StringBuilder sb = new StringBuilder();
        while ((c = read()) != -1) {
            if (c == '"') {
                break;
            } else {
                sb.append((char) c);
            }
        }
        return sb.toString();
    }

    private void startTag(String name) {
        if (root == null) {
            root = new ComponentElement(name);
        }
        if (current == null) {
            current = root;
        } else {
            ComponentElement temp = new ComponentElement(name);
            current.addChild(temp);
            current = temp;
        }
        // System.out.println("startTag:<" + name);
    }

    private void endTag(String name) {
        // System.out.println("endTag:" + name + "/>");
        if (current.getParent() != null) {
            current = current.getParent();
        }
    }

    private void startAttr(String name, String value) {
        // System.out.println(name + "=" + value);
        if (current != null) {
            current.setAttr(name, value);
        }
    }
}
