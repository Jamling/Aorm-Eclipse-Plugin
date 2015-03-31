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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import cn.ieclipse.aorm.eclipse.jdt.JavaSelection;
import cn.ieclipse.aorm.eclipse.jdt.JavaSelection.TypeMapping;
import cn.ieclipse.aorm.eclipse.jdt.SourceAnalysis;

/**
 * @author Jamling
 * 
 */
public class GenDDLAction implements IObjectActionDelegate {

    private Shell shell;

    private IJavaProject project;

    private ISelection selection;

    /**
     * Constructor for Action1.
     */
    public GenDDLAction() {
        super();
    }

    /**
     * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
     */
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        shell = targetPart.getSite().getShell();
    }

    /**
     * @see IActionDelegate#run(IAction)
     */
    public void run(IAction action) {
        JavaSelection sel = new JavaSelection(selection);
        project = sel.getProject();
        String table = sel.getTypeMappings().get(0).getTable();
        StringBuilder sb = new StringBuilder();
        // generate ddl
        sb.append("//--->SQL DDL");
        sb.append(SourceAnalysis.LF);
        for (TypeMapping type : sel.getTypeMappings()) {
            sb.append(SourceAnalysis.getSQL(type, false));
        }
        // generate java code;
        StringBuilder sb2 = new StringBuilder();
        sb2.append("//--->Java code");
        sb2.append(SourceAnalysis.LF);
        sb2.append("String sql=\"\";");
        sb2.append(SourceAnalysis.LF);
        for (TypeMapping type : sel.getTypeMappings()) {
            String[] lines = SourceAnalysis.getSQL(type, true).split(
                    SourceAnalysis.LF);
            for (int i = 0; i < lines.length; i++) {
                if (i == 0) {
                    sb2.append("sql=\"");
                } else {
                    sb2.append("sql+=\"");
                }
                sb2.append(lines[i]);
                sb2.append("\";");
                sb2.append(SourceAnalysis.LF);
            }
            sb2.append("db.execSQL(sql);");
            sb2.append(SourceAnalysis.LF);
        }
        sb.append(sb2);

        InputStream source = new ByteArrayInputStream(sb.toString().getBytes());
        IFile file = project.getProject().getFile(table + ".sql");
        try {
            if (file.exists()) {
                file.setContents(source, 0, null);
            } else {
                file.create(source, true, null);
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public String getType(Class<?> fieldType) {
        String colType = "String";
        if (byte[].class.equals(fieldType)) {
            colType = "Blob";
        } else if (float.class.equals(fieldType)
                || Float.class.equals(fieldType)) {
            colType = "Float";
        } else if (double.class.equals(fieldType)
                || Double.class.equals(fieldType)) {
            colType = "Double";
        } else if (int.class.equals(fieldType)
                || Integer.class.equals(fieldType)) {
            colType = "Integer";
        } else if (long.class.equals(fieldType) || Long.class.equals(fieldType)) {
            colType = "Long";
        } else if (short.class.equals(fieldType)
                || Short.class.equals(fieldType)) {
            colType = "Short";
        } else if (String.class.equals(fieldType)) {
            colType = "String";
        }

        return colType;
    }

    /**
     * project.getProject().getFile(
     * 
     * @see IActionDelegate#selectionChanged(IAction, ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
        this.selection = selection;
    }

}
