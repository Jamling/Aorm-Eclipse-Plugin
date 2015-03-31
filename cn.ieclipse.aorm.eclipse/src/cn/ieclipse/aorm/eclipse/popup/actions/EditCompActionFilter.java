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
import org.eclipse.ui.IActionFilter;

import cn.ieclipse.aorm.eclipse.helpers.AdtConstants;
import cn.ieclipse.aorm.eclipse.helpers.ProjectHelper;

public class EditCompActionFilter implements IActionFilter {

    private static EditCompActionFilter instance = null;

    private ICompilationUnit unit;

    private EditCompActionFilter() {

    }

    public void setUnit(ICompilationUnit unit) {
        this.unit = unit;
    }

    public static EditCompActionFilter getInstance() {
        if (instance == null) {
            synchronized (EditCompActionFilter.class) {
                instance = new EditCompActionFilter();
            }
        }
        return instance;
    }

    public boolean testAttribute(Object target, String name, String value) {
        // System.out.println(String.format("testAttribute:%s=%s", name,
        // value));
        if (this.unit != null && "ieclipse.type".equals(name)
                && "activity".equals(value)) {

            // System.out.println("super1："
            // + ProjectHelper.getSuperTypeName(unit.getJavaProject(),
            // unit.getTypes()[0].getFullyQualifiedName(),
            // false));
            Set<String> supers = ProjectHelper.getSuperTypeName(unit, false);
            if (supers.contains(AdtConstants.ACTIVITY_QNAME)
                    || supers.contains(AdtConstants.SERVICE_QNAME)
                    || supers.contains(AdtConstants.RECEIVER_QNAME)
                    || supers.contains(AdtConstants.PROVIDER_QNAME)) {
                return true;
            }
        }
        return false;
        // if ("ieclipse.type".equals(name)) {
        // ICompilationUnit selection = null;
        // try {
        // ISelection sel = PlatformUI.getWorkbench()
        // .getActiveWorkbenchWindow().getActivePage()
        // .getActivePart().getSite().getSelectionProvider()
        // .getSelection();
        // if (sel == null) {
        // return false;
        // }
        // if (sel instanceof IStructuredSelection) {
        // IJavaElement jele = ProjectHelper
        // .getInitialJavaElement((IStructuredSelection) sel);
        // System.out.println("java ele:" + jele.getClass());
        // if (jele instanceof ICompilationUnit) {
        // selection = (ICompilationUnit) jele;
        // }
        // System.out.println("super1："
        // + ProjectHelper.getSuperTypeName(jele
        // .getJavaProject(), selection.getTypes()[0]
        // .getFullyQualifiedName(), false));
        // }
        // } catch (Exception e) {
        // System.out.println(e.toString());
        // }
        // if (selection != null) {
        // Set<String> supers = ProjectHelper.getSuperTypeName(selection,
        // false);
        // System.out.println("supers:" + supers);
        //
        // if ("activity".equals(value)) {
        // if (supers.contains(AdtConstants.ACTIVITY_QNAME)
        // || supers.contains(AdtConstants.SERVICE_QNAME)
        // || supers.contains(AdtConstants.RECEIVER_QNAME)
        // || supers.contains(AdtConstants.PROVIDER_QNAME)) {
        // return true;
        // }
        // } else if ("provider".equals(value)) {
        // if (supers.contains(AdtConstants.PROVIDER_QNAME)) {
        // return true;
        // }
        // }
        // }
        // }
        // return false;
    }

}
