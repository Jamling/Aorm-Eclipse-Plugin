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
package cn.ieclipse.aorm.eclipse.wizards;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import cn.ieclipse.aorm.eclipse.AormPlugin;
import cn.ieclipse.aorm.eclipse.helpers.ImageConstants;

public class ElementTableSelector {
    private static Image ADD_IMG = AormPlugin.getImageDescriptor(
            ImageConstants.ACTION_ADD).createImage();
    private static Image UP_IMG = AormPlugin.getImageDescriptor(
            ImageConstants.ACTION_UP).createImage();
    private static Image DOWN_IMG = AormPlugin.getImageDescriptor(
            ImageConstants.ACTION_DOWN).createImage();
    private final Button addButton;
    private final Button removeButton;
    private final Object[] elements;
    private final ArrayList<Model> selectedElements = new ArrayList<ElementTableSelector.Model>();

    private final Table table;
    private final TableViewer tv;

    public ElementTableSelector(final Composite composite, GridData gridData,
            final String groupDescr, final String selectionMessage,
            final Object[] elements) {
        this.elements = elements;
        Group intentGroup = new Group(composite, SWT.NONE);
        intentGroup.setLayout(new GridLayout(2, false));
        intentGroup.setLayoutData(gridData);
        intentGroup.setText(groupDescr);

        tv = new TableViewer(intentGroup, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL
                | SWT.FULL_SELECTION);
        table = tv.getTable();
        table.setHeaderVisible(false);
        table.setLinesVisible(true);

        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        final Menu menu = new Menu(tv.getControl());
        MenuItem add = new MenuItem(menu, SWT.PUSH);
        add.setText("&Add Custom");
        add.setImage(ADD_IMG);
        add.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                addModel(new Model("custom"));
                tv.refresh();
            }
        });

        MenuItem up = new MenuItem(menu, SWT.PUSH);
        up.setText("&Up");
        up.setImage(UP_IMG);
        up.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection sel = (IStructuredSelection) tv
                        .getSelection();
                if (!sel.isEmpty()) {
                    Model m = (Model) sel.getFirstElement();
                    int idx = selectedElements.indexOf(m);
                    if (idx > 0) {
                        Model n = selectedElements.get(idx - 1);
                        selectedElements.set(idx, n);
                        selectedElements.set(idx - 1, m);

                        tv.refresh();
                    }

                }
            }
        });

        MenuItem down = new MenuItem(menu, SWT.PUSH);
        down.setText("&Down");
        down.setImage(DOWN_IMG);
        down.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection sel = (IStructuredSelection) tv
                        .getSelection();
                if (!sel.isEmpty()) {
                    Model m = (Model) sel.getFirstElement();
                    int idx = selectedElements.indexOf(m);
                    if (idx < selectedElements.size() - 1) {
                        Model n = selectedElements.get(idx + 1);
                        selectedElements.set(idx, n);
                        selectedElements.set(idx + 1, m);

                        tv.refresh();
                    }

                }
            }
        });

        table.setMenu(menu);

        tv.setContentProvider(new MyContentProvider());

        ColumnViewerToolTipSupport.enableFor(tv, ToolTip.NO_RECREATE);
        // TableViewerFocusCellManager focusCellManager = new
        // TableViewerFocusCellManager(
        // tv, new FocusBorderCellHighlighter(tv));
        ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(
                tv) {
            protected boolean isEditorActivationEvent(
                    ColumnViewerEditorActivationEvent event) {
                return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
                        || event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION
                        || (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && event.keyCode == SWT.CR)
                        || event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
            }
        };

        TableViewerEditor.create(tv, null, actSupport,
                ColumnViewerEditor.TABBING_HORIZONTAL
                        | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
                        | ColumnViewerEditor.TABBING_VERTICAL
                        | ColumnViewerEditor.KEYBOARD_ACTIVATION);

        final TableViewerColumn col = new TableViewerColumn(tv, SWT.NONE);
        col.setLabelProvider(new MyCellLabelProvider());
        col.setEditingSupport(new MyEditingSupport(tv));
        // tv.getTable().
        col.getColumn().setWidth(300);

        table.addControlListener(new ControlListener() {

            public void controlResized(ControlEvent e) {
                int w = table.getClientArea().width;
                if (w > 0) {
                    col.getColumn().setWidth(w);
                }
            }

            public void controlMoved(ControlEvent e) {

            }
        });

        tv.setInput(selectedElements);

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
        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                int[] selection = table.getSelectionIndices();
                if (selection != null) {
                    IStructuredSelection sel = (IStructuredSelection) tv
                            .getSelection();
                    if (!sel.isEmpty()) {
                        for (Object object : sel.toArray()) {
                            selectedElements.remove(object);
                        }
                    }
                    tv.refresh();
                }
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
                addModel(new Model((String) object));
            }
            tv.refresh();
        }
    }

    private boolean addModel(Model model) {
        for (Model m : selectedElements) {
            if (m.getName().equalsIgnoreCase(model.getName())) {
                return false;
            }
        }
        return selectedElements.add(model);
    }

    /**
     * @return selected elements
     */
    public Set<String> getSelectedElements() {
        Set<String> set = new TreeSet<String>();
        for (Model model : selectedElements) {
            set.add(model.getName());
        }
        return set;
    }

    private static class Model {
        String name;

        public Model(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public String toString() {
            return getName();
        }
    }

    private static class MyCellLabelProvider extends
            org.eclipse.jface.viewers.CellLabelProvider {

        static Image custom = AormPlugin.getImageDescriptor(
                ImageConstants.ACTION_CUSTOM).createImage();
        static Image system = AormPlugin.getImageDescriptor(
                ImageConstants.ACTION_SYSTEM).createImage();

        @Override
        public void update(ViewerCell cell) {
            cell.setText(cell.getElement().toString());
            if (cell.getElement().toString().startsWith("android.intent")) {
                cell.setImage(system);
            } else {
                cell.setImage(custom);
            }
        }

        public String getToolTipText(Object element) {
            return "Double click to edit";
        }

        public Point getToolTipShift(Object object) {
            return new Point(5, 5);
        }

        public int getToolTipDisplayDelayTime(Object object) {
            return 200;
        }

        public int getToolTipTimeDisplayed(Object object) {
            return 3000;
        }

    }

    private static class MyContentProvider implements
            IStructuredContentProvider {

        public MyContentProvider() {
        }

        public void dispose() {
            //
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            //
        }

        @SuppressWarnings("unchecked")
        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof List<?>) {
                List<Model> list = (List<Model>) inputElement;
                return list.toArray(new Model[] {});
            }
            return null;
        }

    }

    private static class MyEditingSupport extends EditingSupport {
        CellEditor editor;

        public MyEditingSupport(TableViewer viewer) {
            super(viewer);
            editor = new TextCellEditor(viewer.getTable(), SWT.BORDER) {

            };
        }

        @Override
        protected CellEditor getCellEditor(Object element) {
            return editor;
        }

        @Override
        protected boolean canEdit(Object element) {
            return true;
        }

        @Override
        protected Object getValue(Object element) {
            return element.toString();
        }

        @Override
        protected void setValue(Object element, Object value) {
            if (element instanceof Model) {
                ((Model) element).setName(value.toString());
            }
            getViewer().update(element, null);
        }

    }
}
