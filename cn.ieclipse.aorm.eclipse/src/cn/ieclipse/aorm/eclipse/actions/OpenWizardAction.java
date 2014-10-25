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
package cn.ieclipse.aorm.eclipse.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.LegacyResourceSupport;
import org.eclipse.ui.internal.util.Util;

/**
 * The is abstract "Open Wizard" action class. defining the wizard size,
 * initialize the selection.
 * 
 * @author Jamling
 * 
 */
public abstract class OpenWizardAction implements
        IWorkbenchWindowActionDelegate, IObjectActionDelegate {
    private static final int SIZING_WIZARD_WIDTH = 500;
    private static final int SIZING_WIZARD_HEIGHT = 500;
    private IWorkbenchWizard mWizard;
    private int mDialogResult;
    private ISelection mSelection;
    private IWorkbench mWorkbench;

    public IWorkbenchWizard getWizard() {
        return this.mWizard;
    }

    public int getDialogResult() {
        return this.mDialogResult;
    }

    public void dispose() {
    }

    public void init(IWorkbenchWindow window) {
    }

    public void run(IAction action) {
        IWorkbench workbench = (this.mWorkbench != null) ? this.mWorkbench
                : PlatformUI.getWorkbench();
        IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();

        ISelection selection = this.mSelection;
        if (selection == null) {
            selection = window.getSelectionService().getSelection();
        }

        IStructuredSelection selectionToPass = StructuredSelection.EMPTY;
        if (selection instanceof IStructuredSelection) {
            selectionToPass = (IStructuredSelection) selection;
        } else {
            IWorkbenchPart part = window.getPartService().getActivePart();
            if (part instanceof IEditorPart) {
                IEditorInput input = ((IEditorPart) part).getEditorInput();
                Class fileClass = LegacyResourceSupport.getFileClass();
                if ((input != null) && (fileClass != null)) {
                    Object file = Util.getAdapter(input, fileClass);
                    if (file != null) {
                        selectionToPass = new StructuredSelection(file);
                    }
                }

            }

        }

        this.mWizard = instanciateWizard(action);
        this.mWizard.init(workbench, selectionToPass);

        Shell parent = window.getShell();
        WizardDialog dialog = new WizardDialog(parent, this.mWizard);
        dialog.create();

        // Point defaultSize = dialog.getShell().getSize();
        // dialog.getShell().setSize(
        // Math.max(500, defaultSize.x),
        // Math.max(500, defaultSize.y));
        window.getWorkbench()
                .getHelpSystem()
                .setHelp(dialog.getShell(),
                        "org.eclipse.ui.new_wizard_shortcut_context");

        this.mDialogResult = dialog.open();
    }

    protected abstract IWorkbenchWizard instanciateWizard(IAction paramIAction);

    public void selectionChanged(IAction action, ISelection selection) {
        this.mSelection = selection;
    }

    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        this.mWorkbench = targetPart.getSite().getWorkbenchWindow()
                .getWorkbench();
    }
}