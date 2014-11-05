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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * @author Jamling
 * 
 */
public class MultiCheckSelector extends Dialog {
    
    private Table list;
    
    String[] formats;
    
    String initValue;
    
    /**
     * Create the dialog.
     * 
     * @param parentShell
     */
    public MultiCheckSelector(Shell parentShell) {
        super(parentShell);
    }
    
    public MultiCheckSelector(Shell parentShell, String[] formats, String value) {
        super(parentShell);
        this.formats = formats;
        this.initValue = value;
    }
    
    /**
     * Create contents of the dialog.
     * 
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        
        list = new Table(container, SWT.BORDER | SWT.MULTI | SWT.CHECK);
        list.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        
        List<String> values = new ArrayList<String>();
        if (initValue != null && initValue.trim().length() > 0) {
            String[] temp = initValue.split("\\|");
            for (String str : temp) {
                values.add(str.trim());
            }
        }
        
        for (int i = 0; i < formats.length; i++) {
            TableItem item = new TableItem(list, SWT.NONE);
            item.setText(formats[i]);
            if (values.contains(formats[i])) {
                item.setChecked(true);
            }
        }
        return container;
    }
    
    /**
     * Create contents of the button bar.
     * 
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        Button ok = createButton(parent, IDialogConstants.OK_ID,
                IDialogConstants.OK_LABEL, true);
        ok.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (callback != null) {
                    List<String> sel = getSelection();
                    callback.onOkay(sel);
                }
            }
        });
        Button cancel = createButton(parent, IDialogConstants.CANCEL_ID,
                IDialogConstants.CANCEL_LABEL, false);
        cancel.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (callback != null) {
                    callback.onClose();
                }
            }
        });
    }
    
    /**
     * Return the initial size of the dialog.
     */
    @Override
    protected Point getInitialSize() {
        return new Point(450, 300);
    }
    
    public java.util.List<String> getSelection() {
        ArrayList<String> res = new ArrayList<String>();
        TableItem[] tis = list.getSelection();
        for (TableItem ti : tis) {
            res.add(ti.getText());
        }
        return res;
    }
    
    public void setCallback(Callback callback) {
        this.callback = callback;
    }
    
    private Callback callback;
    
    public static interface Callback {
        public void onClose();
        
        public void onOkay(List<String> selections);
    }
}
