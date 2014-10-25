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
package cn.ieclipse.aorm.eclipse.jdt;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * @author Jamling
 * 
 */
public class JavaSelection {
    private IJavaProject project;

    private Set<ICompilationUnit> units = new HashSet<ICompilationUnit>();

    public JavaSelection(ISelection selection) {
        if (selection != null && selection instanceof IStructuredSelection
                && !selection.isEmpty()) {
            @SuppressWarnings("unchecked")
            List<Object> list = ((IStructuredSelection) selection).toList();
            for (int i = 0; i < list.size(); i++) {
                Object obj = list.get(i);
                if (obj instanceof IProject) {
                    IProject prj = (IProject) obj;
                    try {
                        iterate(JavaCore.create(prj));
                    } catch (JavaModelException e) {
                        // TODO
                        continue;
                    }
                } else if (obj instanceof IJavaElement) {
                    try {
                        iterate((IJavaElement) obj);
                    } catch (JavaModelException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        continue;
                    }
                }

            }
        }
    }

    public IJavaProject getProject() {
        return project;
    }

    public Set<ICompilationUnit> getUnits() {
        return units;
    }

    public List<TypeMapping> getTypeMappings() {
        ArrayList<TypeMapping> list = new ArrayList<TypeMapping>();
        ICompilationUnit[] arrays = units.toArray(new ICompilationUnit[] {});
        for (ICompilationUnit unit : arrays) {
            IType[] types;
            try {
                types = unit.getAllTypes();
            } catch (JavaModelException e) {
                // TODO
                e.printStackTrace();
                continue;
            }
            for (IType type : types) {
                IAnnotation nt = type.getAnnotation("Table");
                if (nt != null && nt.exists()) {
                    IMemberValuePair[] tVars;
                    String table = "";
                    try {
                        tVars = nt.getMemberValuePairs();
                        for (IMemberValuePair tVar : tVars) {
                            if ("name".equals(tVar.getMemberName())) {
                                table = (String) tVar.getValue();
                            }
                        }
                        list.add(new TypeMapping(table, type));
                    } catch (JavaModelException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
            }
        }
        return list;
    }

    private void iterate(IJavaElement element) throws JavaModelException {
        if (project == null) {
            project = element.getJavaProject();
        }
        if (element instanceof IJavaProject) {
            IJavaProject prj = (IJavaProject) element;
            IPackageFragmentRoot[] roots = prj.getAllPackageFragmentRoots();
            for (IPackageFragmentRoot root : roots) {
                iterate(root);
            }
            return;
        } else if (element instanceof IPackageFragmentRoot) {
            IPackageFragmentRoot root = (IPackageFragmentRoot) element;
            if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
                IJavaElement[] children = root.getChildren();
                for (IJavaElement child : children) {
                    iterate(child);
                }
            }
        } else if (element instanceof IPackageFragment) {
            IPackageFragment pkg = (IPackageFragment) element;
            if (pkg.getKind() == IPackageFragmentRoot.K_SOURCE) {
                ICompilationUnit[] units = pkg.getCompilationUnits();
                for (ICompilationUnit unit : units) {
                    this.units.add(unit);
                }
                IJavaElement[] children = pkg.getChildren();
                for (IJavaElement child : children) {
                    iterate(child);
                }
            }
        } else if (element instanceof ICompilationUnit) {
            ICompilationUnit unit = (ICompilationUnit) element;
            this.units.add(unit);
        }
    }

    public static class TypeMapping {
        private boolean checked;
        private String table;
        private IType type;

        public TypeMapping(String table, IType type) {
            this.table = table;
            this.type = type;
        }

        public String getTable() {
            return table;
        }

        public IType getType() {
            return type;
        }

        public void setChecked(boolean checked) {
            this.checked = checked;
        }

        public boolean isChecked() {
            return checked;
        }
    }
}
