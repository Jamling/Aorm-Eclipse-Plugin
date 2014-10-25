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
package cn.ieclipse.aorm.eclipse.popup.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import cn.ieclipse.aorm.eclipse.helpers.ProjectHelper;
import cn.ieclipse.aorm.eclipse.jdt.AormClasspathContainerInitializer;

/**
 * @author Jamling
 * 
 */
public class AddLibAction implements IObjectActionDelegate {

    private Shell shell;

    private IJavaProject project;

    private IStructuredSelection selection;

    /**
     * Constructor for Action1.
     */
    public AddLibAction() {
        super();
    }

    /**
     * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
     */
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        shell = targetPart.getSite().getShell();
    }

    /**
     * @see IActionDelegate#run(IAction)
     */
    public void run(IAction action) {
        Object obj = selection.getFirstElement();
        project = null;
        if (obj instanceof IProject) {
            project = JavaCore.create((IProject) obj);
        } else if (obj instanceof IJavaProject) {
            project = (IJavaProject) obj;
        }
        try {
            IClasspathEntry[] entries = AormClasspathContainerInitializer
                    .getClasspathEntries();
            for (IClasspathEntry entry : entries) {
                ProjectHelper.addOrRemoveEntryToClasspath(project, entry);
            }

        } catch (JavaModelException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * project.getProject().getFile(
     * 
     * @see IActionDelegate#selectionChanged(IAction, ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
        if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
            this.selection = (IStructuredSelection) selection;
        } else {
            this.selection = null;
        }
    }

}
