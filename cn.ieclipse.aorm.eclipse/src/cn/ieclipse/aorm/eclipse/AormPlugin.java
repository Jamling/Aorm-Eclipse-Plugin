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
package cn.ieclipse.aorm.eclipse;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import cn.ieclipse.aorm.eclipse.helpers.Status;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author Jamling
 */
public class AormPlugin extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "cn.ieclipse.aorm.eclipse"; //$NON-NLS-1$

    // The shared instance
    private static AormPlugin plugin;

    /**
     * The constructor
     */
    public AormPlugin() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
     * )
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
     * )
     */
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static AormPlugin getDefault() {
        return plugin;
    }

    /**
     * Returns an image descriptor for the image file at the given plug-in
     * relative path
     * 
     * @param path
     *            the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

    public static void log(int severity, String format, Object... args) {
        if (format == null) {
            return;
        }
        String message = String.format(format, args);
        Status status = new Status();
        status.setMessage(message);
        status.setSeverity(severity);
        if (getDefault() != null) {
            getDefault().getLog().log(status);
        } else {
            ((severity < 4) ? System.out : System.err).println(status
                    .toString());
        }
    }

    public static void log(Throwable exception, String format, Object... args) {
        String message = null;
        if (format != null)
            message = String.format(format, args);
        else
            message = "";

        Status status = new Status();
        status.setError(message);
        status.setException(exception);
        if (getDefault() != null) {
            getDefault().getLog().log(status);
        } else {
            System.err.println(status.toString());
        }
    }
}
