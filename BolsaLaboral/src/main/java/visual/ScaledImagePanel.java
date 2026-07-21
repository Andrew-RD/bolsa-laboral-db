package visual;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

/** Panel de fondo que escala una imagen sin deformarla y cubre el área disponible. */
public final class ScaledImagePanel extends JPanel {

    private final Image image;

    public ScaledImagePanel(String resourceName) {
        image = UIUtils.image(resourceName);
        setBackground(UIUtils.DARK_BACKGROUND);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (getWidth() <= 0 || getHeight() <= 0) {
            return;
        }

        double imageRatio = (double) image.getWidth(this) / image.getHeight(this);
        double panelRatio = (double) getWidth() / getHeight();
        int targetWidth;
        int targetHeight;
        if (panelRatio > imageRatio) {
            targetWidth = getWidth();
            targetHeight = (int) Math.ceil(targetWidth / imageRatio);
        } else {
            targetHeight = getHeight();
            targetWidth = (int) Math.ceil(targetHeight * imageRatio);
        }
        int x = (getWidth() - targetWidth) / 2;
        int y = (getHeight() - targetHeight) / 2;

        Graphics2D graphics2D = (Graphics2D) graphics.create();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics2D.drawImage(image, x, y, targetWidth, targetHeight, this);
        graphics2D.dispose();
    }
}
