package com.drdisagree.iconify.utils.overlay;

import android.util.Log;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.common.Resources;
import com.drdisagree.iconify.config.Prefs;
import com.topjohnwu.superuser.Shell;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings({"unused"})
public class OverlayUtil {

    private static final String TAG = OverlayUtil.class.getSimpleName();

    public static List<String> getOverlayList() {
        return Shell.cmd("cmd overlay list |  grep -E '^....IconifyComponent' | sed -E 's/^....//'").exec().getOut();
    }

    public static List<String> getEnabledOverlayList() {
        return Shell.cmd("cmd overlay list |  grep -E '^.x..IconifyComponent' | sed -E 's/^.x..//'").exec().getOut();
    }

    public static List<String> getDisabledOverlayList() {
        return Shell.cmd("cmd overlay list |  grep -E '^. ..IconifyComponent' | sed -E 's/^. ..//'").exec().getOut();
    }

    public static boolean isOverlayEnabled(String pkgName) {
        try {
            return Iconify.getRootService().isOverlayEnabled(pkgName);
        } catch (Exception e) {
            Log.e(TAG, "isOverlayEnabled: ", e);
            return false;
        }
    }

    public static boolean isOverlayDisabled(String pkgName) {
        return !isOverlayEnabled(pkgName);
    }

    static boolean isOverlayInstalled(String pkgName) {
        try {
            return Iconify.getRootService().isOverlayInstalled(pkgName);
        } catch (Exception e) {
            Log.e(TAG, "isOverlayInstalled: ", e);
            return getEnabledOverlayList().contains(pkgName);
        }
    }

    public static void enableOverlay(String pkgName) {
        try {
            Prefs.putBoolean(pkgName, true);
            Iconify.getRootService().enableOverlay(Collections.singletonList(pkgName));
        } catch (Exception e) {
            Log.e(TAG, "enableOverlay: ", e);
        }
    }

    public static void enableOverlayExclusive(String pkgName) {
        try {
            Prefs.putBoolean(pkgName, true);
            Iconify.getRootService().enableOverlayExclusive(pkgName);
        } catch (Exception e) {
            Log.e(TAG, "enableOverlayExclusive: ", e);
        }
    }

    public static void enableOverlayExclusiveInCategory(String packageName) {
        try {
            Prefs.putBoolean(packageName, true);
            Iconify.getRootService().enableOverlayExclusiveInCategory(packageName);
        } catch (Exception e) {
            Log.e(TAG, "enableOverlayExclusiveInCategory: ", e);
        }
    }

    public static void disableOverlay(String pkgName) {
        try {
            Prefs.putBoolean(pkgName, false);
            Iconify.getRootService().disableOverlay(Collections.singletonList(pkgName));
        } catch (Exception e) {
            Log.e(TAG, "disableOverlay: ", e);
        }
    }

    public static void enableOverlays(String... pkgNames) {
        try {
            for (String pkgName : pkgNames) {
                Prefs.putBoolean(pkgName, true);
            }
            Iconify.getRootService().enableOverlay(Arrays.asList(pkgNames));
        } catch (Exception e) {
            Log.e(TAG, "enableOverlays: ", e);
        }
    }

    public static void disableOverlays(String... pkgNames) {
        try {
            for (String pkgName : pkgNames) {
                Prefs.putBoolean(pkgName, false);
            }
            Iconify.getRootService().disableOverlay(Arrays.asList(pkgNames));
        } catch (Exception e) {
            Log.e(TAG, "disableOverlays: ", e);
        }
    }

    public static void changeOverlayState(Object... args) {
        if (args.length % 2 != 0) {
            throw new IllegalArgumentException("Number of arguments must be even.");
        }

        for (int i = 0; i < args.length; i += 2) {
            String pkgName = (String) args[i];
            boolean state = (boolean) args[i + 1];

            Prefs.putBoolean(pkgName, state);

            try {
                if (state) {
                    Iconify.getRootService().enableOverlay(Collections.singletonList(pkgName));
                } else {
                    Iconify.getRootService().disableOverlay(Collections.singletonList(pkgName));
                }
            } catch (Exception e) {
                Log.e(TAG, "changeOverlayState: ", e);
            }
        }
    }

    public static boolean overlayExists() {
        List<String> list = Shell.cmd("[ -f /system/product/overlay/IconifyComponentAMGC.apk ] && echo \"found\" || echo \"not found\"").exec().getOut();
        return Objects.equals(list.get(0), "found");
    }

    public static boolean matchOverlayAgainstAssets() {
        try {
            String[] packages = Iconify.getAppContext().getAssets().list("Overlays");
            int numberOfOverlaysInAssets = 0;

            assert packages != null;
            for (String overlay : packages) {
                numberOfOverlaysInAssets += Objects.requireNonNull(Iconify.getAppContext().getAssets().list("Overlays/" + overlay)).length;
            }

            int numberOfOverlaysInstalled = Integer.parseInt(Shell.cmd("find /" + Resources.OVERLAY_DIR + "/ -maxdepth 1 -type f -print| wc -l").exec().getOut().get(0));
            return numberOfOverlaysInAssets <= numberOfOverlaysInstalled;
        } catch (Exception e) {
            Log.e(TAG, "matchOverlayAgainstAssets: ", e);
            return false;
        }
    }

    public static String getCategory(String pkgName) {
        String category = BuildConfig.APPLICATION_ID.replace(".debug", "") + ".category.";
        pkgName = pkgName.replace("IconifyComponent", "");

        if (pkgName.contains("MPIP")) {
            pkgName = keepFirstDigit(pkgName);
            category += "media_player_icon_pack_" + pkgName.toLowerCase();
        } else {
            pkgName = removeAllDigits(pkgName);

            switch (pkgName) {
                case "AMAC", "AMGC" -> category += "stock_monet_colors";
                case "BBN", "BBP" -> category += "brightness_bar_style";
                case "MPA", "MPB", "MPS" -> category += "media_player_style";
                case "NFN", "NFP" -> category += "notification_style";
                case "QSNT", "QSPT" -> category += "qs_tile_text_style";
                case "QSSN", "QSSP" -> category += "qs_shape_style";
                case "IPAS" -> category += "icon_pack_android_style";
                case "IPSUI" -> category += "icon_pack_sysui_style";
                default -> category += "iconify_component_" + pkgName.toLowerCase();
            }
        }

        return category;
    }

    private static String removeAllDigits(String input) {
        String regex = "\\d+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        return matcher.replaceAll("");
    }

    private static String keepFirstDigit(String input) {
        StringBuilder output = new StringBuilder();
        boolean firstDigitFound = false;

        for (char c : input.toCharArray()) {
            if (Character.isDigit(c)) {
                if (!firstDigitFound) {
                    output.append(c);
                    firstDigitFound = true;
                }
            } else {
                output.append(c);
            }
        }

        return output.toString();
    }
}
