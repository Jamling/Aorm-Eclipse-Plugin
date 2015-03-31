package cn.ieclipse.aorm.eclipse.test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import cn.ieclipse.aorm.eclipse.wizards.TipShell;

public class T extends Shell {
    private Text txtText;

    /**
     * Launch the application.
     * 
     * @param args
     */
    public static void main(String args[]) {
        try {
            Display display = Display.getDefault();
            T shell = new T(display);
            shell.open();
            shell.layout();
            while (!shell.isDisposed()) {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create the shell.
     * 
     * @param display
     */
    public T(Display display) {
        super(display, SWT.SHELL_TRIM);
        setLayout(new GridLayout(1, false));

        Label lbl = new Label(this, SWT.NONE);
        lbl.setText("New Label");
        TipShell.enableFor(lbl, "label");

        txtText = new Text(this, SWT.BORDER);
        txtText.setText("text");
        txtText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
                1, 1));
        TipShell.enableFor(txtText, "text");

        Combo combo = new Combo(this, SWT.NONE);
        combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1,
                1));
        TipShell.enableFor(combo, "combo");
        combo.setText("combo");

        createContents();
    }

    /**
     * Create contents of the shell.
     */
    protected void createContents() {
        setText("SWT Application");
        setSize(450, 300);

    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

}
