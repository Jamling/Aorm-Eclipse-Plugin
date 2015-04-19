package cn.ieclipse.aorm.eclipse.test;

import java.io.FileReader;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
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

        TipShell.setShow(true);
        Label lbl = new Label(this, SWT.NONE);
        lbl.setText("New Label");
        TipShell.enableFor(lbl, "label");

        txtText = new Text(this, SWT.BORDER);
        txtText.setText("text");
        txtText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
                1, 1));

        String str = "";
        try {
            FileReader r = new FileReader("1.html");
            char[] buf = new char[10240];
            int len = r.read(buf);
            str = new String(buf, 0, len);
            TipShell.enableFor(txtText, str);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Combo combo = new Combo(this, SWT.NONE);
        combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1,
                1));
        TipShell.enableFor(combo, "combo");
        combo.setText("combo");
        
        Browser browser = new Browser(this, SWT.NONE);
        browser.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        browser.setText(str);

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
