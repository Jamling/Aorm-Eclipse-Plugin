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

import cn.ieclipse.aorm.eclipse.AormPlugin;
import cn.ieclipse.aorm.eclipse.helpers.AndroidManifest;

/**
 * @author melord
 * 
 */
public class EditProviderWizard extends EditComponentWizard {

    /**
     * Wizard id.
     */
    public static final String ID = AormPlugin.PLUGIN_ID
            + "wizards.EditProviderWizard";

    EditComponentWizardPage page0;

    public EditProviderWizard() {
        super("Edit Android ContentProvider");
    }

    @Override
    public void addPages() {
        super.addPages();
        if (page0 == null && nodeName != null) {
            page0 = new EditComponentWizardPage("Edit Component attributes");
            page0.setComponentType(nodeName);
        }
        addPage(page0);
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
