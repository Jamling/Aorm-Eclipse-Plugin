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

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Composite;

import cn.ieclipse.aorm.eclipse.AormPlugin;
import cn.ieclipse.aorm.eclipse.helpers.AndroidManifest;
import cn.ieclipse.aorm.eclipse.helpers.ComponentAttribute;
import cn.ieclipse.aorm.eclipse.helpers.ProjectHelper;
import cn.ieclipse.aorm.eclipse.helpers.Status;

/**
 * @author Jamling
 * 
 */
public class EditActivityWizard extends EditComponentWizard {
    
    /**
     * Wizard id.
     */
    public static final String ID = AormPlugin.PLUGIN_ID
            + "wizards.EditActivityWizard";
    
    EditComponentWizardPage page0;
    EditActivityWizardPage page;
    
    /**
	 * 
	 */
    public EditActivityWizard() {
        super("Edit Android Activity");
    }
    
    @Override
    public void addPages() {
        super.addPages();
        if (page0 == null && nodeName != null) {
            page0 = new EditComponentWizardPage("Edit Component attributes");
            page0.setComponentType(nodeName);
            
            if (jProject != null) {
                IFile manifestFile = ProjectHelper.getManifestLocation(jProject
                        .getProject());
                if (manifestFile != null) {
                    try {
                        AndroidManifest manifest = new AndroidManifest(
                                manifestFile.getLocation().toOSString(),
                                jProject);
                        List<ComponentAttribute> initAttrs = manifest
                                .getComponentAttribute(nodeName, compName);
                        page0.setInitAttributes(initAttrs);
                    } catch (Exception e) {
                        Status status = new Status();
                        status.setError(e.toString());
                        ErrorDialog.openError(getShell(),
                                "Error when updating manifest", e.getMessage(),
                                status);
                    }
                }
            }
        }
        addPage(page0);
        // if (page == null) {
        // page = new
        // EditActivityWizardPage("Edit Component action & category");
        // page.init(selection);
        // }
        // addPage(page);
    }
    
    @Override
    public void createPageControls(Composite pageContainer) {
        super.createPageControls(pageContainer);
    }
    
    @Override
    public boolean canFinish() {
        // only allow the user to finish if the current page is the last page.
        return super.canFinish() && getContainer().getCurrentPage() == page0;
    }
    
    @Override
    protected void updateManifest(AndroidManifest manifest) {
        manifest.setComponentAttribute(nodeName, compName, page0.attributes);
        super.updateManifest(manifest);
    }
}
