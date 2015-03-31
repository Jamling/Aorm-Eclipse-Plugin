/*
 * Copyright 2010 the original author or authors.
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

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jdt.core.IJavaProject;

import cn.ieclipse.aorm.eclipse.AormPlugin;

/**
 * Helper for getting the Intent categories and actions from the Intent.class
 * via reflection.
 * 
 * @author Michael Kober
 * 
 */
public class IntentReflectionHelper {

    private static final String ACTION_PREFIX = "ACTION_";
    private static final String CATEGORY_PREFIX = "CATEGORY_";
    private static final String PERMISSION_PREFIX = "android.permission";

    private final Set<String> categories = new TreeSet<String>();
    private final Set<String> actions = new TreeSet<String>();
    private final Set<String> permissions = new TreeSet<String>();
    private final IJavaProject javaProject;

    /**
     * Constructor.
     * 
     * @param javaProject
     *            current java project
     */
    public IntentReflectionHelper(IJavaProject javaProject) {
        this.javaProject = javaProject;
    }

    /**
     * Get the intent categories.
     * 
     * @return Set of intent categories.
     */
    public Set<String> getCategories() {
        if (categories.isEmpty()) {
            init();
        }
        return categories;
    }

    /**
     * Get the intent actions.
     * 
     * @return Set of intent actions.
     */
    public Set<String> getActions() {
        if (actions.isEmpty()) {
            init();
        }
        return actions;
    }

    /**
     * Get the Manifest permissions.
     * 
     * @return Set of Manifest permissions.
     */
    public Set<String> getPermissions() {
        if (permissions.isEmpty()) {
            init();
        }
        return permissions;
    }

    /**
     * Get categories and actions from the Intent.class
     */
    private void init() {
        try {
            ClassLoader classLoader = IntentReflectionHelper.class
                    .getClassLoader();
            File androidJar = new File(
                    ProjectHelper.getAndroidJarFromClasspath(javaProject));
            URL url = androidJar.toURI().toURL();
            URL[] urls = new URL[] { url };
            URLClassLoader urlCL = new URLClassLoader(urls, classLoader);
            Class<?> intent = Class.forName("android.content.Intent", true,
                    urlCL);
            Field[] declaredFields = intent.getDeclaredFields();
            for (Field field : declaredFields) {
                if (field.getName().startsWith(CATEGORY_PREFIX)) {
                    categories.add((String) field.get(null));
                } else if (field.getName().startsWith(ACTION_PREFIX)) {
                    actions.add((String) field.get(null));
                }
            }

            Class<?> permission = Class.forName("android.Manifest$permission",
                    true, urlCL);
            declaredFields = permission.getDeclaredFields();
            for (Field field : declaredFields) {
                Object temp = field.get(null);
                if (temp instanceof String) {
                    String str = (String) temp;
                    if (str.startsWith(PERMISSION_PREFIX)) {
                        permissions.add(str);
                    }
                }
            }
        } catch (Exception e) {
            // actions and intents will remain empty
            AormPlugin.log(e, "unable to get Intent actions and categories",
                    (Object[]) null);
        }
    }

}
