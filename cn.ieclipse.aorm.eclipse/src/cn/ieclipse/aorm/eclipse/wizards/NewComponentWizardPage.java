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
package cn.ieclipse.aorm.eclipse.wizards;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.wizards.NewTypeWizardPage;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import cn.ieclipse.aorm.eclipse.helpers.AdtConstants;
import cn.ieclipse.aorm.eclipse.helpers.AndroidManifest;
import cn.ieclipse.aorm.eclipse.helpers.ProjectHelper;

/**
 * Wizard page for new android activity.
 * 
 * @author Michael Kober
 */
public class NewComponentWizardPage extends NewTypeWizardPage {
    //
    // private static final String PAGE_NAME = "NewTypeWizardPage";
    // private static final String SETTINGS_CREATEMAIN = "create_main";
    // private static final String SETTINGS_CREATECONSTR = "create_constructor";
    // private static final String SETTINGS_CREATEUNIMPLEMENTED =
    // "create_unimplemented";
    //
    // private static final String[] ACTIVITY_METHODS = { "onCreate", "onStart",
    // "onResume", "onPause", "onStop", "onDestroy" };
    //
    // private static final String[] SERVICE_METHODS = { "onCreate", "onStart",
    // "onDestroy" };
    //
    // private static final String[] RECEIVER_METHODS = { "onReceive" };
    //
    // private IntentReflectionHelper helper;
    //
    // private ElementTableSelector actionSelector;
    // private ElementTableSelector categorySelector;

    protected IJavaProject javaProject = null;

    // protected Composite methodComp;

    public static final int EDIT_MODE_NEW = 0;
    public static final int EDIT_MODE_MOD = 1;
    protected int editMode = EDIT_MODE_NEW;

    protected Combo compCombo;

    /**
     * Creates a new {@code ProjectSettingsWizardPage}.
     * 
     * @param midletProject
     *            the project data container
     */
    public NewComponentWizardPage() {
        super(true, "NewAndroidComponentWizardPage");
        setTitle("Android Activity");
        setDescription("Create a new Android Activity, Service, BroadcastReceiver.");
    }

    /**
     * The wizard owning this page is responsible for calling this method with
     * the current selection. The selection is used to initialize the fields of
     * the wizard page.
     * 
     * @param selection
     *            used to initialize the fields
     */
    public void init(IStructuredSelection selection) {
        IJavaElement jelem = getInitialJavaElement(selection);
        javaProject = jelem.getJavaProject();
        initContainerPage(jelem);
        initTypePage(jelem);
        doStatusUpdate();

        AndroidManifest manifest = ProjectHelper.getAndroidManifest(jelem);
        if (manifest != null) {

        }
    }

    // ------ validation --------
    protected void doStatusUpdate() {
        // the mode severe status will be displayed and the OK button
        // enabled/disabled.
        updateStatus(getUpdateStatus());
    }

    protected IStatus[] getUpdateStatus() {
        // status of all used components
        IStatus[] status = new IStatus[] {
                fContainerStatus,
                isEnclosingTypeSelected() ? fEnclosingTypeStatus
                        : fPackageStatus, fTypeNameStatus, fModifierStatus,
                fSuperClassStatus, fSuperInterfacesStatus };
        return status;
    }

    // ------ UI --------

    /*
     * @see WizardPage#createControl
     */
    public void createControl(Composite parent) {
        initializeDialogUnits(parent);

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setFont(parent.getFont());
        int nColumns = 4;
        GridLayout layout = new GridLayout();
        layout.numColumns = nColumns;
        composite.setLayout(layout);

        // pick component
        createComponent(composite, nColumns);
        // pick & choose the wanted UI components
        createContainerControls(composite, nColumns);
        createPackageControls(composite, nColumns);
        createSeparator(composite, nColumns);
        createTypeNameControls(composite, nColumns);

        // setTypeName("Activity", true);
        createSuperClassControls(composite, nColumns);

        createSuperInterfacesControls(composite, nColumns);

        createOtherControls(composite, nColumns);

        // createCommentControls(composite, nColumns);
        setAddComments(true, false);
        enableCommentControl(true);

        setControl(composite);
        Dialog.applyDialogFont(composite);
    }

    protected void createComponent(Composite composite, int nColumns) {
        Label l = new Label(composite, SWT.NONE);
        l.setText("Component:");

        compCombo = new Combo(composite, SWT.READ_ONLY);
        compCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
                false, 1, 1));
        compCombo.add(AdtConstants.ACTIVITY_QNAME);
        compCombo.add(AdtConstants.SERVICE_QNAME);
        compCombo.add(AdtConstants.RECEIVER_QNAME);
        compCombo.add(AdtConstants.PROVIDER_QNAME);
        compCombo.select(0);
        compCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setSuperClass(compCombo.getText(), true);
            }
        });

        new Label(composite, SWT.NONE);
        new Label(composite, SWT.NONE);
    }

    /**
     * add some customize controls
     * 
     * @param composite
     * @param nColumns
     */
    protected void createOtherControls(Composite composite, int nColumns) {

    }

    //
    // /*
    // * @see WizardPage#becomesVisible
    // */
    // public void setVisible(boolean visible) {
    // super.setVisible(visible);
    // if (visible) {
    // setFocus();
    // } else {
    // IDialogSettings dialogSettings = getDialogSettings();
    // if (dialogSettings != null) {
    // IDialogSettings section = dialogSettings.getSection(PAGE_NAME);
    // if (section == null) {
    // section = dialogSettings.addNewSection(PAGE_NAME);
    // }
    // section.put(SETTINGS_CREATEMAIN, false);
    // section.put(SETTINGS_CREATECONSTR, false);
    // section.put(SETTINGS_CREATEUNIMPLEMENTED, true);
    // }
    // }
    // }

    /*
     * @see NewTypeWizardPage#createTypeMembers
     */
    protected void createTypeMembers(final IType type,
            final ImportsManager imports, IProgressMonitor monitor)
            throws CoreException {
        boolean doConstr = false;
        boolean doInherited = true;
        createInheritedMethods(type, doConstr, doInherited, imports,
                new SubProgressMonitor(monitor, 1));
    }

    @Override
    protected void createTypeNameControls(Composite composite, int nColumns) {
        super.createTypeNameControls(composite, nColumns - 1);
        final Button btn = new Button(composite, SWT.CHECK);
        btn.setText("With supper suffix?");
        btn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String superName = getSuperClass();
                int pos = superName.lastIndexOf('.');
                if (pos > 0 && pos < superName.length() - 1) {
                    String simpleName = superName.substring(pos + 1);
                    if (btn.getSelection()) {
                        if (!getTypeName().endsWith(simpleName)) {
                            setTypeName(getTypeName() + simpleName, true);
                        }
                    } else {
                        if (getTypeName().endsWith(simpleName)) {
                            setTypeName(
                                    getTypeName().substring(
                                            0,
                                            getTypeName().length()
                                                    - simpleName.length()),
                                    true);
                        }
                    }
                }
            }
        });
    }

    @Override
    protected IStatus superClassChanged() {
        // TODO Auto-generated method stub
        return super.superClassChanged();

    }

    protected String findSuperName(String superName) {
        boolean flag = AdtConstants.ACTIVITY_QNAME.equals(superName);
        flag = flag | AdtConstants.SERVICE_QNAME.equals(superName);
        flag = flag | AdtConstants.PROVIDER_QNAME.equals(superName);
        flag = flag | AdtConstants.RECEIVER_QNAME.equals(superName);
        if (flag) {
            return superName;
        } else {
            try {
                Class clz = Class.forName(superName);

            } catch (Exception e) {
                // TODO: handle exception
            }
        }
        return null;
    }
}
