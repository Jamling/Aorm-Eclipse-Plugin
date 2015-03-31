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

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;

/**
 * @author Jamling
 * 
 */
public class AormClasspathContainer implements IClasspathContainer {
    public static final String CON_PATH = "cn.ieclipse.aorm.eclipse.LIB";
    public static final String LIB_NAME = "Android ORM";
    // public static final IPath PATH = new Path(CON_PATH);
    public IClasspathEntry[] entries;
    public IPath path;

    AormClasspathContainer(IClasspathEntry[] entries, IPath path) {
        this.entries = entries;
        this.path = path;
    }

    public IClasspathEntry[] getClasspathEntries() {
        return entries;
    }

    public String getDescription() {
        return LIB_NAME;
    }

    public int getKind() {
        return IClasspathContainer.K_DEFAULT_SYSTEM;
    }

    public IPath getPath() {
        return path;
    }

}
