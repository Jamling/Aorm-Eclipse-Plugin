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
import org.eclipse.ui.IWorkbenchWizard;

import cn.ieclipse.aorm.eclipse.wizards.EditComponentWizard;

/**
 * Edit activity action to open editing activity wizard page.
 * 
 * @author Jamling
 * 
 */
public class EditActivityAction extends OpenWizardAction {

    @Override
    protected IWorkbenchWizard instanciateWizard(IAction paramIAction) {
        return new EditComponentWizard();
    }

}
