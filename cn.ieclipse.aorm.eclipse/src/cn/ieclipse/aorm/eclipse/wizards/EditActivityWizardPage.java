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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import cn.ieclipse.aorm.eclipse.AormPlugin;
import cn.ieclipse.aorm.eclipse.helpers.ImageConstants;
import cn.ieclipse.aorm.eclipse.helpers.IntentReflectionHelper;
import cn.ieclipse.aorm.eclipse.helpers.ProjectHelper;

/**
 * @author melord
 * 
 */
public class EditActivityWizardPage extends WizardPage {
    IntentReflectionHelper helper;
    IJavaProject javaProject;
    private ElementTableSelector actionSelector;
    private ElementTableSelector categorySelector;
    
    /**
     * @wbp.parser.constructor
     */
    public EditActivityWizardPage(String pageName) {
        this(pageName, "Configure Component intent-filter", AormPlugin
                .getImageDescriptor(ImageConstants.LARGE_ACTIVITY_ICON));
    }
    
    public EditActivityWizardPage(String pageName, String title,
            ImageDescriptor titleImage) {
        super(pageName, title, titleImage);
    }
    
    public void init(IStructuredSelection selection) {
        IJavaElement ele = ProjectHelper.getInitialJavaElement(selection);
        if (ele != null) {
            javaProject = ele.getJavaProject();
        }
        helper = new IntentReflectionHelper(javaProject);
    }
    
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        container.setFont(parent.getFont());
        int nColumns = 1;
        GridLayout layout = new GridLayout();
        layout.numColumns = nColumns;
        createIntentActionsControl(container, 1, helper.getActions());
        createIntentCategoriesControl(container, 1, helper.getCategories());
        container.setLayout(layout);
        
        setControl(container);
        Dialog.applyDialogFont(container);
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
