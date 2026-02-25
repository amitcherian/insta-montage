package insta_montage;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.PlugIn;

public class Main implements PlugIn {

    static final String PLUGIN_NAME = "Insta Montage";
    static final String VERSION = "0.0.1";

    public void run(String arg) {
        // Check that there are at least 2 images open
        int[] imageIDs = WindowManager.getIDList();
        if (imageIDs == null || imageIDs.length < 2) {
            IJ.error(PLUGIN_NAME, "Please open at least 2 images before running Insta Montage.");
            return;
        }

        // Collect open images
        ImagePlus[] images = new ImagePlus[imageIDs.length];
        for (int i = 0; i < imageIDs.length; i++) {
            images[i] = WindowManager.getImage(imageIDs[i]);
        }

        // Launch the settings dialog
        MontageDialog dialog = new MontageDialog(PLUGIN_NAME, VERSION, images);
        dialog.show();

        // If user clicked OK, run the montage
        if (!dialog.wasCanceled()) {
            MontageSettings settings = dialog.getSettings();
            MontageProcessor processor = new MontageProcessor(images, settings);
            processor.run();
        }
    }
}