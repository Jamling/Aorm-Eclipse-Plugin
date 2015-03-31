/**
 * 
 */
package cn.ieclipse.aorm.eclipse.test;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import cn.ieclipse.aorm.eclipse.wizards.EditComponentWizard;

/**
 * @author Jamling
 * 
 */
public class EditActivity {

    /**
     * @param args
     */
    public static void main(String[] args) {
        WizardDialog dialog = new WizardDialog(
                new Shell(),
                new EditComponentWizard("activity", "com.ex.udisk.MainActivity"));

        dialog.open();
        Display.getCurrent().dispose();
    }

}
