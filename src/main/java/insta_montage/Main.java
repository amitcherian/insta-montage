package insta_montage;

import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.PlugIn;

public class Main implements PlugIn {

    static final String PLUGIN_NAME = "Insta Montage";
    static final String VERSION = "0.0.1";

    public void run(String arg) {
        // Collect currently open images (may be empty - that's OK)
        int[] imageIDs = WindowManager.getIDList();
        ImagePlus[] images = new ImagePlus[0];
        if (imageIDs != null) {
            images = new ImagePlus[imageIDs.length];
            for (int i = 0; i < imageIDs.length; i++) {
                images[i] = WindowManager.getImage(imageIDs[i]);
            }
        }

        // Launch the persistent dialog
        MontageDialog dialog = new MontageDialog(PLUGIN_NAME, VERSION, images);
        dialog.display();
    }
}