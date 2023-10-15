package com.drdisagree.iconify.utils.overlay.manager;

import com.drdisagree.iconify.utils.overlay.OverlayUtil;

public class MediaPlayerIconManager {

    public static void enableOverlay(int m, int n) {
        OverlayUtil.enableOverlayExclusiveInCategory("IconifyComponentMPIP" + m + n + ".overlay");
    }

    public static void disableOverlay(int m, int n) {
        OverlayUtil.disableOverlay("IconifyComponentMPIP" + m + n + ".overlay");
    }
}