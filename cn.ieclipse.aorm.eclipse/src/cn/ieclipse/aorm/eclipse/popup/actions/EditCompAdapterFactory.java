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

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.ui.IActionFilter;

public class EditCompAdapterFactory implements IAdapterFactory {

    private static final Class<?>[] ADAPTER = { IActionFilter.class };

    public Object getAdapter(Object adaptableObject, Class adapterType) {
        // System.out.println("1:" + adaptableObject.getClass() + ","
        // + adapterType);
        if (IActionFilter.class.equals(adapterType)) {
            if (adaptableObject instanceof ICompilationUnit) {
                EditCompActionFilter filter = EditCompActionFilter
                        .getInstance();
                filter.setUnit((ICompilationUnit) adaptableObject);
                return filter;
            }
        }
        return null;
    }

    public Class[] getAdapterList() {
        // System.out.println("2");
        return ADAPTER;
    }

}
