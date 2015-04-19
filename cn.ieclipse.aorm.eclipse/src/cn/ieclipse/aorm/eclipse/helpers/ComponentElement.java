/**
 * 
 */
package cn.ieclipse.aorm.eclipse.helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

/**
 * @author Jamling
 * 
 */
public class ComponentElement {
    private String name;
    private ComponentElement parent;
    private List<ComponentAttribute> attrs = new ArrayList<ComponentAttribute>();
    private List<ComponentElement> children = new ArrayList<ComponentElement>();

    public ComponentElement(String name) {
        this.name = name;
    }

    public void addChild(ComponentElement child) {
        child.parent = this;
        this.children.add(child);
    }

    public List<ComponentElement> getChildren() {
        return children;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ComponentElement getParent() {
        return parent;
    }

    public void setAttr(String name, String value) {
        ComponentAttribute attr = new ComponentAttribute();
        attr.setName(name);
        attr.setFormats(value);
        attrs.add(attr);
    }

    public List<ComponentAttribute> getAttributes() {
        return attrs;
    }

    public void init(Element node) {
        NamedNodeMap map = node.getAttributes();
        for (int i = 0; i < map.getLength(); i++) {
            Attr attr = (Attr) map.item(i);
            ComponentAttribute ca = findAttr(attr.getName());
            if (ca != null) {
                ca.setValue(attr.getValue());
                ca.setPriority(map.getLength() - i);
            }
        }
        ComponentAttribute ca = findAttr("android:name");
        if (ca != null) {
            ca.setPriority(100);
        }
        Collections.sort(attrs);
    }

    public ComponentAttribute findAttr(String name) {
        for (ComponentAttribute attr : attrs) {
            if (attr.getName().equals(name)) {
                return attr;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return getName();
    }
}
