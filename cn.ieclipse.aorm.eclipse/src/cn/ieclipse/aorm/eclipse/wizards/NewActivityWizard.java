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

import cn.ieclipse.aorm.eclipse.AormPlugin;
import cn.ieclipse.aorm.eclipse.helpers.AndroidManifest;

/**
 * Wizard class for creating a new Activity
 * 
 * @author Michael Kober
 */
public class NewActivityWizard extends NewComponentWizard {

    /**
     * Wizard id.
     */
    public static final String ID = AormPlugin.PLUGIN_ID
            + "wizards.NewActivityWizard";

    /**
     * Creates a new android activity project wizard.
     */
    public NewActivityWizard() {
        setWindowTitle("New Android Activity");
        page = new NewActivityWizardPage();
    }
    public NewActivityWizard (String type){
        setWindowTitle("New Android Activity");
        page = new NewActivityWizardPage();
    }

    @Override
    protected void updateManifest(AndroidManifest manifest) {
        NewActivityWizardPage newActivityPage = (NewActivityWizardPage) page;
        String[] selectedActions = newActivityPage.getSelectedActions();
        String[] selectedCategories = newActivityPage.getSelectedCategories();
        String superName = newActivityPage.getSuperClass();
        String activityName = newActivityPage.getPackageText() + "."
                + newActivityPage.getTypeName();
        manifest.addActivity(superName, activityName, selectedActions,
                selectedCategories);
    }
}
