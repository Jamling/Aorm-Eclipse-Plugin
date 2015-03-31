package cn.ieclipse.aorm.eclipse.helpers;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import com.android.SdkConstants;

public class LetterImageDescriptor extends ImageDescriptor {
    private final char mLetter;

    private final int mColor;
    private final int mShape;

    public LetterImageDescriptor(char letter, int color, int shape) {
        this.mLetter = Character.toUpperCase(letter);
        this.mColor = color;
        this.mShape = shape;
    }

    public ImageData getImageData() {
        Display display = Display.getCurrent();
        if (display == null) {
            return null;
        }

        Image image = new Image(display, 15, 15);

        GC gc = new GC(image);
        gc.setAdvanced(true);
        gc.setAntialias(1);
        gc.setTextAntialias(1);

        RGB backgroundRgb = new RGB(254, 254, 254);
        Color backgroundColor = new Color(display, backgroundRgb);
        gc.setBackground(backgroundColor);
        gc.fillRectangle(0, 0, 15, 15);

        gc.setBackground(display.getSystemColor(1));
        if (this.mShape == 67) {
            gc.fillOval(0, 0, 14, 14);
        } else if (this.mShape == 82) {
            gc.fillRoundRectangle(0, 0, 14, 14, 4, 4);
        }

        gc.setForeground(display.getSystemColor(2));
        gc.setLineWidth(1);
        if (this.mShape == 67) {
            gc.drawOval(0, 0, 14, 14);
        } else if (this.mShape == 82) {
            gc.drawRoundRectangle(0, 0, 14, 14, 4, 4);
        }

        Font font = display.getSystemFont();
        FontData[] fds = font.getFontData();
        fds[0].setStyle(1);

        fds[0].setHeight((int) (864.0D / display.getDPI().y));

        font = new Font(display, fds);
        gc.setFont(font);
        gc.setForeground(display.getSystemColor(this.mColor));

        int ofx = 0;
        int ofy = 0;
        if (SdkConstants.CURRENT_PLATFORM == 2) {
            ofx = 1;
            ofy = -1;
        } else if (SdkConstants.CURRENT_PLATFORM == 3) {
            if ((this.mLetter != 'T') && (this.mLetter != 'V')) {
                ofy = -1;
            }
            if (this.mLetter == 'I') {
                ofx = -2;
            }
        }

        String s = Character.toString(this.mLetter);
        Point p = gc.textExtent(s);
        int tx = (15 + ofx - p.x) / 2;
        int ty = (15 + ofy - p.y) / 2;
        gc.drawText(s, tx, ty, true);

        font.dispose();
        gc.dispose();

        ImageData data = image.getImageData();
        image.dispose();
        backgroundColor.dispose();

        int backgroundPixel = data.palette.getPixel(backgroundRgb);
        data.transparentPixel = backgroundPixel;

        return data;
    }
}
