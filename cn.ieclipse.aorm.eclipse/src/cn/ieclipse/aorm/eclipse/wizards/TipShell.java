package cn.ieclipse.aorm.eclipse.wizards;

import java.io.File;
import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import cn.ieclipse.aorm.eclipse.AormPlugin;
import cn.ieclipse.aorm.eclipse.helpers.ComponentAttribute;

import com.android.ide.eclipse.adt.internal.sdk.Sdk;

public class TipShell extends Shell {

    private Browser text;
    private static TipShell tip;
    private static boolean show = false;

    private String nodeName = "activity";
    private ComponentAttribute attr;

    /**
     * Set node name.
     * 
     * @param nodeName
     */
    public static void setNodeName(String nodeName) {
        if (tip != null) {
            tip.nodeName = nodeName;
        }
    }

    /**
     * Set whether show tooltip
     * 
     * @param show
     */
    public static void setShow(boolean show) {
        TipShell.show = show;
    }

    /**
     * Create the shell.
     * 
     * @param display
     * @wbp.parser.constructor
     */
    public TipShell(Shell shell) {
        super(shell, SWT.ON_TOP | SWT.TOOL);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        setLayout(gridLayout);

        // text = new Text(this, SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL
        // | SWT.MULTI);
        text = new Browser(this, SWT.NONE);
        text.setBackground(SWTResourceManager
                .getColor(SWT.COLOR_INFO_BACKGROUND));
        text.setForeground(SWTResourceManager
                .getColor(SWT.COLOR_INFO_FOREGROUND));
        GridData gd_text = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
        gd_text.widthHint = 300;
        gd_text.heightHint = 180;
        text.setLayoutData(gd_text);

        ToolBar toolBar = new ToolBar(this, SWT.FLAT | SWT.RIGHT);
        GridData gd_toolBar = new GridData(SWT.LEFT, SWT.CENTER, false, false,
                1, 1);
        gd_toolBar.horizontalIndent = 5;
        toolBar.setLayoutData(gd_toolBar);

        ToolItem tbLocal = new ToolItem(toolBar, SWT.NONE);
        tbLocal.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                File f = Sdk.getCurrent().getSdkFileLocation();
                f = new File(f, "docs/guide/topics/manifest/");
                String url = "file://" + f.getAbsolutePath();
                if (!url.endsWith("/")) {
                    url = url + "/";
                }
                url = url + nodeName + "-element.html";
                openBrowser(url, true);
            }
        });
        tbLocal.setImage(SWTResourceManager.getImage(
                org.eclipse.jdt.ui.ISharedImages.class,
                "/icons/full/elcl16/open_browser.gif"));
        tbLocal.setToolTipText("Open in Local SDK docs");

        ToolItem tbSite = new ToolItem(toolBar, SWT.NONE);
        tbSite.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String url = "http://developer.android.com/guide/topics/manifest/activity-element.html";
                openBrowser(url.replace("activity", nodeName), true);
            }
        });
        tbSite.setImage(SWTResourceManager.getImage(
                org.eclipse.jdt.ui.ISharedImages.class,
                "/icons/full/elcl16/open_browser.gif"));
        tbSite.setToolTipText("Open in Android Developer Site");

        ToolItem tbClose = new ToolItem(toolBar, SWT.NONE);
        tbClose.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (!isDisposed()) {
                    dispose();
                }
            }
        });
        tbClose.setImage(ResourceManager.getPluginImage(AormPlugin.PLUGIN_ID,
                "res/delete.png"));
        tbClose.setToolTipText("Close");

        setSize(300, 180);

        Label label = new Label(this, SWT.NONE);
        label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false,
                1, 1));
        label.setText("Press 'F2' to focus.");

    }

    private void openBrowser(String url, boolean internal) {
        IWorkbenchBrowserSupport support = PlatformUI.getWorkbench()
                .getBrowserSupport();
        IWebBrowser browser;
        try {
            browser = support.getExternalBrowser();
            browser.openURL(new URL(url + "#" + attr.getAchor()));
        } catch (PartInitException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

    public static void enableFor(final Control widget, final String text) {
        ComponentAttribute attr = new ComponentAttribute();
        attr.setTip(text);
        enableFor(widget, attr);
    }

    public static void enableFor(final Control widget,
            final ComponentAttribute attr) {
        Listener listener = new Listener() {
            private boolean focus = false;

            public void handleEvent(Event event) {
                if (event.type == SWT.KeyDown) {
                    if (event.keyCode == SWT.F2) {
                        if (tip != null && !tip.isDisposed()) {
                            focus = tip.forceFocus();
                            focus = tip.text.setFocus();
                            Rectangle r = tip.getBounds();
                            int offset = 450;
                            tip.setBounds(r.x - offset, r.y, r.width + offset,
                                    r.height);
                        }
                    }
                } else if (event.type == SWT.MouseHover) {
                    if (hasTip(attr)) {
                        return;
                    }
                    if ((tip == null || tip.isDisposed())) {
                        tip = new TipShell(widget.getShell());
                    }
                    widget.setFocus();
                    tip.text.setText(attr.getTip());
                    Point p = widget.getShell().toDisplay(widget.getBounds().x,
                            widget.getBounds().y);
                    p = getLoc(widget, new Point(event.x, event.y));
                    int x = p.x;
                    int y = p.y + 5;
                    tip.setLocation(x, y);
                    tip.setVisible(true);
                    tip.pack();
                } else if (event.type == SWT.MouseExit) {
                    if (!focus && tip != null && !tip.isDisposed()) {
                        tip.setVisible(false);
                    }
                }
            }

            private boolean hasTip(ComponentAttribute attr) {
                boolean flag = !TipShell.show || attr == null
                        || attr.getTip() == null
                        || attr.getTip().trim().length() == 0;
                return !flag;
            }

            private Point getLoc(Control widget, Point pt) {
                return widget.getDisplay().getCursorLocation();
            }
        };
        widget.addListener(SWT.KeyDown, listener);
        widget.addListener(SWT.MouseHover, listener);
        widget.addListener(SWT.MouseExit, listener);

    }
}
