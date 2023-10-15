package com.drdisagree.iconify.services;

interface IRootServiceProvider {
    boolean isOverlayInstalled(String packageName);
    boolean isOverlayEnabled(String packageName);
    void enableOverlay(in List<String> packages);
    boolean enableOverlayExclusive(in String packageName);
    boolean enableOverlayExclusiveInCategory(in String packageName);
    void disableOverlay(in List<String> packages);
    boolean setHighestPriority(String packageName);
    boolean setLowestPriority(String packageName);
    void uninstallOverlayUpdates(String packageName);
    void restartSystemUI();
    String[] runCommand(in List<String> command);
}