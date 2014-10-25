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

import org.eclipse.core.runtime.IStatus;

import cn.ieclipse.aorm.eclipse.AormPlugin;

/**
 * @author Jamling
 * 
 */
public class Status extends org.eclipse.core.runtime.Status {

    public Status(int severity, String message, Throwable exception) {
        super(severity, AormPlugin.PLUGIN_ID, message, exception);
    }

    public Status(int severity, String message) {
        this(severity, message, null);
    }

    public Status() {
        this(IStatus.OK, null);
    }

    public void setError(String message) {
        setSeverity(IStatus.ERROR);
        setMessage(message);
    }

    public void setWarning(String message) {
        setSeverity(IStatus.WARNING);
        setMessage(message);
    }

    public void setOK() {
        setSeverity(OK);
        setMessage(null);
    }

    @Override
    public void setSeverity(int severity) {
        super.setSeverity(severity);
    }

    @Override
    public void setMessage(String message) {
        super.setMessage(message);
    }

    @Override
    public void setException(Throwable exception) {
        super.setException(exception);
    }

    public static void main(String[] args) {
        Status st = new Status();
        System.out.println(st);
        st.setError("error");
        System.out.println(st);
        st.setOK();
        System.out.println(st);
    }
}
