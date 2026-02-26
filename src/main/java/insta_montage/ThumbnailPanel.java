package insta_montage;

import ij.ImagePlus;
import ij.process.ImageProcessor;

import javax.swing.*;

import org.w3c.dom.events.MouseEvent;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ThumbnailPanel extends JPanel {

    private static final int THUMB_W = 80;
    private static final int THUMB_H = 80;
    private static final int PADDING = 8;
    private static final Color HIGHLIGHT_COLOR = new Color(70, 130, 180);

    private List<ImagePlus> images;
    private List<BufferedImage> thumbnails;
    private int dragSourceIndex = -1;
    private int dropTargetIndex = -1;
    private Runnable onOrderChanged;

    public ThumbnailPanel() {
        this.images = new ArrayList<>();
        this.thumbnails = new ArrayList<>();
        setBackground(new Color(45, 45, 45));
        setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Image Order (drag to reorder)",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            null, Color.WHITE));
        setupDragAndDrop();
    }

    public void setOnOrderChanged(Runnable callback) {
        this.onOrderChanged = callback;
    }

    public void setImages(List<ImagePlus> images) {
        this.images = new ArrayList<>(images);
        this.thumbnails = new ArrayList<>();
        for (ImagePlus imp : images) {
            thumbnails.add(toBufferedImage(imp));
        }
        repaint();
    }

    public List<ImagePlus> getOrderedImages() {
        return new ArrayList<>(images);
    }

    private BufferedImage toBufferedImage(ImagePlus imp) {
        ImageProcessor ip = imp.getProcessor().convertToRGB();
        ip = ip.resize(THUMB_W, THUMB_H, true);
        BufferedImage bi = new BufferedImage(THUMB_W, THUMB_H, BufferedImage.TYPE_INT_RGB);
        bi.getGraphics().drawImage(ip.createImage(), 0, 0, null);
        return bi;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int x = PADDING;
        int y = 30; // leave room for titled border

        for (int i = 0; i < thumbnails.size(); i++) {
            // Highlight drop target
            if (i == dropTargetIndex) {
                g2.setColor(HIGHLIGHT_COLOR);
                g2.fillRoundRect(x - 3, y - 3, THUMB_W + 6, THUMB_H + 6, 6, 6);
            }

            // Draw thumbnail
            if (dragSourceIndex == i) {
                // Make dragged thumbnail semi-transparent
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
            }
            g2.drawImage(thumbnails.get(i), x, y, null);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

            // Draw index number
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2.drawString(String.valueOf(i + 1), x + 4, y + THUMB_H - 4);

            // Draw title below thumbnail
            g2.setColor(new Color(200, 200, 200));
            g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
            String title = images.get(i).getTitle();
            if (title.contains(".")) title = title.substring(0, title.lastIndexOf('.'));
            if (title.length() > 10) title = title.substring(0, 10) + "...";
            g2.drawString(title, x, y + THUMB_H + 12);

            x += THUMB_W + PADDING;
        }
    }

    @Override
    public Dimension getPreferredSize() {
        int width = Math.max(300, images.size() * (THUMB_W + PADDING) + PADDING);
        return new Dimension(width, THUMB_H + 55);
    }

    private int getIndexAt(int mouseX) {
        int x = PADDING;
        for (int i = 0; i < images.size(); i++) {
            if (mouseX >= x && mouseX <= x + THUMB_W) return i;
            x += THUMB_W + PADDING;
        }
        return -1;
    }

    private void setupDragAndDrop() {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                dragSourceIndex = getIndexAt(e.getX());
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                if (dragSourceIndex >= 0 && dropTargetIndex >= 0
                        && dragSourceIndex != dropTargetIndex) {
                    ImagePlus tempImg = images.remove(dragSourceIndex);
                    images.add(dropTargetIndex, tempImg);
                    BufferedImage tempThumb = thumbnails.remove(dragSourceIndex);
                    thumbnails.add(dropTargetIndex, tempThumb);
                    if (onOrderChanged != null) onOrderChanged.run();
                }
                dragSourceIndex = -1;
                dropTargetIndex = -1;
                repaint();
            }

            @Override
            public void mouseDragged(java.awt.event.MouseEvent e) {
                int idx = getIndexAt(e.getX());
                if (idx >= 0) dropTargetIndex = idx;
                repaint();
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
    }
}