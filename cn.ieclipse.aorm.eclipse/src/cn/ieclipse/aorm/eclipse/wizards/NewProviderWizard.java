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

import cn.ieclipse.aorm.eclipse.AormPlugin;
import cn.ieclipse.aorm.eclipse.helpers.AndroidManifest;

/**
 * Wizard class for creating a Android ContentProvider.
 * 
 * @author Jamling
 */

public class NewProviderWizard extends NewComponentWizard {
    /**
     * Wizard id.
     */
    public static final String ID = AormPlugin.PLUGIN_ID
            + "wizards.NewProviderWizard";
    
    /**
     * Constructor for ProviderNewWizard.
     */
    public NewProviderWizard() {
        super();
        setWindowTitle("New ContentProvider");
        page = new NewProviderWizardPage();
    }
    
    @Override
    protected void updateManifest(AndroidManifest manifest) {
        NewProviderWizardPage newProviderPage = (NewProviderWizardPage) page;
        final String pkgName = newProviderPage.getPackageText();
        final String superName = newProviderPage.getSuperClass();
        final String typeName = newProviderPage.getTypeName();
        final String authority = newProviderPage.getAuthority();
        manifest.addProvider(superName, pkgName + "." + typeName, authority);
    }
    
}