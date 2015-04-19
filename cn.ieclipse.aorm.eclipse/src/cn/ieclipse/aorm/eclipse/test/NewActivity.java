/**
 * 
 */
package cn.ieclipse.aorm.eclipse.test;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import cn.ieclipse.aorm.eclipse.wizards.NewActivityWizard;

/**
 * @author Jamling
 * 
 */
public class NewActivity {

    /**
     * @param args
     */
    public static void main(String[] args) {
        WizardDialog dialog = new WizardDialog(
                new Shell(),
                new NewActivityWizard(""));

        dialog.open();
        Display.getCurrent().dispose();
    }

}
