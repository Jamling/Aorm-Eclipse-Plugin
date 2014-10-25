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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import cn.ieclipse.aorm.eclipse.AormPlugin;
import cn.ieclipse.aorm.eclipse.helpers.Status;
import cn.ieclipse.aorm.eclipse.jdt.JavaSelection;
import cn.ieclipse.aorm.eclipse.jdt.SourceAnalysis;
import cn.ieclipse.aorm.eclipse.jdt.SourceGenerator;
import cn.ieclipse.aorm.eclipse.jdt.JavaSelection.TypeMapping;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (a).
 * 
 * @author Jamling
 */

public class NewOrmProviderWizardPage extends NewProviderWizardPage {

    private JavaSelection javaSelection;
    private Table table;

    private Text databaseText;
    private String database;
    private IStatus databaseStatus;
    private List<TypeMapping> mapList;

    // /**
    // * Constructor for SampleNewWizardPage.
    // *
    // * @param pageName
    // */
    // public NewOrmProviderWizardPage() {
    // super();
    // setTitle("Android ContentProvider");
    // setDescription("Create a new Android ContentProvider.");
    // }

    @Override
    protected void createOtherControls(Composite composite, int nColumns) {
        super.createOtherControls(composite, nColumns);
        createDatabase(composite, nColumns);
        createTableArea(composite, nColumns);
    }

    private void createDatabase(Composite composite, int nColumns) {
        Label label = new Label(composite, SWT.NONE);
        label.setText("Database Name:");
        databaseText = new Text(composite, SWT.BORDER);
        databaseText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false,
                false, nColumns - 2, 1));
        databaseText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                handleFieldChanged("database");
            }
        });
        new Label(composite, SWT.NONE);
    }

    private void createTableArea(Composite composite, int nColumns) {
        // Group group = new Group(composite, SWT.NONE);
        // group.setText("select tables which you want to generate DDL");
        // group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,
        // nColumns, 2));
        // group.setLayout(new FillLayout());

        Label label = new Label(composite, SWT.NONE);
        label.setText("select tables which you want to generate DDL");
        label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false,
                nColumns, 1));

        // new Label(composite, SWT.NONE);

        table = new Table(composite, SWT.H_SCROLL | SWT.V_SCROLL
                | SWT.FULL_SELECTION | SWT.CHECK | SWT.BORDER);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,
                nColumns, 1));
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        TableLayout layout = new TableLayout();

        layout.addColumnData(new ColumnWeightData(5, 25, true));
        layout.addColumnData(new ColumnWeightData(20, 75, true));
        layout.addColumnData(new ColumnWeightData(70, true));
        table.setLayout(layout);

        TableColumn checkCol = new TableColumn(table, SWT.NONE);
        checkCol.setText("");

        TableColumn tableCol = new TableColumn(table, SWT.NONE);
        tableCol.setText("Table");
        //tableCol.setImage(AormPlugin.getImageDescriptor("res/table.png").createImage());

        TableColumn typeCol = new TableColumn(table, SWT.NONE);
        typeCol.setText("Class");
        //typeCol.setImage(AormPlugin.getImageDescriptor("res/class.gif").createImage());

        for (final TypeMapping type : mapList) {
            final TableItem ti = new TableItem(table, SWT.NONE);
            ti.setChecked(true);
            type.setChecked(true);
            ti.setData(type);
            ti.setText(new String[] { "", type.getTable(),
                    type.getType().getFullyQualifiedName() });
            ti.addListener(SWT.MouseDown, new Listener() {

                public void handleEvent(Event event) {
                    type.setChecked(ti.getChecked());
                }
            });

        }

    }

    /**
     * Tests if the current workbench selection is a suitable container to use.
     */

    public void init(IStructuredSelection selection) {
        super.init(selection);
        javaSelection = new JavaSelection(selection);
        mapList = javaSelection.getTypeMappings();
    }

    @Override
    protected IStatus[] getUpdateStatus() {
        IStatus[] status = super.getUpdateStatus();
        IStatus[] dest = new IStatus[status.length + 1];
        System.arraycopy(status, 0, dest, 0, status.length);
        dest[status.length] = databaseStatus;
        return dest;
    }

    @Override
    protected void handleFieldChanged(String fieldName) {
        super.handleFieldChanged(fieldName);
        databaseStatus = databaseChanged();
        doStatusUpdate();
    }

    private IStatus databaseChanged() {
        Status status = new Status();
        if (databaseText != null) {
            database = databaseText.getText().trim();
            if (database.length() <= 0) {
                status.setError("database name can't be empty!");
            } else if (!database.endsWith(".db")) {
                status.setWarning("database name should with .db suffix");
            } else {
                status.setOK();
            }
        }
        return status;
    }

    public String getDatabase() {
        return database;
    }

    public List<TypeMapping> getTypeMappings() {
        // final List<TypeMapping> list = new
        // ArrayList<JavaSelection.TypeMapping>();
        // //
        // // TableItem[] items = table.getSelection();
        // // for (TableItem ti : items) {
        // // list.add((TypeMapping) ti.getData());
        // // }
        //
        // for(TypeMapping type: javaSelection.getTypeMappings()){
        // if(type.isChecked()){
        //
        // }
        // }

        return mapList;
    }

    @Override
    protected void createTypeMembers(IType newType, ImportsManager imports,
            IProgressMonitor monitor) throws CoreException {
        // super.createTypeMembers(newType, imports, monitor);
        createInheritedMethods(newType, false, true, imports,
                new SubProgressMonitor(monitor, 1));

        final List<String> tableCreators = new ArrayList<String>();

        List<TypeMapping> list = getTypeMappings();
        for (TypeMapping map : list) {
            if (map.isChecked()) {
                tableCreators.add(SourceAnalysis.getSQL(map, true));
            }
        }
        ICompilationUnit cu = newType.getCompilationUnit();

        try {
            // CompilationUnit unit = SourceGenerator.merge(cu,
            // getPackageText(), getTypeName(),
            // getAuthority(), getDatabase(), tableCreators);
            // SourceGenerator.applyChange(cu, unit);

            imports.addImport("android.database.sqlite.SQLiteDatabase");
            imports.addImport("android.database.sqlite.SQLiteOpenHelper");
            imports.addImport("org.melord.android.orm.Session");
            // imports.addImport("android.content.UriMatcher");
            // imports.addImport("android.content.ContentValues");
            SourceGenerator.merge(newType, getAuthority(), getDatabase(),
                    tableCreators);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}