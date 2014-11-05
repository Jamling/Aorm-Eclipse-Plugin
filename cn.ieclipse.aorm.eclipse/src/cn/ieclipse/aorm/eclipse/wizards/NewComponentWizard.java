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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.wizards.NewElementWizard;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;

import cn.ieclipse.aorm.eclipse.AormPlugin;
import cn.ieclipse.aorm.eclipse.helpers.AndroidManifest;
import cn.ieclipse.aorm.eclipse.helpers.ImageConstants;
import cn.ieclipse.aorm.eclipse.helpers.ProjectHelper;
import cn.ieclipse.aorm.eclipse.helpers.Status;

/**
 * Wizard class for creating a Android component. such Activity,Provider
 * 
 * @author Melord Li
 */
@SuppressWarnings("restriction")
public class NewComponentWizard extends NewElementWizard {
    protected NewComponentWizardPage page = null;
    
    /**
     * Creates a new android component wizard. set default title and page icon
     */
    public NewComponentWizard() {
        setWindowTitle("New Android Component Wizard");
        setDefaultPageImageDescriptor(AormPlugin
                .getImageDescriptor(ImageConstants.LARGE_ACTIVITY_ICON));
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jdt.internal.ui.wizards.NewElementWizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        boolean res = super.performFinish();
        if (res) {
            IResource resource = page.getModifiedResource();
            if (resource != null) {
                IProject project = page.getJavaProject().getProject();
                IFile manifestFile = ProjectHelper.getManifestLocation(project);
                if (manifestFile != null) {
                    try {
                        AndroidManifest manifest = new AndroidManifest(
                                manifestFile.getLocation().toOSString(),
                                page.getJavaProject());
                        
                        updateManifest(manifest);
                        manifest.save2();
                        resource.refreshLocal(0, null);
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
                openResource((IFile) resource);
            }
        }
        return res;
    }
    
    /**
     * apply change to AndroidManifest.xml
     * 
     * @param manifest
     */
    protected void updateManifest(AndroidManifest manifest) {
        
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    @Override
    public void addPages() {
        addPage(page);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jdt.internal.ui.wizards.NewElementWizard#init(org.eclipse
     * .ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
     */
    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        page.init(selection);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.Wizard#canFinish()
     */
    @Override
    public boolean canFinish() {
        // only allow the user to finish if the current page is the last page.
        return super.canFinish() && getContainer().getCurrentPage() == page;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jdt.internal.ui.wizards.NewElementWizard#finishPage(org.eclipse
     * .core.runtime.IProgressMonitor)
     */
    @Override
    protected void finishPage(IProgressMonitor monitor)
            throws InterruptedException, CoreException {
        page.createType(monitor);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jdt.internal.ui.wizards.NewElementWizard#getCreatedElement()
     */
    @Override
    public IJavaElement getCreatedElement() {
        return page.getCreatedType();
    }
}
