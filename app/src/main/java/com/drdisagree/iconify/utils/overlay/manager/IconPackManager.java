package com.drdisagree.iconify.utils.overlay.manager;

import com.drdisagree.iconify.utils.overlay.OverlayUtil;

public class IconPackManager {

    public static void enableOverlay(int n) {
        OverlayUtil.enableOverlayExclusiveInCategory("IconifyComponentIPAS" + n + ".overlay");
        OverlayUtil.enableOverlayExclusiveInCategory("IconifyComponentIPSUI" + n + ".overlay");
    }

    public static void disableOverlay(int n) {
        OverlayUtil.disableOverlays("IconifyComponentIPAS" + n + ".overlay", "IconifyComponentIPSUI" + n + ".overlay");
    }
}