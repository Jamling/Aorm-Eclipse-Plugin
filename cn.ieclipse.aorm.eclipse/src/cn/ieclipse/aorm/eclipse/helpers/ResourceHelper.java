/**
 * 
 */
package cn.ieclipse.aorm.eclipse.helpers;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;

import cn.ieclipse.aorm.eclipse.AormPlugin;

/**
 * @author Jamling
 * 
 */
public class ResourceHelper {
    protected Class<?> systemR;
    protected Class<?> projectR;
    private final IJavaProject javaProject = null;

    public ResourceHelper(IJavaProject javaProject, String pkgName) {
        load(javaProject, pkgName);
    }

    public void load(IJavaProject javaProject, String pkgName) {
        try {
            ClassLoader classLoader = ResourceHelper.class.getClassLoader();
            File androidJar = new File(
                    ProjectHelper.getAndroidJarFromClasspath(javaProject));
            URL url = androidJar.toURI().toURL();
            URL[] urls = new URL[] { url };
            URLClassLoader urlCL = new URLClassLoader(urls, classLoader);
            systemR = Class.forName("android.R", true, urlCL);

            // load project R
            IPath path = javaProject.getProject().getWorkspace().getRoot()
                    .getLocation();
            path = path.append(javaProject.getOutputLocation()).makeAbsolute()
                    .append(pkgName);
            if (path != null) {
                url = new URL("file://" + path.toOSString());
                urls = new URL[] { url };
                urlCL = new URLClassLoader(urls, classLoader);
                projectR = urlCL.loadClass("R");// Class.forName("R", true,
                                                // urlCL);
            }
        } catch (Exception e) {
            // actions and intents will remain empty
            AormPlugin.log(e, "unable to get Intent actions and categories",
                    (Object[]) null);
        }
    }

    private String getProjectR(IJavaProject javaProject) throws CoreException {
        IPackageFragmentRoot gen = javaProject.getPackageFragmentRoot("gen");

        if (gen != null && gen.exists()) {
            IResource r = findR(gen.getResource());
            System.out.println("R:" + r);
        }

        ICompilationUnit unit = null;
        // unit.getT
        return null;
    }

    private IResource findR(IResource f) throws CoreException {
        if (f instanceof IFolder) {
            IFolder dir = (IFolder) f;
            IResource[] fs = dir.members();
            if (fs != null) {
                for (IResource sub : fs) {
                    IResource temp = findR(sub);
                    if (temp != null) {
                        return temp;
                    }
                }
            }
        } else {
            if (f.getName().equals("R")) {
                return f;
            }
        }
        return null;
    }

}
