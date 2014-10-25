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
package cn.ieclipse.aorm.eclipse.helpers;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;

import cn.ieclipse.aorm.eclipse.AormPlugin;

/**
 * @author Jamling
 * 
 */
public class ProjectHelper {
    private static final String MANIFEST_FILE = "AndroidManifest.xml";

    public static void addOrRemoveEntryToClasspath(IJavaProject javaProject,
            IClasspathEntry newEntry) throws JavaModelException {
        int idx = isEntryInClasspath(javaProject, newEntry);
        IClasspathEntry[] entries = javaProject.getRawClasspath();
        if (idx >= 0) {
            entries = removeEntryFromClasspath(entries, idx);
        } else {
            entries = addEntryToClasspath(entries, newEntry);
        }
        javaProject.setRawClasspath(entries, new NullProgressMonitor());
    }

    public static int isEntryInClasspath(IJavaProject javaProject,
            IClasspathEntry newEntry) throws JavaModelException {
        IClasspathEntry[] entries = javaProject.getRawClasspath();
        int idx = -1;
        int i = 0;
        for (IClasspathEntry entry : entries) {
            if (entry.equals(newEntry)) {
                idx = i;
                break;
            }
            i++;
        }
        return idx;
    }

    public static boolean isContainerInClasspath(IJavaProject javaProject,
            IClasspathContainer container) throws JavaModelException {
        IClasspathEntry[] entries = javaProject.getRawClasspath();
        // IClasspathEntry[] temps = container.getClasspathEntries();
        boolean result = false;

        for (IClasspathEntry entry : entries) {
            if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER
                    && entry.getPath().equals(container.getPath())) {
                result = true;
                break;
            }
        }
        return result;
    }

    /**
     * Get the location of the AndroidManifest.xml.
     * 
     * @return manifest location as IPath
     */
    public static IFile getManifestLocation(IProject project) {
        IResource resource = project.findMember(MANIFEST_FILE);
        boolean resourceFound = ((resource != null) && ((resource.exists())) && (resource instanceof IFile));
        return resourceFound ? ((IFile) resource) : null;
    }

    public static AndroidManifest getAndroidManifest(IJavaElement jEle) {
        AndroidManifest manifest = null;
        IProject prj = null;
        IJavaProject jprj = jEle.getJavaProject();
        // if (jEle instanceof IJavaProject) {
        // prj = jEle.getJavaProject().getProject();
        // } else {
        // prj = (IProject) jEle;
        // }
        prj = jprj.getProject();
        IFile file = getManifestLocation(prj);
        if (file != null) {
            try {
                manifest = new AndroidManifest(file.getLocation().toOSString(),
                        jprj);
            } catch (Exception e) {

            }
        }
        return manifest;
    }

    private static IClasspathEntry[] removeEntryFromClasspath(
            IClasspathEntry[] entries, int index) {
        int n = entries.length;
        IClasspathEntry[] newEntries = new IClasspathEntry[n - 1];

        System.arraycopy(entries, 0, newEntries, 0, index);

        System.arraycopy(entries, index + 1, newEntries, index, entries.length
                - index - 1);

        return newEntries;
    }

    private static IClasspathEntry[] addEntryToClasspath(
            IClasspathEntry[] entries, IClasspathEntry newEntry) {
        int n = entries.length;
        IClasspathEntry[] newEntries = new IClasspathEntry[n + 1];
        System.arraycopy(entries, 0, newEntries, 0, n);
        newEntries[n] = newEntry;
        return newEntries;
    }

    // /

    /**
     * Utility method to inspect a selection to find a Java element.
     * 
     * @param selection
     *            the selection to be inspected
     * @return a Java element to be used as the initial selection, or
     *         <code>null</code>, if no Java element exists in the given
     *         selection
     */
    public static IJavaElement getInitialJavaElement(
            IStructuredSelection selection) {
        IJavaElement jelem = null;
        if (selection != null && !selection.isEmpty()) {
            Object selectedElement = selection.getFirstElement();
            if (selectedElement instanceof IAdaptable) {
                IAdaptable adaptable = (IAdaptable) selectedElement;

                jelem = (IJavaElement) adaptable.getAdapter(IJavaElement.class);
                if (jelem == null || !jelem.exists()) {
                    jelem = null;
                    IResource resource = (IResource) adaptable
                            .getAdapter(IResource.class);
                    if (resource != null
                            && resource.getType() != IResource.ROOT) {
                        while (jelem == null
                                && resource.getType() != IResource.PROJECT) {
                            resource = resource.getParent();
                            jelem = (IJavaElement) resource
                                    .getAdapter(IJavaElement.class);
                        }
                        if (jelem == null) {
                            jelem = JavaCore.create(resource); // java project
                        }
                    }
                }
            }
        }
        return jelem;
    }

    public static Set<IType> getSuperType(ICompilationUnit unit,
            boolean includeInterface) {
        Set<IType> set = new HashSet<IType>();
        try {
            IType[] types = unit.getTypes();
            if (null != types && types.length > 0) {
                ITypeHierarchy typeHierarchy = types[0]
                        .newSupertypeHierarchy(null);
                IType[] superclass = typeHierarchy.getAllSupertypes(types[0]);
                for (IType type : superclass) {
                    if (type.isInterface() && includeInterface) {
                        set.add(type);
                    } else {
                        set.add(type);
                    }

                }
            }
        } catch (Exception e) {
        }
        return set;
    }

    public static Set<String> getSuperTypeName(ICompilationUnit unit,
            boolean includeInterface) {
        Set<String> set = new HashSet<String>();
        try {
            IType[] types = unit.getTypes();
            if (null != types && types.length > 0) {
                ITypeHierarchy typeHierarchy = types[0]
                        .newSupertypeHierarchy(null);
                IType[] superclass = typeHierarchy.getAllSupertypes(types[0]);
                for (IType type : superclass) {
                    if (type.isInterface()) {
                        if (includeInterface) {
                            set.add(type.getFullyQualifiedName());
                        }
                    } else {
                        set.add(type.getFullyQualifiedName());
                    }

                }
            }
        } catch (Exception e) {
        }
        return set;
    }

    public static List<String> getSuperTypeName(IJavaProject project,
            String className, boolean includeInterface) {
        List<String> set = new ArrayList<String>();
        try {
            IType type = project.findType(className);
            if (type != null) {
                set.add(className);
                ITypeHierarchy typeHierarchy = type.newSupertypeHierarchy(null);
                IType[] superclass = typeHierarchy.getAllSupertypes(type);
                for (IType s : superclass) {
                    if (s.isInterface()) {
                        if (includeInterface) {
                            set.add(s.getFullyQualifiedName());
                        }
                    } else {
                        set.add(s.getFullyQualifiedName());
                    }

                }
            }
        } catch (Exception e) {

        }
        return set;
    }

    public static List<String> getSuperTypeName(String className) {
        List<String> set = new ArrayList<String>();
        getSuperTypeName(className, set);
        return set;
    }

    private static void getSuperTypeName(String className, List<String> set) {
        Class<?> clazz;
        try {
            clazz = Class.forName(className);
            set.add(className);
            if (clazz.getSuperclass() != null) {
                getSuperTypeName(clazz.getSuperclass().getName(), set);
            }
        } catch (ClassNotFoundException e) {

        }
    }

    public static ArrayList<ComponentAttribute> getConfAttrs(String path) {
        ArrayList<ComponentAttribute> attrs = new ArrayList<ComponentAttribute>();
        InputStream is = AormPlugin.class.getResourceAsStream(path);
        if (is != null) {
            try {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(is));
                String line = br.readLine();
                boolean isKey = false;
                String key = "";
                StringBuilder val = new StringBuilder();
                while (line != null) {
                    int pos = line.indexOf('=');
                    if (pos > 0) {
                        if (isKey) {
                            ComponentAttribute att = new ComponentAttribute();
                            att.setName(key);
                            String value = getVal(val.toString());
                            att.setFormats(value);
                            val.delete(0, val.length());
                            isKey = false;
                            attrs.add(att);
                        }
                        isKey = true;
                        key = getKey(line, pos);
                        val.append(line.substring(pos + 1));
                    } else {
                        if (isKey) {
                            val.append(line);
                        }
                    }
                    line = br.readLine();
                }
                if (isKey) {
                    ComponentAttribute att = new ComponentAttribute();
                    att.setName(key);
                    String value = getVal(val.toString());
                    att.setFormats(value);
                    val.delete(0, val.length());
                    isKey = false;
                    attrs.add(att);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return attrs;
    }

    private static String getKey(String str, int pos) {
        String key = str.substring(0, pos);
        pos = key.indexOf("android:");
        if (pos > 0) {
            key = key.substring(pos);
        }
        return key;
    }

    private static String getVal(String str) {
        String ret = str;
        int pos = str.indexOf('>');
        if (pos > 0) {
            ret = str.substring(0, pos);
        }
        return ret;
    }

    public static void main(String[] args) {
        System.out.println(getConfAttrs("Activity.def"));
        System.out.println(getSuperTypeName("java.util.ArrayList"));
    }
}
