package com.drdisagree.iconify.utils.overlay;

import android.util.Log;
import android.util.TypedValue;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.utils.helper.TypedValueUtil;
import com.drdisagree.iconify.utils.overlay.fabricated.FabricatedOverlay;
import com.topjohnwu.superuser.Shell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
public class FabricatedUtil {

    private static final String TAG = FabricatedUtil.class.getSimpleName();

    public static List<String> getOverlayList() {
        return Shell.cmd("cmd overlay list |  grep -E '^....com.android.shell:IconifyComponent' | sed -E 's/^....com.android.shell:IconifyComponent//'").exec().getOut();
    }

    public static List<String> getEnabledOverlayList() {
        return Shell.cmd("cmd overlay list |  grep -E '^.x..com.android.shell:IconifyComponent' | sed -E 's/^.x..com.android.shell:IconifyComponent//'").exec().getOut();
    }

    public static List<String> getDisabledOverlayList() {
        return Shell.cmd("cmd overlay list |  grep -E '^. ..com.android.shell:IconifyComponent' | sed -E 's/^. ..com.android.shell:IconifyComponent//'").exec().getOut();
    }

    public static void buildAndEnableOverlay(String overlayName, String targetPackage, String... resources) {
        if (!overlayName.startsWith("IconifyComponent")) {
            overlayName = "IconifyComponent" + overlayName;
        }

        try {
            Iconify.getRootService().registerFabricatedOverlay(overlayName, targetPackage, Arrays.asList(resources));
            Iconify.getRootService().enableOverlayWithIdentifier(Collections.singletonList(overlayName));
        } catch (Exception e) {
            Log.e(TAG, "buildAndEnableOverlay: ", e);
        }
    }

    public static void buildAndEnableOverlays(String... args) {
        if (args.length == 0 || args.length % 5 != 0) {
            throw new IllegalArgumentException("Mismatch in number of arguments.");
        }

        for (int i = 0; i < args.length; i += 5) {
            String overlayName = args[i];
            String targetPackage = args[i + 1];
            List<String> resources = Arrays.asList(args[i + 2], args[i + 3], args[i + 4]);

            if (!overlayName.startsWith("IconifyComponent")) {
                overlayName = "IconifyComponent" + overlayName;
            }

            try {
                Iconify.getRootService().registerFabricatedOverlay(overlayName, targetPackage, resources);
                Iconify.getRootService().enableOverlayWithIdentifier(Collections.singletonList(overlayName));
            } catch (Exception e) {
                Log.e(TAG, "buildAndEnableOverlays: ", e);
            }
        }
    }

    public static void disableOverlay(String overlayName) {
        if (!overlayName.startsWith("IconifyComponent")) {
            overlayName = "IconifyComponent" + overlayName;
        }

        try {
            Iconify.getRootService().unregisterFabricatedOverlay("IconifyComponent" + overlayName);
        } catch (Exception e) {
            Log.e(TAG, "disableOverlay: ", e);
        }
    }

    public static void disableOverlays(String... overlayNames) {
        for (String overlayName : overlayNames) {
            if (!overlayName.startsWith("IconifyComponent")) {
                overlayName = "IconifyComponent" + overlayName;
            }

            try {
                Iconify.getRootService().unregisterFabricatedOverlay("IconifyComponent" + overlayName);
            } catch (Exception e) {
                Log.e(TAG, "disableOverlays: ", e);
            }
        }
    }

    public static boolean isOverlayEnabled(String name) {
        return Shell.cmd("[[ $(cmd overlay list | grep -o '\\[x\\] com.android.shell:IconifyComponent" + name + "') ]] && echo 1 || echo 0").exec().getOut().get(0).equals("1");
    }

    public static boolean isOverlayDisabled(String name) {
        return !isOverlayEnabled(name);
    }

    public static List<String> buildCommands(String name, String target, String type, String resourceName, String value) {
        if (target.equals("systemui") || target.equals("sysui")) {
            target = "com.android.systemui";
        }

        String resourceType = getResourceType(type);

        if (type.equals("dimen")) {
            value = String.valueOf(TypedValueUtil.createComplexDimension(getResourceValue(value), getResourceValueType(value)));
        }

        List<String> commands = new ArrayList<>();
        commands.add("cmd overlay fabricate --target " + target + " --name IconifyComponent" + name + " " + target + ":" + type + "/" + resourceName + " " + resourceType + " " + value);
        commands.add("cmd overlay enable --user current com.android.shell:IconifyComponent" + name);

        return commands;
    }

    private static String getResourceType(String type) {
        switch (type) {
            case "color" -> {
                return "0x1c";
            }
            case "dimen" -> {
                return "0x05";
            }
            case "bool" -> {
                return "0x12";
            }
            case "integer" -> {
                return "0x10";
            }
            case "attr" -> {
                return "0x02";
            }
        }
        throw new IllegalArgumentException("Invalid resource type: " + type);
    }

    private static int getResourceValue(String value) {
        if (value.contains("dp") || value.contains("dip")) {
            value = value.replace("dp", "").replace("dip", "");
        } else if (value.contains("sp")) {
            value = value.replace("sp", "");
        } else if (value.contains("px")) {
            value = value.replace("px", "");
        } else if (value.contains("in")) {
            value = value.replace("in", "");
        } else if (value.contains("pt")) {
            value = value.replace("pt", "");
        } else if (value.contains("mm")) {
            value = value.replace("mm", "");
        }
        return Integer.parseInt(value);
    }

    private static int getResourceValueType(String value) {
        int valType = -1;

        if (value.contains("dp") || value.contains("dip")) {
            valType = TypedValue.COMPLEX_UNIT_DIP;
        } else if (value.contains("sp")) {
            valType = TypedValue.COMPLEX_UNIT_SP;
        } else if (value.contains("px")) {
            valType = TypedValue.COMPLEX_UNIT_PX;
        } else if (value.contains("in")) {
            valType = TypedValue.COMPLEX_UNIT_IN;
        } else if (value.contains("pt")) {
            valType = TypedValue.COMPLEX_UNIT_PT;
        } else if (value.contains("mm")) {
            valType = TypedValue.COMPLEX_UNIT_MM;
        }

        return valType;
    }

    public static FabricatedOverlay getFabricatedOverlay(String overlayName, String targetPackage, String... resources) {
        if (resources.length == 0 || resources.length % 3 != 0) {
            throw new IllegalArgumentException("Mismatch in number of arguments.");
        }

        if (!overlayName.startsWith("IconifyComponent")) {
            overlayName = "IconifyComponent" + overlayName;
        }

        FabricatedOverlay fabricatedOverlay = new FabricatedOverlay(overlayName, targetPackage);

        for (int i = 0; i < resources.length; i += 3) {
            String resourceType = resources[i];
            String resourceName = resources[i + 1];
            String resourceValue = resources[i + 2];

            switch (resourceType) {
                case "color" -> {
                    if (resourceValue.startsWith("0x")) {
                        resourceValue = resourceValue.substring(2);
                    }
                    int colorInt = (int) Long.parseLong(resourceValue, 16);
                    if (resourceValue.length() <= 6) {
                        colorInt |= 0xFF000000;
                    }
                    fabricatedOverlay.setColor(targetPackage + ":" + resourceType + "/" + resourceName, colorInt);
                }
                case "dimen" -> {
                    int complexDimen = TypedValueUtil.createComplexDimension(getResourceValue(resourceValue), getResourceValueType(resourceValue));
                    fabricatedOverlay.setDimension(targetPackage + ":" + resourceType + "/" + resourceName, complexDimen);
                }
                case "bool" ->
                        fabricatedOverlay.setBoolean(targetPackage + ":" + resourceType + "/" + resourceName, resourceValue.equals("true") || resourceValue.equals("1"));
                case "integer" ->
                        fabricatedOverlay.setInteger(targetPackage + ":" + resourceType + "/" + resourceName, Integer.parseInt(resourceValue));
                case "attr" ->
                        fabricatedOverlay.setAttribute(targetPackage + ":" + resourceType + "/" + resourceName, Integer.parseInt(resourceValue));
            }
        }

        return fabricatedOverlay;
    }
}
