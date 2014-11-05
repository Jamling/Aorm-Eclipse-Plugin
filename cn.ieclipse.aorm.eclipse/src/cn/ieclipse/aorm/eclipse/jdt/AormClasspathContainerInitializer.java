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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.osgi.framework.Bundle;

import cn.ieclipse.aorm.eclipse.AormPlugin;

/**
 * @author Jamling
 * 
 */
public class AormClasspathContainerInitializer extends
        ClasspathContainerInitializer {
    private static final String RESOURCE_LIB = "libs";
    private static final String FS = System.getProperty("file.separator");
    private static final String AORM_NAME = "aorm";
    
    public static final IClasspathEntry getContainerEntry() {
        return JavaCore.newContainerEntry(new Path(
                AormClasspathContainer.CON_PATH));
    }
    
    @Override
    public void initialize(IPath containerPath, IJavaProject project)
            throws CoreException {
        if (AormClasspathContainer.CON_PATH.equals(containerPath.toString())) {
            IClasspathContainer container = allocateAndroidContainer(project);
            if (container != null)
                JavaCore.setClasspathContainer(new Path(
                        AormClasspathContainer.CON_PATH),
                        new IJavaProject[] { project },
                        new IClasspathContainer[] { container },
                        new NullProgressMonitor());
        }
        
    }
    
    private IClasspathContainer allocateAndroidContainer(IJavaProject project) {
        IClasspathContainer ormContainer = new AormClasspathContainer(
                getClasspathEntries(),
                new Path(AormClasspathContainer.CON_PATH));
        return ormContainer;
    }
    
    public static boolean invalidOrmClassPath(IPath path) {
        boolean ret = false;
        String file = path.lastSegment();
        if (file.startsWith(AORM_NAME)) {
            Bundle bundle = AormPlugin.getDefault().getBundle();
            Enumeration<URL> urls = bundle.findEntries(RESOURCE_LIB, file,
                    false);
            ret = urls == null || !urls.hasMoreElements();
        }
        return ret;
    }
    
    public static IClasspathEntry[] getClasspathEntries() {
        ArrayList<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
        Bundle bundle = AormPlugin.getDefault().getBundle();
        Enumeration<URL> urls = bundle
                .findEntries(RESOURCE_LIB, "*.jar", false);
        ArrayList<Path> paths = new ArrayList<Path>();
        if (urls != null) {
            while (urls.hasMoreElements()) {
                URL url = (URL) urls.nextElement();
                try {
                    url = FileLocator.resolve(url);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                Path path = new Path(url.getPath());
                paths.add(path);
            }
        }
        
        Path libPath = getJarPath(AORM_NAME, paths);
        if (libPath != null) {
            Path docPath = getJarPath(AORM_NAME + ".doc", paths);
            Path sourcePath = getJarPath(AORM_NAME + ".source", paths);
            IClasspathEntry entry = JavaCore.newLibraryEntry(libPath,
                    sourcePath, null, null, null, true);
            
            entries.add(entry);
        }
        return entries.toArray(new IClasspathEntry[entries.size()]);
    }
    
    private static Path getJarPath(String name, ArrayList<Path> paths) {
        Path ret = null;
        for (Path path : paths) {
            String fn = path.lastSegment();
            if (fn != null) {
                boolean isLib = fn.contains(name + "_")
                        || fn.contains(name + "-");
                if (isLib) {
                    ret = path;
                    break;
                }
            }
        }
        return ret;
    }
}
