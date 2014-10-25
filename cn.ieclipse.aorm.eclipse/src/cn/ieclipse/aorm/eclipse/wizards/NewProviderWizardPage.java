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
package cn.ieclipse.aorm.eclipse.wizards;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import cn.ieclipse.aorm.eclipse.helpers.AdtConstants;
import cn.ieclipse.aorm.eclipse.helpers.Status;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (a).
 * 
 * @author Jamling
 */

public class NewProviderWizardPage extends NewComponentWizardPage {

    private IStatus authorityStatus;
    private Text authorityText;
    private String authority;

    /**
     * Constructor for SampleNewWizardPage.
     * 
     * @param pageName
     */
    public NewProviderWizardPage() {
        super();
        setTitle("Android ContentProvider");
        setDescription("Create a new Android ContentProvider.");

    }

    @Override
    protected void createOtherControls(Composite composite, int nColumns) {
        createAuthorize(composite, nColumns);
        setSuperClass(AdtConstants.PROVIDER_QNAME, true);
        setAuthority(getPackageText());
        compCombo.select(3);
        compCombo.setEnabled(false);
    }

    private void createAuthorize(Composite composite, int nColumns) {
        Label label = new Label(composite, SWT.NONE);
        label.setText("Authorities:");
        authorityText = new Text(composite, SWT.BORDER);
        authorityText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true,
                false, nColumns - 2, 1));
        authorityText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                handleFieldChanged("authority");
            }
        });
        new Label(composite, SWT.NONE);
    }

    @Override
    protected IStatus[] getUpdateStatus() {
        IStatus[] status = super.getUpdateStatus();
        IStatus[] dest = new IStatus[status.length + 1];
        System.arraycopy(status, 0, dest, 0, status.length);
        dest[status.length] = authorityStatus;
        return dest;
    }

    @Override
    protected void handleFieldChanged(String fieldName) {
        super.handleFieldChanged(fieldName);
        authorityStatus = authorityChanged();
        doStatusUpdate();
    }

    private IStatus authorityChanged() {
        Status status = new Status();
        if (authorityText != null) {
            authority = authorityText.getText().trim();
            if (authority.length() <= 0) {
                status.setError("authorities can't be empty!");
            } else {
                status.setOK();
            }
        }
        return status;
    }

    @Override
    protected void createTypeMembers(IType newType, ImportsManager imports,
            IProgressMonitor monitor) throws CoreException {
        super.createTypeMembers(newType, imports, monitor);

        try {
            // imports.addImport("android.database.sqlite.SQLiteDatabase");
            imports.addImport("android.database.sqlite.SQLiteOpenHelper");
            // imports.addImport("android.content.UriMatcher");
            // imports.addImport("android.content.ContentValues");
            newType.createField("public static final String AUTH=\""
                    + authority + "\";", null, true, null);
            newType.createField(
                    "public static final Uri URI=Uri.parse(\"content://\" + AUTH);",
                    null, true, null);

            newType.createField("private SQLiteOpenHelper mOpenHelper;", null,
                    true, null);
        } catch (Exception e) {
            e.printStackTrace();
            Status status = new Status();
            status.setError("error in generate code : " + e.getMessage());
            throw new CoreException(status);
        }
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        if (authorityText != null) {
            authorityText.setText(authority);
            this.authority = authority;
        }
    }
}