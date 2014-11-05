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

import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

import cn.ieclipse.aorm.eclipse.AormPlugin;
import cn.ieclipse.aorm.eclipse.helpers.AdtConstants;
import cn.ieclipse.aorm.eclipse.helpers.AndroidManifest;
import cn.ieclipse.aorm.eclipse.helpers.ImageConstants;
import cn.ieclipse.aorm.eclipse.helpers.ProjectHelper;
import cn.ieclipse.aorm.eclipse.helpers.Status;

/**
 * @author melord
 * 
 */
public class EditComponentWizard extends Wizard implements IWorkbenchWizard {
    
    IStructuredSelection selection;
    protected String nodeName;
    protected String compName;
    IJavaProject jProject;
    
    /**
	 * 
	 */
    public EditComponentWizard(String title) {
        setWindowTitle(title);
        setDefaultPageImageDescriptor(AormPlugin
                .getImageDescriptor(ImageConstants.LARGE_ACTIVITY_ICON));
        
    }
    
    protected void updateManifest(AndroidManifest manifest) {
        
    }
    
    @Override
    public boolean performFinish() {
        if (jProject != null) {
            IFile manifestFile = ProjectHelper.getManifestLocation(jProject
                    .getProject());
            if (manifestFile != null) {
                try {
                    AndroidManifest manifest = new AndroidManifest(manifestFile
                            .getLocation().toOSString(), jProject);
                    updateManifest(manifest);
                    manifest.save2();
                    manifestFile.refreshLocal(0, null);
                } catch (Exception e) {
                    Status status = new Status();
                    status.setError(e.toString());
                    ErrorDialog.openError(getShell(),
                            "Error when updating manifest", e.getMessage(),
                            status);
                }
            }
            else {
                Status status = new Status();
                status.setError("Could not find Android manifest file.");
                ErrorDialog.openError(getShell(),
                        "Error when updating manifest",
                        "No AndroidManifest.xml found", status);
            }
            return true;
        }
        return false;
    }
    
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
        IJavaElement jele = ProjectHelper
                .getInitialJavaElement((IStructuredSelection) selection);
        jProject = jele.getJavaProject();
        if (jele instanceof ICompilationUnit) {
            ICompilationUnit unit = (ICompilationUnit) jele;
            Set<String> supers = ProjectHelper.getSuperTypeName(unit, false);
            if (supers.contains(AdtConstants.ACTIVITY_QNAME)) {
                nodeName = AdtConstants.ACTIVITY_NODE;
            }
            else if (supers.contains(AdtConstants.SERVICE_QNAME)) {
                nodeName = AdtConstants.SERVICE_NODE;
            }
            else if (supers.contains(AdtConstants.RECEIVER_QNAME)) {
                nodeName = AdtConstants.RECEIVER_NODE;
            }
            else if (supers.contains(AdtConstants.PROVIDER_QNAME)) {
                nodeName = AdtConstants.PROVIDER_NODE;
            }
            try {
                compName = unit.getTypes()[0].getFullyQualifiedName();
            } catch (JavaModelException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // int pos = compName.indexOf('.');
            // compName = compName.substring(0, pos);
            System.out.println("node:" + nodeName + ",class:" + compName);
        }
    }
    
    @Override
    public boolean canFinish() {
        // only allow the user to finish if the current page is the last page.
        return super.canFinish();
    }
}
