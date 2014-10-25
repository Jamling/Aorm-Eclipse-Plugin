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
import java.util.List;

import org.eclipse.jdt.internal.ui.wizards.dialogfields.ComboDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IStringButtonAdapter;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringButtonDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import cn.ieclipse.aorm.eclipse.AormPlugin;
import cn.ieclipse.aorm.eclipse.helpers.ComponentAttribute;
import cn.ieclipse.aorm.eclipse.helpers.ImageConstants;
import cn.ieclipse.aorm.eclipse.helpers.MultiCheckSelector;
import cn.ieclipse.aorm.eclipse.helpers.ProjectHelper;

/**
 * @author melord
 * 
 */
public class EditComponentWizardPage extends WizardPage {
    protected ArrayList<ComponentAttribute> attributes = new ArrayList<ComponentAttribute>();
    private FontMetrics fontMetrics;

    public EditComponentWizardPage(String pageName) {
        this(pageName, "Edit Component attribute", AormPlugin
                .getImageDescriptor(ImageConstants.LARGE_ACTIVITY_ICON));
    }

    protected EditComponentWizardPage(String pageName, String title,
            ImageDescriptor titleImage) {
        super(pageName, title, titleImage);
    }

    public void setComponentType(String nodeName) {

        attributes = ProjectHelper.getConfAttrs(nodeName + ".def");
    }

    public void setInitAttributes(List<ComponentAttribute> initAttrs) {
        for (ComponentAttribute old : initAttrs) {
            for (ComponentAttribute attr : attributes) {
                if (old.getShortName().equals(attr.getShortName())) {
                    attr.setValue(old.getValue());
                    System.out.println("set " + attr.getName() + "="
                            + attr.getValue());
                    break;
                }
            }
        }
    }

    public void createControl(Composite parent) {

        GC gc = new GC(parent);
        gc.setFont(parent.getFont());
        fontMetrics = gc.getFontMetrics();
        gc.dispose();
        Composite fixPane = new Composite(parent, SWT.NONE);
        fixPane.setLayout(new GridLayout(1, false));
        ScrolledComposite page = new ScrolledComposite(fixPane, SWT.NONE
                | SWT.V_SCROLL);
        page.setExpandVertical(true);
        page.setExpandHorizontal(true);
        GridData data = new GridData();
        data.heightHint = 300;
        page.setLayoutData(data);
        page.setLayout(new GridLayout(1, false));

        Composite container = new Composite(page, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.numColumns = 4;
        layout.makeColumnsEqualWidth = false;
        container.setLayout(layout);
        for (ComponentAttribute attr : attributes) {
            createAttrFiled(container, layout.numColumns, attr);
        }
        container.setLayoutData(new GridData(GridData.FILL_BOTH));
        page.setContent(container);
        Point p = container.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        page.setMinSize(p);

        setControl(fixPane);

        Dialog.applyDialogFont(fixPane);
    }

    private void createAttrFiled(Composite parent, int nColumns,
            ComponentAttribute attr) {
        //
        // GridData gridData = new GridData();
        // gridData.horizontalSpan = 1;
        // Label label = new Label(parent, SWT.NONE);
        // label.setText(attr.getName());
        // label.setLayoutData(gridData);
        //
        // gridData = new GridData();
        int type = attr.getType();
        if (ComponentAttribute.TYPE_LIST == type) {
            ComboDialogField field = new ComboDialogField(SWT.BORDER);
            field.setDialogFieldListener(new ComboAdapter(attr));
            field.setLabelText(attr.getShortName());
            if (attr.getFormats() != null) {
                field.setItems(attr.getFormats());
            }
            if (attr.getValue() != null && !"".equals(attr.getValue())) {
                System.out.println(attr.getName() + "init:" + attr.getValue());
                field.setText(attr.getValue());
            }
            field.doFillIntoGrid(parent, nColumns);
            // int w = Dialog.convertWidthInCharsToPixels(fontMetrics, 50);
            // LayoutUtil.setWidthHint(field.get, w);
        } else if (ComponentAttribute.TYPE_MLIST == type
                || ComponentAttribute.TYPE_DRAWABLE == type
                || ComponentAttribute.TYPE_STYPE == type
                || ComponentAttribute.TYPE_STRING_REF == type) {

            MyStringButtonDialogField field = new MyStringButtonDialogField(
                    new StringButtonAdapter(attr, parent.getShell()));
            field.setLabelText(attr.getShortName());
            field.setButtonLabel(JFaceResources.getString("openBrowse"));
            field.doFillIntoGrid(parent, nColumns);
            int w = Dialog.convertWidthInCharsToPixels(fontMetrics, 50);
            LayoutUtil.setWidthHint(field.getTextControl(null), w);
            if (attr.getValue() != null) {
                field.setText(attr.getValue());
            }
        } else if (ComponentAttribute.TYPE_STRING == type) {
            StringDialogField field = new StringDialogField();
            field.setLabelText(attr.getShortName());
            field.setDialogFieldListener(new StringButtonAdapter(attr, parent
                    .getShell()));
            field.doFillIntoGrid(parent, nColumns);
            if (attr.getValue() != null) {
                field.setText(attr.getValue());
            }
            if ("name".equals(attr.getShortName())) {

            }
        }

    }

    private static class ButtonSelector extends SelectionAdapter {
        Text text;
        ComponentAttribute attr;

        public ButtonSelector(Text text, ComponentAttribute attr) {
            this.text = text;
            this.attr = attr;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {

        }
    }

    @SuppressWarnings("restriction")
    private static class MyStringButtonDialogField extends
            StringButtonDialogField {

        public MyStringButtonDialogField(StringButtonAdapter adapter) {
            super(adapter);
            setDialogFieldListener(adapter);
        }

    }

    private static class StringButtonAdapter implements IStringButtonAdapter,
            IDialogFieldListener {
        private ComponentAttribute attr;
        private Shell shell;

        public StringButtonAdapter(ComponentAttribute attr, Shell shell) {
            this.attr = attr;
            this.shell = shell;
        }

        public void changeControlPressed(DialogField field) {
            if (attr.getType() == ComponentAttribute.TYPE_MLIST) {
                MultiCheckSelector muSelector = new MultiCheckSelector(shell,
                        attr.getFormats(), attr.getValue());
                muSelector.setCallback(new MultiCheckSelector.Callback() {

                    public void onOkay(List<String> selections) {
                        StringBuilder sb = new StringBuilder();
                        for (String item : selections) {
                            sb.append(item);
                            sb.append("|");
                        }
                        if (sb.length() > 0) {
                            sb.deleteCharAt(sb.length() - 1);
                        }
                        attr.setValue(sb.toString());
                    }

                    public void onClose() {
                        // TODO Auto-generated method stub

                    }
                });
                muSelector.open();
            }
        }

        public void dialogFieldChanged(DialogField field) {
            StringDialogField f = (StringDialogField) field;
            attr.setValue(f.getText());

        }
    }

    private static class ComboAdapter implements IDialogFieldListener {
        private ComponentAttribute attr;

        public ComboAdapter(ComponentAttribute attr) {
            this.attr = attr;
        }

        public void dialogFieldChanged(DialogField field) {
            ComboDialogField f = (ComboDialogField) field;
            System.out.println(f.getText() + " change to ");
            attr.setValue(f.getText());
        }
    }

}
