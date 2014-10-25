/*
 * Copyright 2010 the original author or authors.
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

import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * Provides a List element with Add and Remove buttons to select entries from a
 * list of Objects.
 * 
 * @author Michael Kober
 * 
 */
public class ElementListSelector {

    private final Button addButton;
    private final Button removeButton;
    private final Object[] elements;
    private final Set<String> selectedElements = new TreeSet<String>();

    private final List list;

    public ElementListSelector(final Composite composite, GridData gridData,
            final String groupDescr, final String selectionMessage,
            final Object[] elements) {
        this.elements = elements;
        Group intentGroup = new Group(composite, SWT.NONE);
        intentGroup.setLayout(new GridLayout(2, false));
        intentGroup.setLayoutData(gridData);
        intentGroup.setText(groupDescr);

        list = new List(intentGroup, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);

        list.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Composite buttonComp = new Composite(intentGroup, SWT.NONE);
        buttonComp.setLayout(new FillLayout(SWT.VERTICAL));
        addButton = new Button(buttonComp, SWT.NONE);
        addButton.setText("Add...");
        addButton.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                showSelectionDialog(composite, selectionMessage);
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        removeButton = new Button(buttonComp, SWT.NONE);
        removeButton.setText("Remove...");
        removeButton.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                int[] selection = list.getSelectionIndices();
                if (selection != null) {
                    for (String selectedElement : list.getSelection()) {
                        selectedElements.remove(selectedElement);
                    }
                    list.remove(selection);
                    list.update();
                }
            }

            public void widgetDefaultSelected(SelectionEvent e) {
                // TODO Auto-generated method stub

            }
        });
    }

    private void showSelectionDialog(Composite composite,
            String selectionMessage) {
        LabelProvider labelProvider = new LabelProvider();
        ElementListSelectionDialog dialog = new ElementListSelectionDialog(
                composite.getShell(), labelProvider);
        dialog.setElements(elements);
        dialog.setIgnoreCase(true);
        dialog.setMessage(selectionMessage);
        dialog.setMultipleSelection(true);
        dialog.open();
        Object[] result = dialog.getResult();
        if (result != null) {
            for (Object object : result) {
                selectedElements.add((String) object);
            }
            list.removeAll();
            for (Object elem : selectedElements) {
                list.add(elem.toString());
            }
            list.update();
        }
    }

    /**
     * @return selected elements
     */
    public Set<String> getSelectedElements() {
        return selectedElements;
    }
}
