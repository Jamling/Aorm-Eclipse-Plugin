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

import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionFilter;
import org.eclipse.ui.PlatformUI;

import cn.ieclipse.aorm.eclipse.helpers.AdtConstants;
import cn.ieclipse.aorm.eclipse.helpers.ProjectHelper;

public class EditCompActionFilter implements IActionFilter {
    
    private static final EditCompActionFilter instance = new EditCompActionFilter();
    
    private EditCompActionFilter() {
        
    }
    
    public static EditCompActionFilter getInstance() {
        return instance;
    }
    
    public boolean testAttribute(Object target, String name, String value) {
        if ("type".equals(name)) {
            ICompilationUnit selection = null;
            try {
                ISelection sel = PlatformUI.getWorkbench()
                        .getActiveWorkbenchWindow().getActivePage()
                        .getActivePart().getSite().getSelectionProvider()
                        .getSelection();
                if (sel == null) {
                    return false;
                }
                if (sel instanceof IStructuredSelection) {
                    IJavaElement jele = ProjectHelper
                            .getInitialJavaElement((IStructuredSelection) sel);
                    if (jele instanceof ICompilationUnit) {
                        selection = (ICompilationUnit) jele;
                    }
                }
            } catch (Exception e) {
                System.out.println(e.toString());
            }
            if (selection != null) {
                Set<String> supers = ProjectHelper.getSuperTypeName(selection,
                        false);
                if ("activity".equals(value)) {
                    if (supers.contains(AdtConstants.ACTIVITY_QNAME)
                            || supers.contains(AdtConstants.SERVICE_QNAME)
                            || supers.contains(AdtConstants.RECEIVER_QNAME)) {
                        return true;
                    }
                }
                else if ("provider".equals(value)) {
                    if (supers.contains(AdtConstants.PROVIDER_QNAME)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
}
