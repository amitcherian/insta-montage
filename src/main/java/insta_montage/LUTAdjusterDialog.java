package insta_montage;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.process.LUT;
import ij.plugin.frame.ContrastAdjuster;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import ij.CompositeImage;
import ij.measure.Measurements;
import ij.process.ImageStatistics;

public class LUTAdjusterDialog extends JFrame {

    private JPanel channelsPanel;
    private JButton refreshButton;
    private JButton autoScaleButton;
    private List<ChannelAdjuster> adjusters = new ArrayList<>();

    public LUTAdjusterDialog() {
        super("LUT Adjuster");
        buildUI();
        refresh();
    }

    private void buildUI() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(true);
        setLayout(new BorderLayout(8, 8));

        // --- Top label ---
        JLabel infoLabel = new JLabel("Active image channels:", SwingConstants.LEFT);
        infoLabel.setBorder(BorderFactory.createEmptyBorder(8, 10, 0, 10));
        add(infoLabel, BorderLayout.NORTH);

        // --- Channels panel (dynamically populated) ---
        channelsPanel = new JPanel();
        channelsPanel.setLayout(new BoxLayout(channelsPanel, BoxLayout.Y_AXIS));
        channelsPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JScrollPane scrollPane = new JScrollPane(channelsPanel,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new Dimension(380, 300));
        add(scrollPane, BorderLayout.CENTER);

        // --- Buttons ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        refreshButton = new JButton("Refresh");
        autoScaleButton = new JButton("Auto Scale All");
        autoScaleButton.setBackground(new Color(70, 130, 180));
        autoScaleButton.setForeground(Color.WHITE);
        autoScaleButton.setFont(autoScaleButton.getFont().deriveFont(Font.BOLD));
        buttonPanel.add(refreshButton);
        buttonPanel.add(autoScaleButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // --- Button actions ---
        refreshButton.addActionListener(e -> refresh());

        autoScaleButton.addActionListener(e -> {
            ImagePlus imp = WindowManager.getCurrentImage();
            if (imp == null) {
                IJ.error("LUT Adjuster", "No active image.");
                return;
            }
            int channels = imp.getNChannels();
            if (imp.isComposite()) {
                CompositeImage ci = (CompositeImage) imp;
                for (int c = 1; c <= channels; c++) {
                    int stackIndex = imp.getStackIndex(c, imp.getZ(), imp.getT());
                    ij.process.ImageProcessor ip = imp.getStack().getProcessor(stackIndex);
                    ImageStatistics stats = ImageStatistics.getStatistics(
                        ip, Measurements.MIN_MAX, null);
                    LUT lut = ci.getChannelLut(c);
                    lut.min = stats.min;
                    lut.max = stats.max;
                    ci.setChannelLut(lut, c);
                }
            } else {
                ij.process.ImageStatistics stats = ImageStatistics.getStatistics(
                    imp.getProcessor(), Measurements.MIN_MAX, null);
                imp.getProcessor().setMinAndMax(stats.min, stats.max);
            }
            imp.updateAndDraw();
            refresh();
        });

        pack();
        setLocationRelativeTo(null);
    }

    public void refresh() {
        channelsPanel.removeAll();
        adjusters.clear();

        ImagePlus imp = WindowManager.getCurrentImage();

        if (imp == null) {
            JLabel noImage = new JLabel("No image open.", SwingConstants.CENTER);
            noImage.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
            channelsPanel.add(noImage);
        } else {
            int channels = imp.getNChannels();
            int bitDepth = imp.getBitDepth();
            double maxPossible = Math.pow(2, bitDepth) - 1;
            if (bitDepth == 32) maxPossible = 1.0; // float images

            for (int c = 1; c <= channels; c++) {
                // Get current min/max for this channel
                imp.setC(c);
                double currentMin = imp.getProcessor().getMin();
                double currentMax = imp.getProcessor().getMax();

                // Get channel color from LUT
                Color channelColor = getChannelColor(imp, c);

                ChannelAdjuster adjuster = new ChannelAdjuster(
                    imp, c, channelColor, currentMin, currentMax, maxPossible);
                adjusters.add(adjuster);
                channelsPanel.add(adjuster.getPanel());
                channelsPanel.add(Box.createVerticalStrut(6));
            }
        }

        channelsPanel.revalidate();
        channelsPanel.repaint();
        pack();
    }

    private Color getChannelColor(ImagePlus imp, int channel) {
        try {
            LUT lut = imp.getLuts()[channel - 1];
            // Sample the color from the end of the LUT (brightest value)
            int r = lut.getRed(255);
            int g = lut.getGreen(255);
            int b = lut.getBlue(255);
            return new Color(r, g, b);
        } catch (Exception e) {
            // Fallback colors for common channels
            switch (channel) {
                case 1: return Color.RED;
                case 2: return Color.GREEN;
                case 3: return Color.BLUE;
                case 4: return Color.CYAN;
                case 5: return Color.MAGENTA;
                case 6: return Color.YELLOW;
                default: return Color.WHITE;
            }
        }
    }

    public void display() {
        setVisible(true);
        toFront();
    }

    // --- Inner class representing one channel's min/max adjuster ---
    private static class ChannelAdjuster {

        private final ImagePlus imp;
        private final int channel;
        private final JSlider minSlider;
        private final JSlider maxSlider;
        private final JLabel minValueLabel;
        private final JLabel maxValueLabel;
        private final JPanel panel;
        private boolean updating = false;

        public ChannelAdjuster(ImagePlus imp, int channel, Color color,
                               double currentMin, double currentMax, double maxPossible) {
            this.imp = imp;
            this.channel = channel;

            int maxVal = (int) maxPossible;
            int minVal = 0;

            panel = new JPanel();
            panel.setLayout(new GridBagLayout());
            panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(color, 2),
                "Channel " + channel,
                TitledBorder.LEFT, TitledBorder.TOP));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(2, 4, 2, 4);

            // --- Min row ---
            gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
            panel.add(new JLabel("Min:"), gbc);

            gbc.gridx = 1; gbc.weightx = 1.0;
            minSlider = new JSlider(minVal, maxVal, (int) currentMin);
            minSlider.setPreferredSize(new Dimension(200, 20));
            panel.add(minSlider, gbc);

            gbc.gridx = 2; gbc.weightx = 0;
            minValueLabel = new JLabel(String.valueOf((int) currentMin));
            minValueLabel.setPreferredSize(new Dimension(50, 20));
            panel.add(minValueLabel, gbc);

            // --- Max row ---
            gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
            panel.add(new JLabel("Max:"), gbc);

            gbc.gridx = 1; gbc.weightx = 1.0;
            maxSlider = new JSlider(minVal, maxVal, (int) currentMax);
            maxSlider.setPreferredSize(new Dimension(200, 20));
            panel.add(maxSlider, gbc);

            gbc.gridx = 2; gbc.weightx = 0;
            maxValueLabel = new JLabel(String.valueOf((int) currentMax));
            maxValueLabel.setPreferredSize(new Dimension(50, 20));
            panel.add(maxValueLabel, gbc);

            // --- Live update listeners ---
            minSlider.addChangeListener(e -> {
                if (updating) return;
                updating = true;
                // Clamp min so it never exceeds max
                if (minSlider.getValue() > maxSlider.getValue()) {
                    maxSlider.setValue(minSlider.getValue());
                }
                minValueLabel.setText(String.valueOf(minSlider.getValue()));
                maxValueLabel.setText(String.valueOf(maxSlider.getValue()));
                applyLUT();
                updating = false;
            });

            maxSlider.addChangeListener(e -> {
                if (updating) return;
                updating = true;
                // Clamp max so it never goes below min
                if (maxSlider.getValue() < minSlider.getValue()) {
                    minSlider.setValue(maxSlider.getValue());
                }
                minValueLabel.setText(String.valueOf(minSlider.getValue()));
                maxValueLabel.setText(String.valueOf(maxSlider.getValue()));
                applyLUT();
                updating = false;
            });
        }

        private void applyLUT() {
            if (imp.isComposite()) {
                CompositeImage ci = (CompositeImage) imp;
                LUT lut = ci.getChannelLut(channel);
                lut.min = minSlider.getValue();
                lut.max = maxSlider.getValue();
                ci.setChannelLut(lut, channel);
            } else {
                imp.getProcessor().setMinAndMax(
                    minSlider.getValue(), maxSlider.getValue());
            }
            imp.updateAndDraw();
        }

        public JPanel getPanel() {
            return panel;
        }
    }
}