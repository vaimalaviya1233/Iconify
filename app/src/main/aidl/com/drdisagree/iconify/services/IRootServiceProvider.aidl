package com.drdisagree.iconify.services;

import android.content.om.OverlayIdentifier;
import com.drdisagree.iconify.utils.extension.MethodInterface;

interface IRootServiceProvider {
    boolean isOverlayInstalled(String packageName);
    boolean isOverlayEnabled(String packageName);
    void enableOverlay(in List<String> packages);
    void enableOverlayWithIdentifier(in List<String> packages);
    boolean enableOverlayExclusive(in String packageName);
    boolean enableOverlayExclusiveInCategory(in String packageName);
    void disableOverlay(in List<String> packages);
    void disableOverlayWithIdentifier(in List<String> packages);
    void registerFabricatedOverlay(in String overlayName, in String targetPackage, in List<String> resources);
    void unregisterFabricatedOverlay(in String packageName);
    boolean setHighestPriority(String packageName);
    boolean setLowestPriority(String packageName);
    OverlayIdentifier generateOverlayIdentifier(String overlayName);
    void uninstallOverlayUpdates(String packageName);
    void restartSystemUI();
    String[] runCommand(in List<String> command);
    void runWithRoot(in MethodInterface method);
}