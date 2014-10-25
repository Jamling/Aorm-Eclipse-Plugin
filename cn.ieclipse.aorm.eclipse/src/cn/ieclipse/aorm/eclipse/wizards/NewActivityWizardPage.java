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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.CodeGeneration;
import org.eclipse.jdt.ui.wizards.NewTypeWizardPage.ImportsManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import cn.ieclipse.aorm.eclipse.helpers.AdtConstants;
import cn.ieclipse.aorm.eclipse.helpers.IntentReflectionHelper;
import cn.ieclipse.aorm.eclipse.helpers.ProjectHelper;

/**
 * Wizard page for new android activity.
 * 
 * @author Michael Kober
 */
public class NewActivityWizardPage extends NewComponentWizardPage {

    // private static final String PAGE_NAME = "NewTypeWizardPage";
    // private static final String SETTINGS_CREATEMAIN = "create_main";
    // private static final String SETTINGS_CREATECONSTR = "create_constructor";
    // private static final String SETTINGS_CREATEUNIMPLEMENTED =
    // "create_unimplemented";

    private static final String[] ACTIVITY_METHODS = { "onCreate", "onStart",
            "onResume", "onPause", "onStop", "onDestroy" };

    private static final String[] SERVICE_METHODS = { "onCreate",
            "onStartCommand", "onDestroy" };

    private static final String[] RECEIVER_METHODS = { "onReceive" };

    private IntentReflectionHelper helper;
    private ElementTableSelector actionSelector;
    private ElementTableSelector categorySelector;
    private Composite methodComp;

    /**
     * Creates a new {@code ProjectSettingsWizardPage}.
     * 
     * @param midletProject
     *            the project data container
     */
    public NewActivityWizardPage() {
        super();
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
        super.init(selection);
        helper = new IntentReflectionHelper(javaProject);
        doStatusUpdate();

    }

    /*
     * @see NewContainerWizardPage#handleFieldChanged
     */
    protected void handleFieldChanged(String fieldName) {
        super.handleFieldChanged(fieldName);
        doStatusUpdate();
    }

    @Override
    protected void createOtherControls(Composite composite, int nColumns) {
        createMethodStubSelectionControls(composite, nColumns);
        setSuperClass("android.app.Activity", true);
        createIntentActionsControl(composite, nColumns, helper.getActions());
        createIntentCategoriesControl(composite, nColumns,
                helper.getCategories());
        compCombo.remove(3);
    }

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

    private void createMethodStubSelectionControls(Composite composite,
            int nColumns) {

        Label label = new Label(composite, SWT.NONE);
        label.setText("Which method stubs would you like to create?");
        label.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false,
                nColumns, 1));

        Composite methodsComposite = new Composite(composite, SWT.NONE);
        methodsComposite.setFont(composite.getFont());
        GridLayout layout = new GridLayout(nColumns, true);
        methodsComposite.setLayout(layout);
        methodsComposite.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, true,
                false, nColumns, 1));

        methodComp = methodsComposite;
    }

    private void updateMethods(String[] methodNames) {
        if (methodNames == null || methodComp == null) {
            return;
        }
        methodComp.setVisible(false);
        Control[] ch = methodComp.getChildren();
        for (Control c : ch) {
            c.dispose();
        }
        for (String m : methodNames) {
            final Button onRestartCB = new Button(methodComp, SWT.CHECK);
            onRestartCB.setText(m + "()");
            onRestartCB.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false,
                    false, 1, 1));

        }

        methodComp.layout();
        methodComp.setVisible(true);
    }

    /*
     * @see NewTypeWizardPage#createTypeMembers
     */
    protected void createTypeMembers(final IType type,
            final ImportsManager imports, IProgressMonitor monitor)
            throws CoreException {
        super.createTypeMembers(type, imports, monitor);

        final ArrayList<String> list = new ArrayList<String>();
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                Control[] ch = methodComp.getChildren();
                for (Control control : ch) {
                    if (control instanceof Button) {
                        boolean sel = ((Button) control).getSelection();
                        String txt = ((Button) control).getText();
                        if (sel) {
                            list.add(txt.substring(0, txt.length() - 2));
                        }
                    }
                }
            }
        });
        for (String string : list) {
            generateStub(string, type, imports);
        }

        if (monitor != null) {
            monitor.done();
        }
    }

    private void generateOnCreate(IType type, ImportsManager imports)
            throws CoreException, JavaModelException {
        StringBuilder buf = new StringBuilder();
        final String lineDelim = "\n"; // OK, since content is formatted afterwards //$NON-NLS-1$
        buf.append("/* (non-Javadoc)").append(lineDelim);
        buf.append("* @see android.app.Activity#onCreate(android.os.Bundle)")
                .append(lineDelim);
        buf.append("*/").append(lineDelim);
        buf.append("@Override");
        buf.append(lineDelim);
        buf.append("public void onCreate("); //$NON-NLS-1$
        buf.append(imports.addImport("android.os.Bundle")); //$NON-NLS-1$
        buf.append(" savedInstanceState) {"); //$NON-NLS-1$
        buf.append(lineDelim);
        buf.append("super.onCreate(savedInstanceState);");
        buf.append(lineDelim);
        final String content = CodeGeneration.getMethodBodyContent(
                type.getCompilationUnit(), type.getTypeQualifiedName('.'),
                "onCreate", false, "", lineDelim); //$NON-NLS-1$ //$NON-NLS-2$
        if (content != null && content.length() != 0)
            buf.append(content);
        buf.append(lineDelim);
        buf.append("}"); //$NON-NLS-1$
        type.createMethod(buf.toString(), null, false, null);
    }

    private void generateOnStartCommand(IType type, ImportsManager imports)
            throws CoreException, JavaModelException {
        StringBuilder buf = new StringBuilder();
        final String lineDelim = "\n"; // OK, since content is formatted afterwards //$NON-NLS-1$
        buf.append("/* (non-Javadoc)").append(lineDelim);
        buf.append(
                "* @see android.app.Service#onStartCommand(android.content.Intent, int, int)")
                .append(lineDelim);
        buf.append("*/").append(lineDelim);
        buf.append("@Override");
        buf.append(lineDelim);
        buf.append("public int onStartCommand("); //$NON-NLS-1$
        buf.append(imports.addImport("android.content.Intent")); //$NON-NLS-1$
        buf.append(" intent, int flags, int startId) {"); //$NON-NLS-1$
        buf.append(lineDelim);
        
        final String content = CodeGeneration.getMethodBodyContent(
                type.getCompilationUnit(), type.getTypeQualifiedName('.'),
                "super.onStartCommand", false, "", lineDelim); //$NON-NLS-1$ //$NON-NLS-2$
        if (content != null && content.length() != 0)
            buf.append(content);
        buf.append(lineDelim);
        buf.append("return super.onStartCommand(intent, flags, startId);");
        buf.append(lineDelim);
        buf.append("}"); //$NON-NLS-1$
        type.createMethod(buf.toString(), null, false, null);

    }

    private void generateStub(String method, IType type, ImportsManager imports)
            throws CoreException, JavaModelException {
        List<String> supers = ProjectHelper.getSuperTypeName(getJavaProject(),
                getSuperClass(), false);
        if (supers.contains(AdtConstants.ACTIVITY_QNAME)
                && "onCreate".equals(method)) {
            generateOnCreate(type, imports);
            return;
        } else if (supers.contains(AdtConstants.SERVICE_QNAME)
                && "onStartCommand".equals(method)) {
            generateOnStartCommand(type, imports);
            return;
        }
        StringBuilder buf = new StringBuilder();
        final String lineDelim = "\n"; // OK, since content is formatted afterwards //$NON-NLS-1$
        buf.append("/* (non-Javadoc)").append(lineDelim);
        buf.append("* @see " + getSuperClass() + "#" + method + "()").append(
                lineDelim);
        buf.append("*/").append(lineDelim);
        buf.append("@Override");
        buf.append(lineDelim);
        buf.append("public void " + method + "(){"); //$NON-NLS-1$
        //

        buf.append(lineDelim);
        buf.append("super." + method + "();");
        buf.append(lineDelim);

        final String content = CodeGeneration.getMethodBodyContent(
                type.getCompilationUnit(), type.getTypeQualifiedName('.'),
                method, false, "", lineDelim); //$NON-NLS-1$ //$NON-NLS-2$
        if (content != null && content.length() != 0)
            buf.append(content);
        buf.append(lineDelim);
        buf.append("}"); //$NON-NLS-1$
        type.createMethod(buf.toString(), null, false, null);
    }

    private void createIntentCategoriesControl(final Composite composite,
            int nColumns, Set<String> categories) {
        GridData gridData = new GridData(SWT.FILL, SWT.TOP, true, false,
                nColumns, 1);
        categorySelector = new ElementTableSelector(composite, gridData,
                "Intent categories", "Select Intent categories",
                categories.toArray());
        // selectedCategories = selector.getSelectedElements();
    }

    private void createIntentActionsControl(final Composite composite,
            int nColumns, Set<String> actions) {
        GridData gridData = new GridData(SWT.FILL, SWT.TOP, true, false,
                nColumns, 1);
        actionSelector = new ElementTableSelector(composite, gridData,
                "Intent actions", "Select Intent actions", actions.toArray());
        // selectedActions = selector.getSelectedElements();
    }

    /**
     * Get intent categories
     * 
     * @return selected Intent categories
     */
    public String[] getSelectedCategories() {
        return asArray(categorySelector.getSelectedElements());
    }

    /**
     * Get intent actions
     * 
     * @return selected Intent actions
     */
    public String[] getSelectedActions() {
        return asArray(actionSelector.getSelectedElements());
    }

    @Override
    protected IStatus superClassChanged() {
        IStatus status = super.superClassChanged();
        List<String> supers = ProjectHelper.getSuperTypeName(getJavaProject(),
                getSuperClass(), false);
        if (supers.contains(AdtConstants.SERVICE_QNAME)) {
            updateMethods(SERVICE_METHODS);
        } else if (supers.contains(AdtConstants.ACTIVITY_QNAME)) {
            updateMethods(ACTIVITY_METHODS);
        } else if (supers.contains(AdtConstants.RECEIVER_QNAME)) {
            updateMethods(RECEIVER_METHODS);
        } else {
            updateMethods(new String[] {});
        }
        return status;
    }

    /**
     * helper method.
     * 
     * @param set
     *            set of intents or categories
     * @return set as array
     */
    private String[] asArray(Set<String> set) {
        List<String> list = new ArrayList<String>();
        for (String entry : set) {
            list.add(entry);
        }
        return list.toArray(new String[] {});
    }
}
