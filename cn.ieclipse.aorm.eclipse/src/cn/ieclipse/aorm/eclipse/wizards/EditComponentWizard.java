/*
 * Copyright 2012 Jamling(li.jamling@gmail.com).
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
package cn.ieclipse.aorm.eclipse.wizards;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cn.ieclipse.aorm.eclipse.AormPlugin;
import cn.ieclipse.aorm.eclipse.helpers.AdtConstants;
import cn.ieclipse.aorm.eclipse.helpers.AndroidManifest;
import cn.ieclipse.aorm.eclipse.helpers.ComponentAttribute;
import cn.ieclipse.aorm.eclipse.helpers.ComponentAttributeTipHelper;
import cn.ieclipse.aorm.eclipse.helpers.ComponentElement;
import cn.ieclipse.aorm.eclipse.helpers.ImageConstants;
import cn.ieclipse.aorm.eclipse.helpers.ProjectHelper;
import cn.ieclipse.aorm.eclipse.helpers.Status;

/**
 * @author melord
 * 
 */
public class EditComponentWizard extends Wizard implements IWorkbenchWizard {

    /**
     * Wizard id.
     */
    public static final String ID = AormPlugin.PLUGIN_ID
            + "wizards.EditComponentWizard";
    protected IStructuredSelection selection;

    /**
     * AndroidManifest.xml node name
     */
    protected String nodeName;

    protected Element node;

    /**
     * Selected .java QName.
     */
    protected String compName;

    protected Map<String, String> nodeMap;

    protected ComponentElement nodeElement;
    protected IJavaProject jProject;
    protected AndroidManifest manifest;
    protected IFile manifestFile;

    private EditComponentWizardPage page0;
    private boolean add = false;
    private String pageTitle = "Edit Android Component";

    protected void updateManifest(AndroidManifest manifest) {
        Element e = page0.getRootNode();
        if (e == null && node != null) {
            node.getParentNode().removeChild(node);
            node = null;
        }
        if (node != null) {
            updateNode(node);
        }
    }

    protected void updateNode(Element node) {
        ComponentElement ce = page0.getNodeAttrCache().get(node);
        if (ce != null) {
            List<ComponentAttribute> attrs = ce.getAttributes();
            for (ComponentAttribute attr : attrs) {
                String key = attr.getName();
                if (attr.getValue() == null
                        || "".equals(attr.getValue().trim())) {
                    if (node.getAttribute(key) != null) {
                        node.removeAttribute(key);
                    } else {
                        continue;
                    }
                } else {
                    node.setAttribute(key, attr.getValue().trim());
                }
            }
        }
        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node n = list.item(i);
            if (n instanceof Element) {
                updateNode((Element) n);
            }
        }

    }

    public EditComponentWizard() {
        setWindowTitle("Edit Android Component");
        setDefaultPageImageDescriptor(AormPlugin
                .getImageDescriptor(ImageConstants.LARGE_ACTIVITY_ICON));
    }

    /**
     * For test
     * 
     * @param nodeName
     * @param compName
     */
    public EditComponentWizard(String nodeName, String compName) {
        this.nodeName = nodeName;
        this.compName = compName;

        page0 = new EditComponentWizardPage("", "test ", null);

        try {
            manifest = new AndroidManifest("AndroidManifest.xml", null);
            node = manifest.getComponentNode(this.nodeName, this.compName);
            if (node == null) {
                this.add = true;
                if (nodeName == AdtConstants.PROVIDER_NODE) {
                    node = manifest.addProvider(AdtConstants.PROVIDER_QNAME,
                            compName, null);
                } else {
                    node = manifest.addActivity(compName, compName, null, null);
                }
            }
            page0.setInput(node);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addPages() {
        super.addPages();
        if (page0 == null && nodeName != null) {
            if (this.add) {
                pageTitle = String.format("Add Android <%s>", nodeName);
            } else {
                pageTitle = String.format("Edit Android <%s>", nodeName);
            }
            page0 = new EditComponentWizardPage(pageTitle);
            page0.setDescription(String.format(
                    "Edit <%s> element in AndroidManifest.xml", nodeName));
            page0.setInput(node);
            page0.setProject(jProject);
        }
        addPage(page0);
    }

    @Override
    public boolean performFinish() {
        if (jProject != null) {
            try {
                updateManifest(manifest);
                manifest.save2();
                manifestFile.refreshLocal(1, null);
                return true;
            } catch (Exception e) {
                Status status = new Status();
                status.setError(e.toString());
                ErrorDialog.openError(getShell(),
                        "Error when updating manifest", e.getMessage(), status);
                return false;
            }
        } else { // for test.
            try {
                updateManifest(manifest);
                manifest.save2();
                return true;
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return false;
    }

    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
        IJavaElement jele = ProjectHelper
                .getInitialJavaElement((IStructuredSelection) selection);
        jProject = jele.getJavaProject();
        if (jele instanceof ICompilationUnit) {
            // init nodeName
            ICompilationUnit unit = (ICompilationUnit) jele;
            Set<String> supers = ProjectHelper.getSuperTypeName(unit, false);
            if (supers.contains(AdtConstants.ACTIVITY_QNAME)) {
                nodeName = AdtConstants.ACTIVITY_NODE;
            } else if (supers.contains(AdtConstants.SERVICE_QNAME)) {
                nodeName = AdtConstants.SERVICE_NODE;
            } else if (supers.contains(AdtConstants.RECEIVER_QNAME)) {
                nodeName = AdtConstants.RECEIVER_NODE;
            } else if (supers.contains(AdtConstants.PROVIDER_QNAME)) {
                nodeName = AdtConstants.PROVIDER_NODE;
            } else {
                nodeName = null;
            }
            // System.out.println("node name:" + nodeName);
            try {
                nodeMap = ComponentAttributeTipHelper.load(nodeName);
                // init compName
                compName = unit.getTypes()[0].getFullyQualifiedName();
                // init manifest file
                manifestFile = ProjectHelper.getManifestLocation(jProject
                        .getProject());
                // init manifest
                manifest = ProjectHelper.getAndroidManifest(jele);
                // init node of selected java file.
                node = manifest.getComponentNode(nodeName, compName);
                if (node == null) {
                    this.add = true;
                    if (nodeName == AdtConstants.PROVIDER_NODE) {
                        node = manifest.addProvider(
                                AdtConstants.PROVIDER_QNAME, compName, null);
                    } else {
                        node = manifest.addActivity(
                                AdtConstants.ACTIVITY_QNAME, compName, null,
                                null);
                    }
                }

            } catch (Exception e) {
                Status status = new Status();
                status.setError(e.toString());
                ErrorDialog.openError(getShell(),
                        "Error init EditComponentWizard", e.getMessage(),
                        status);
            }
        }
    }
}
