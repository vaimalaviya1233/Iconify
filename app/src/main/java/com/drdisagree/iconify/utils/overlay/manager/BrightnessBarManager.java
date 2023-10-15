package com.drdisagree.iconify.utils.overlay.manager;

import com.drdisagree.iconify.utils.overlay.OverlayUtil;

public class BrightnessBarManager {

    public static void enableOverlay(int n) {
        OverlayUtil.enableOverlayExclusiveInCategory("IconifyComponentBBN" + n + ".overlay");

        if (!OverlayUtil.isOverlayEnabled("IconifyComponentCR1.overlay") || !OverlayUtil.isOverlayEnabled("IconifyComponentCR2.overlay")) {
            OverlayUtil.enableOverlays("IconifyComponentCR1.overlay", "IconifyComponentCR2.overlay");
        }
    }

    public static void disableOverlay(int n) {
        OverlayUtil.disableOverlay("IconifyComponentBBN" + n + ".overlay");
    }
}