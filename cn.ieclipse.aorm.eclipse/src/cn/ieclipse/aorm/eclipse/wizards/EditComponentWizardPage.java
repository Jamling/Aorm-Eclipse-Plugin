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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.ComboDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IStringButtonAdapter;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringButtonDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.AutoCompleteField;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cn.ieclipse.aorm.eclipse.AormPlugin;
import cn.ieclipse.aorm.eclipse.helpers.ComponentAttribute;
import cn.ieclipse.aorm.eclipse.helpers.ComponentAttributeTipHelper;
import cn.ieclipse.aorm.eclipse.helpers.ComponentElement;
import cn.ieclipse.aorm.eclipse.helpers.ImageConstants;
import cn.ieclipse.aorm.eclipse.helpers.IntentReflectionHelper;
import cn.ieclipse.aorm.eclipse.helpers.LetterImageDescriptor;
import cn.ieclipse.aorm.eclipse.helpers.MultiCheckSelector;
import cn.ieclipse.aorm.eclipse.helpers.ProjectHelper;

import com.android.ide.eclipse.adt.internal.sdk.Sdk;
import com.android.ide.eclipse.adt.internal.ui.ResourceChooser;
import com.android.resources.ResourceType;

/**
 * @author melord
 * 
 */
public class EditComponentWizardPage extends WizardPage {
    private static Image ADD_IMG = ResourceManager.getPluginImage(
            AormPlugin.PLUGIN_ID, "res/add.png");

    private static Image DEL_IMG = ResourceManager.getPluginImage(
            AormPlugin.PLUGIN_ID, "res/delete.png");
    private static Image UP_IMG = ResourceManager.getPluginImage(
            AormPlugin.PLUGIN_ID, "res/up.png");
    private static Image DOWN_IMG = ResourceManager.getPluginImage(
            AormPlugin.PLUGIN_ID, "res/down.png");
    private static Image CLASS_IMG = ResourceManager.getPluginImage(
            AormPlugin.PLUGIN_ID, "res/class.gif");

    protected ArrayList<ComponentAttribute> attributes = new ArrayList<ComponentAttribute>();
    private FontMetrics fontMetrics;
    private ScrolledComposite scrolledComposite;
    private Composite composite;
    private GridLayout layout;
    private Element rootNode;
    private Element selectedNode;
    private IProject project;
    private IntentReflectionHelper intentHelper;
    private Map<Element, ComponentElement> nodeAttrCache = new HashMap<Element, ComponentElement>();
    private Map<Element, ComponentElement> nodeAttrTipCache = new HashMap<Element, ComponentElement>();

    private static final int MID_ADD = 0x01;
    private static final int MID_COPY = 0x02;
    private static final int MID_PASTE = 0x03;
    private static final int MID_DELETE = 0x04;
    private static final int MID_UP = 0x05;
    private static final int MID_DOWN = 0x06;
    private TreeViewer treeViewer;

    /**
     * @wbp.parser.constructor
     */
    public EditComponentWizardPage(String title) {
        this("EditComponentWizardPage", title, AormPlugin
                .getImageDescriptor(ImageConstants.LARGE_ACTIVITY_ICON));
    }

    /**
     * For test
     */
    public EditComponentWizardPage() {
        this("EditComponentWizardPage", null, null);
    }

    protected EditComponentWizardPage(String pageName, String title,
            ImageDescriptor titleImage) {
        super(pageName, title, titleImage);
    }

    public void setProject(IJavaProject project) {
        this.project = project.getProject();
        intentHelper = new IntentReflectionHelper(project);
    }

    public void setInput(Element input) {
        this.rootNode = input;
    }

    public Map<Element, ComponentElement> getNodeAttrCache() {
        return nodeAttrCache;
    }

    public Element getRootNode() {
        return rootNode;
    }

    public void createControl(Composite parent) {

        GC gc = new GC(parent);
        gc.setFont(parent.getFont());
        fontMetrics = gc.getFontMetrics();
        gc.dispose();
        // for (ComponentAttribute attr : attributes) {
        // createAttrFiled(container, layout.numColumns, attr);
        // }
        SashForm sashForm = new SashForm(parent, SWT.NONE);
        sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Composite composite_1 = new Composite(sashForm, SWT.NONE);
        composite_1.setLayout(new GridLayout(1, false));

        ToolBar toolBar = new ToolBar(composite_1, SWT.FLAT | SWT.RIGHT);
        toolBar.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false,
                1, 1));

        final ToolItem tbAdd = new ToolItem(toolBar, SWT.NONE);
        tbAdd.setToolTipText("Add");
        tbAdd.setImage(ADD_IMG);

        final ToolItem tbDelete = new ToolItem(toolBar, SWT.NONE);
        tbDelete.setImage(DEL_IMG);
        tbDelete.setToolTipText("Delete");

        final ToolItem tbUp = new ToolItem(toolBar, SWT.NONE);
        tbUp.setToolTipText("Up");
        tbUp.setImage(UP_IMG);

        final ToolItem tbDown = new ToolItem(toolBar, SWT.NONE);
        tbDown.setToolTipText("Down");
        tbDown.setImage(DOWN_IMG);

        tbAdd.addSelectionListener(new MenuItemSelectionAdapter(MID_ADD));
        tbDelete.addSelectionListener(new MenuItemSelectionAdapter(MID_DELETE));
        tbUp.addSelectionListener(new MenuItemSelectionAdapter(MID_UP));
        tbDown.addSelectionListener(new MenuItemSelectionAdapter(MID_DOWN));
        tbAdd.setEnabled(false);
        tbDelete.setEnabled(false);
        tbUp.setEnabled(false);
        tbDown.setEnabled(false);

        final ToolItem tbTip = new ToolItem(toolBar, SWT.CHECK);
        tbTip.setImage(SWTResourceManager.getImage(
                org.eclipse.jdt.ui.ISharedImages.class,
                "/icons/full/obj16/never_translate.gif"));
        tbTip.setHotImage(SWTResourceManager.getImage(
                org.eclipse.jdt.ui.ISharedImages.class,
                "/icons/full/obj16/translate.gif"));
        tbTip.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                TipShell.setShow(tbTip.getSelection());
            }
        });

        tbTip.setToolTipText("Show/Hide tooltip text for attribute");
        // tbTip.setText("Show attribute tooltip");

        treeViewer = new TreeViewer(composite_1, SWT.BORDER);
        Tree tree = treeViewer.getTree();
        tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        Menu menu = new Menu(tree);
        tree.setMenu(menu);

        final MenuItem miAdd = new MenuItem(menu, SWT.NONE);
        miAdd.setText("Add");
        miAdd.setID(MID_ADD);
        miAdd.setImage(ADD_IMG);

        // final MenuItem miCopy = new MenuItem(menu, SWT.NONE);
        // miCopy.setText("Copy");
        // miCopy.setID(MID_COPY);
        //
        // final MenuItem miPaste = new MenuItem(menu, SWT.NONE);
        // miPaste.setText("Paste");
        // miPaste.setID(MID_PASTE);

        final MenuItem miDel = new MenuItem(menu, SWT.NONE);
        miDel.setText("Delete");
        miDel.setID(MID_DELETE);
        miDel.setImage(DEL_IMG);

        final MenuItem miUp = new MenuItem(menu, SWT.NONE);
        miUp.setText("Up");
        miUp.setID(MID_UP);
        miUp.setImage(UP_IMG);

        final MenuItem miDown = new MenuItem(menu, SWT.NONE);
        miDown.setText("Down");
        miDown.setID(MID_DOWN);
        miDown.setImage(DOWN_IMG);

        miAdd.addSelectionListener(new MenuItemSelectionAdapter(MID_ADD));
        miDel.addSelectionListener(new MenuItemSelectionAdapter(MID_DELETE));
        miUp.addSelectionListener(new MenuItemSelectionAdapter(MID_UP));
        miDown.addSelectionListener(new MenuItemSelectionAdapter(MID_DOWN));

        menu.addMenuListener(new MenuAdapter() {
            @Override
            public void menuShown(MenuEvent e) {
                miUp.setEnabled(selectedNode != rootNode);
                miDown.setEnabled(selectedNode != rootNode);

                boolean canAdd = selectedNode != null;
                if (canAdd) {
                    ComponentElement ce = getNodeAttrCache().get(selectedNode);
                    canAdd = canAdd && !ce.getChildren().isEmpty();
                    miAdd.setEnabled(canAdd);
                }
            }
        });

        treeViewer.setLabelProvider(new NodeLabelProvider());
        treeViewer.setContentProvider(new NodeContentProvider());
        treeViewer.setAutoExpandLevel(5);

        treeViewer.setInput(rootNode);
        treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection sel = (IStructuredSelection) event
                        .getSelection();
                update(sel.getFirstElement());

                tbUp.setEnabled(selectedNode != rootNode);
                tbDown.setEnabled(selectedNode != rootNode);
                tbDelete.setEnabled(selectedNode != null);

                boolean canAdd = selectedNode != null;
                if (canAdd) {
                    ComponentElement ce = getNodeAttrCache().get(selectedNode);
                    canAdd = canAdd && !ce.getChildren().isEmpty();
                    tbAdd.setEnabled(canAdd);
                }
                fireTreeUpdated();
            }
        });
        treeViewer.getTree().forceFocus();
        treeViewer.setAutoExpandLevel(3);

        scrolledComposite = new ScrolledComposite(sashForm, SWT.H_SCROLL
                | SWT.V_SCROLL);
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);

        scrolledComposite.setLayout(new FillLayout());
        composite = new Composite(scrolledComposite, SWT.NONE);
        scrolledComposite.setContent(composite);
        layout = new GridLayout();
        layout.numColumns = 4;
        layout.makeColumnsEqualWidth = false;
        composite.setLayout(layout);
        scrolledComposite.setMinSize(480, 320);
        sashForm.setWeights(new int[] { 400, 480 });
        setControl(sashForm);
    }

    private void fireTreeUpdated() {
        NodeContentProvider provider = (NodeContentProvider) treeViewer
                .getContentProvider();
        provider.setInit(true);
        treeViewer.refresh(true);
    }

    private class MenuItemSelectionAdapter extends SelectionAdapter {
        private int id;

        public MenuItemSelectionAdapter(int id) {
            this.id = id;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            switch (id) {
            case MID_ADD:
                if (selectedNode == null) {
                    return;
                }
                ComponentElement ce = getNodeAttrCache().get(selectedNode);
                NewNodeDialog dialog = new NewNodeDialog(getShell(), ce);
                dialog.setElements(ce.getChildren().toArray(
                        new ComponentElement[] {}));
                String name = dialog.getStringResult();
                if (name != null) {
                    Element c = selectedNode.getOwnerDocument().createElement(
                            name);
                    selectedNode.appendChild(c);
                    fireTreeUpdated();
                }
                break;
            case MID_COPY:
                selectedNode.cloneNode(true);
                break;
            case MID_PASTE:
                break;
            case MID_DELETE:
                if (selectedNode == null) {
                    return;
                }
                if (selectedNode == rootNode) {
                    boolean confirm = MessageDialog
                            .openConfirm(
                                    getShell(),
                                    "Confirm Delete",
                                    String.format(
                                            "Are you sure to remove the whole componet (%1$s)? ",
                                            rootNode.getNodeName()));
                    if (confirm) {
                        // rootNode.getParentNode().removeChild(rootNode);
                        treeViewer.setInput((Element) null);
                        rootNode = null;
                        selectedNode = null;
                        // fireTreeUpdated();
                    }
                } else {
                    selectedNode.getParentNode().removeChild(selectedNode);
                    fireTreeUpdated();
                }
                break;
            case MID_UP:
                Element prev = findPrev(selectedNode);
                if (prev != null) {
                    Element p = (Element) selectedNode.getParentNode();
                    p.removeChild(selectedNode);
                    p.insertBefore(selectedNode, prev);
                    ISelection sel = treeViewer.getSelection();

                    fireTreeUpdated();
                    // treeViewer.setSelection(sel);
                }
                break;
            case MID_DOWN:
                Element next = findNext(selectedNode);
                if (next != null) {
                    Element p = (Element) selectedNode.getParentNode();
                    p.removeChild(next);
                    p.insertBefore(next, selectedNode);
                    fireTreeUpdated();
                }
                break;
            default:
                break;
            }
        }

        private Element findPrev(Node e) {
            if (e == null) {
                return null;
            }
            Node prev = e.getPreviousSibling();
            if (prev instanceof Element) {
                return (Element) prev;
            } else {
                return findPrev(prev);
            }
        }

        private Element findNext(Node e) {
            if (e == null) {
                return null;
            }
            Node next = e.getNextSibling();
            if (next instanceof Element) {
                return (Element) next;
            } else {
                return findNext(next);
            }
        }
    }

    private void update(Object input) {

        Control[] cs = composite.getChildren();
        if (cs != null) {
            for (Control c : cs) {
                c.dispose();
            }
        }
        if (input == null) {
            return;
        }
        Element e = (Element) input;
        selectedNode = e;
        ComponentElement ce = nodeAttrCache.get(e);
        if (ce == null) {
            try {
                ce = ProjectHelper.getNodeDef(e.getTagName());
                if (ce == null) {
                    ComponentElement temp = nodeAttrCache
                            .get(e.getParentNode());
                    for (ComponentElement item : temp.getChildren()) {
                        if (item.getName().equals(e.getTagName())) {
                            ce = item;
                            break;
                        }
                    }
                }
                nodeAttrCache.put(e, ce);
                
                ComponentAttributeTipHelper.loadHtml(e
                        .getTagName(), ce);
                
                
            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
        ce.init(e);
        List<ComponentAttribute> attributes = ce.getAttributes();
        

        boolean isAction = e.getNodeName().equals("action");
        boolean isCategory = e.getNodeName().equals("category");

        for (ComponentAttribute attr : attributes) {
            createAttrFiled(composite, layout.numColumns, attr, ce, e);
        }
        ScrolledComposite sc = (ScrolledComposite) composite.getParent();
        composite.layout(true);
        Point p = composite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        sc.setMinSize(p);
    }

    private class NodeContentProvider implements ITreeContentProvider {

        boolean flag = false;

        public void dispose() {

        }

        public void setInit(boolean b) {
            this.flag = !b;
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            flag = false;
        }

        /**
         * It's really a joker!
         */
        public Object[] getElements(Object inputElement) {
            if (!flag) {
                flag = true;
                return new Element[] { (Element) inputElement };
            }
            return getChildren(inputElement);
        }

        public Object[] getChildren(Object parentElement) {
            Element e = (Element) parentElement;
            NodeList list = e.getChildNodes();
            ArrayList<Element> temp = new ArrayList<Element>();
            for (int i = 0; i < list.getLength(); i++) {
                Node n = list.item(i);
                if (n instanceof Element) {
                    temp.add((Element) n);
                }
            }
            return temp.toArray();
        }

        public Object getParent(Object element) {
            if (element instanceof Element) {
                Element e = (Element) element;
                return e.getParentNode();
            }
            return null;
        }

        public boolean hasChildren(Object element) {
            Element e = (Element) element;
            boolean ret = e.getChildNodes().getLength() > 0;
            return ret;
        }
    }

    private class NodeLabelProvider extends LabelProvider {
        @Override
        public String getText(Object element) {
            Element e = (Element) element;
            String name = e.getAttribute("android:name");
            if (name == null || name.length() == 0) {
                name = e.getTagName();
            } else {
                name = name + " (" + e.getNodeName() + ")";
            }
            return name;
        }

        @Override
        public Image getImage(Object element) {
            if (rootNode == element) {
                return CLASS_IMG;
            }
            Element e = (Element) element;
            char letter = Character.toUpperCase(e.getTagName().charAt(0));
            return new LetterImageDescriptor(letter, 10, 'R')
                    .createImage(false);
        }
    }

    private void createAttrFiled(Composite parent, int nColumns,
            ComponentAttribute attr, ComponentElement ce, Element e) {
        Control control = null;
        int type = attr.getType();
        if (ComponentAttribute.TYPE_LIST == type) {
            ComboDialogField field = new ComboDialogField(SWT.BORDER);
            field.setDialogFieldListener(new TextDialogFieldAdapter(attr,
                    parent.getShell(), project, e, treeViewer));
            field.setLabelText(attr.getShortName());
            String initText = attr.getValue();
            if (attr.getFormats() != null) {
                field.setItems(attr.getFormats());
            }
            if (initText != null && !"".equals(initText)) {
                field.selectItem(initText);
            }
            field.doFillIntoGrid(parent, nColumns);
            control = field.getComboControl(parent);
        } else if (ComponentAttribute.TYPE_MLIST == type
                || ComponentAttribute.TYPE_DRAWABLE == type
                || ComponentAttribute.TYPE_STYPE == type
                || ComponentAttribute.TYPE_STRING_REF == type) {

            StringButtonAdapter adapter = new StringButtonAdapter(attr,
                    parent.getShell(), project, e, treeViewer);
            StringButtonDialogField field = new StringButtonDialogField(adapter);
            field.setLabelText(attr.getShortName());
            field.setButtonLabel(JFaceResources.getString("openBrowse"));
            field.setDialogFieldListener(adapter);
            field.doFillIntoGrid(parent, nColumns);
            int w = Dialog.convertWidthInCharsToPixels(fontMetrics, 50);
            LayoutUtil.setWidthHint(field.getTextControl(null), w);
            if (attr.getValue() != null) {
                field.setText(attr.getValue());
            }
            control = field.getTextControl(parent);
        } else if (ComponentAttribute.TYPE_STRING == type) {
            StringDialogField field = new StringDialogField();
            field.setLabelText(attr.getShortName());
            field.setDialogFieldListener(new StringButtonAdapter(attr, parent
                    .getShell(), project, e, treeViewer));
            field.doFillIntoGrid(parent, nColumns);
            int w = Dialog.convertWidthInCharsToPixels(fontMetrics, 50);
            LayoutUtil.setWidthHint(field.getTextControl(null), w);

            control = field.getTextControl(parent);
            if (attr.getValue() != null) {
                field.setText(attr.getValue());
            }
            if (intentHelper != null) {
                if ("name".equals(attr.getShortName())) {
                    if (ce.getName().equals("action")) {
                        AutoCompleteField acf = new AutoCompleteField(control,
                                new TextContentAdapter(), intentHelper
                                        .getActions().toArray(new String[] {}));
                    } else if (ce.getName().equals("category")) {
                        AutoCompleteField acf = new AutoCompleteField(control,
                                new TextContentAdapter(), intentHelper
                                        .getCategories().toArray(
                                                new String[] {}));
                    }
                } else if (attr.getShortName().toLowerCase()
                        .contains("permission")) {
                    AutoCompleteField acf = new AutoCompleteField(control,
                            new TextContentAdapter(), intentHelper
                                    .getPermissions().toArray(new String[] {}));
                }
            }
        }
        if (control != null) {
            TipShell.enableFor(control, attr.getTip());
            TipShell.setNodeName(selectedNode.getNodeName());
        }
    }

    private static class TextDialogFieldAdapter implements IDialogFieldListener {
        protected ComponentAttribute attr;
        protected Element e;
        protected IProject project;
        protected Shell shell;
        protected TreeViewer treeViewer;

        public TextDialogFieldAdapter(ComponentAttribute attr, Shell shell,
                IProject project, Element e, TreeViewer treeViewer) {
            this.attr = attr;
            this.project = project;
            this.shell = shell;
            this.e = e;
            this.treeViewer = treeViewer;
        }

        public void dialogFieldChanged(DialogField field) {
            String text = "";
            if (field instanceof StringDialogField) {
                text = ((StringDialogField) field).getText();
            } else if (field instanceof ComboDialogField) {
                text = ((ComboDialogField) field).getText();
            }
            attr.setValue(text);

            if ("".equals(attr.getValue().trim())
                    && e.getAttribute(attr.getName()) != null) {
                e.removeAttribute(attr.getName());
            } else {
                e.setAttribute(attr.getName(), attr.getValue().trim());
            }

            // if ("name".equals(attr.getShortName())) {
            // treeViewer.refresh();
            // }
        }

    }

    private static class StringButtonAdapter extends TextDialogFieldAdapter
            implements IStringButtonAdapter {

        public StringButtonAdapter(ComponentAttribute attr, Shell shell,
                IProject project, Element e, TreeViewer treeViewer) {
            super(attr, shell, project, e, treeViewer);
        }

        public void changeControlPressed(final DialogField field) {
            if (attr.getType() == ComponentAttribute.TYPE_MLIST) {
                MultiCheckSelector muSelector = new MultiCheckSelector(shell,
                        attr.getFormats(), attr.getValue());
                if (muSelector.open() == 0) {
                    StringBuilder sb = new StringBuilder();
                    for (String item : muSelector.getSelected()) {
                        sb.append(item);
                        sb.append("|");
                    }
                    if (sb.length() > 0) {
                        sb.deleteCharAt(sb.length() - 1);
                    }
                    attr.setValue(sb.toString());
                    ((StringButtonDialogField) field).setText(attr.getValue());
                }
            } else {
                if (project != null) {
                    ResourceType type = ResourceType.STRING;
                    if (attr.getType() == ComponentAttribute.TYPE_DRAWABLE) {
                        type = ResourceType.DRAWABLE;
                    } else if (attr.getType() == ComponentAttribute.TYPE_STYPE) {
                        type = ResourceType.STYLE;
                    }
                    ResourceChooser dia = ResourceChooser.create(project, type,
                            Sdk.getCurrent().getTargetData(project), shell);
                    dia.setCurrentResource(attr.getValue());
                    if (dia.open() == 0) {// ok
                        attr.setValue(dia.getCurrentResource());
                        ((StringButtonDialogField) field).setText(attr
                                .getValue());
                    }
                }
            }
        }
    }

    private static class NewNodeDialog extends ElementListSelectionDialog {
        private ComponentElement ce;

        protected NewNodeDialog(Shell parent, ComponentElement ce) {
            super(parent, new LabelProvider() {
                @Override
                public String getText(Object element) {
                    return element.toString();
                }
            });
            this.ce = ce;
        }

        // @Override
        // protected void computeResult() {
        // setResult(ce.getChildren());
        // }

        public String getStringResult() {
            if (open() == 0) {
                ComponentElement ret = (ComponentElement) getResult()[0];
                return ret.getName();
            }
            return null;
        }
    }
}
