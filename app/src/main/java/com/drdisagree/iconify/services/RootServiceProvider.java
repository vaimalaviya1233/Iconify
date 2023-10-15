package com.drdisagree.iconify.services;

import android.content.Intent;
import android.content.om.IOverlayManager;
import android.content.om.OverlayIdentifier;
import android.content.om.OverlayInfo;
import android.content.om.OverlayManagerTransaction;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.drdisagree.iconify.utils.extension.MethodInterface;
import com.drdisagree.iconify.utils.overlay.FabricatedUtil;
import com.drdisagree.iconify.utils.overlay.fabricated.FabricatedOverlay;
import com.drdisagree.iconify.utils.overlay.fabricated.FabricatedOverlayEntry;
import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.ipc.RootService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import rikka.shizuku.SystemServiceHelper;

@SuppressWarnings({"all"})
public class RootServiceProvider extends RootService {

    static String TAG = RootServiceProvider.class.getSimpleName();

    @Override
    public IBinder onBind(@NonNull Intent intent) {
        return new RootServiceImpl();
    }

    static class RootServiceImpl extends IRootServiceProvider.Stub {

        private static final UserHandle currentUser;
        private static final int currentUserId;
        private static IOverlayManager mOMS;
        private static Class<?> oiClass;
        private static Class<?> foClass;
        private static Class<?> fobClass;
        private static Class<?> omtbClass;

        static {
            currentUser = getCurrentUser();
            currentUserId = getCurrentUserId();
            if (mOMS == null) {
                mOMS = IOverlayManager.Stub.asInterface(SystemServiceHelper.getSystemService("overlay"));
            }

            try {
                if (oiClass == null) {
                    oiClass = Class.forName("android.content.om.OverlayIdentifier");
                }
                if (foClass == null) {
                    foClass = Class.forName("android.content.om.FabricatedOverlay");
                }
                if (fobClass == null) {
                    fobClass = Class.forName("android.content.om.FabricatedOverlay$Builder");
                }
                if (omtbClass == null) {
                    omtbClass = Class.forName("android.content.om.OverlayManagerTransaction$Builder");
                }
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "static: ", e);
            }
        }

        private static UserHandle getCurrentUser() {
            return Process.myUserHandle();
        }

        private static Integer getCurrentUserId() {
            try {
                return (Integer) UserHandle.class.getMethod("getIdentifier").invoke(currentUser);
            } catch (NoSuchMethodException | IllegalAccessException |
                     InvocationTargetException exception) {
                return 0;
            }
        }

        private static IOverlayManager getOMS() {
            if (mOMS == null) {
                mOMS = IOverlayManager.Stub.asInterface(SystemServiceHelper.getSystemService("overlay"));
            }
            return mOMS;
        }

        /**
         * Return true if an overlay package is installed.
         */
        @Override
        public boolean isOverlayInstalled(String packageName) throws RemoteException {
            OverlayInfo info = getOMS().getOverlayInfo(packageName, currentUserId);
            return info != null;
        }

        /**
         * Return true if an overlay package is enabled.
         */
        @Override
        public boolean isOverlayEnabled(String packageName) throws RemoteException {
            OverlayInfo info = getOMS().getOverlayInfo(packageName, currentUserId);
            try {
                Boolean enabled = (Boolean) OverlayInfo.class.getMethod("isEnabled").invoke(info);
                return info != null && enabled != null && enabled;
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                Log.e(TAG, "isOverlayEnabled: ", e);
                return false;
            }
        }

        /**
         * Request that an overlay package be enabled when possible to do so.
         */
        @Override
        public void enableOverlay(List<String> packages) throws RemoteException {
            for (String p : packages) {
                switchOverlay(p, true);
            }
        }

        @Override
        public void enableOverlayWithIdentifier(List<String> packages) throws RemoteException {
            for (String p : packages) {
                if (!p.startsWith("IconifyComponent")) {
                    p = "IconifyComponent" + p;
                }
                OverlayIdentifier identifier = generateOverlayIdentifier(p);
                switchOverlayWithIdentifier(identifier, true);
            }
        }

        /**
         * Request that an overlay package is enabled and any other overlay packages with the same
         * target package are disabled.
         */
        @Override
        public boolean enableOverlayExclusive(String packageName) throws RemoteException {
            return getOMS().setEnabledExclusive(packageName, true, currentUserId);
        }

        /**
         * Request that an overlay package is enabled and any other overlay packages with the same
         * target package and category are disabled.
         */
        @Override
        public boolean enableOverlayExclusiveInCategory(String packageName) throws RemoteException {
            return getOMS().setEnabledExclusiveInCategory(packageName, currentUserId);
        }

        /**
         * Request that an overlay package be disabled when possible to do so.
         */
        @Override
        public void disableOverlay(List<String> packages) throws RemoteException {
            for (String p : packages) {
                switchOverlay(p, false);
            }
        }

        @Override
        public void disableOverlayWithIdentifier(List<String> packages) throws RemoteException {
            for (String p : packages) {
                if (!p.startsWith("IconifyComponent")) {
                    p = "IconifyComponent" + p;
                }
                OverlayIdentifier identifier = generateOverlayIdentifier(p);
                switchOverlayWithIdentifier(identifier, false);
            }
        }

        private void switchOverlay(String packageName, boolean enable) {
            try {
                getOMS().setEnabled(packageName, enable, currentUserId);
            } catch (Exception e) {
                Log.e(TAG, "switchOverlay: ", e);
            }
        }

        private void switchOverlayWithIdentifier(OverlayIdentifier identifier, boolean enable) {
            try {
                Object omtbInstance = omtbClass.newInstance();

                omtbClass.getMethod(
                        "setEnabled",
                        OverlayIdentifier.class,
                        boolean.class,
                        int.class
                ).invoke(
                        omtbInstance,
                        identifier,
                        enable,
                        currentUserId
                );

                Object omtInstance = omtbClass.getMethod(
                        "build"
                ).invoke(omtbInstance);

                commit(omtInstance);
            } catch (Exception e) {
                Log.e(TAG, "switchOverlayWithIdentifier: ", e);
            }
        }

        /**
         * Registers the fabricated overlay with the overlay manager so it can be enabled and
         * disabled for any user.
         * <p>
         * The fabricated overlay is initialized in a disabled state. If an overlay is re-registered
         * the existing overlay will be replaced by the newly registered overlay and the enabled
         * state of the overlay will be left unchanged if the target package and target overlayable
         * have not changed.
         *
         * @param overlay the overlay to register with the overlay manager
         */
        @Override
        public void registerFabricatedOverlay(String overlayName, String targetPackage, String resourceType, String resourceName, String resourceValue) {
            try {
                FabricatedOverlay overlay = FabricatedUtil.getFabricatedOverlay(overlayName, targetPackage, resourceType, resourceName, resourceValue);

                Object fobInstance = fobClass.getConstructor(
                        String.class,
                        String.class,
                        String.class
                ).newInstance(
                        overlay.sourcePackage,
                        overlay.overlayName,
                        overlay.targetPackage
                );

                Method setResourceValueMethod = fobClass.getMethod(
                        "setResourceValue",
                        String.class,
                        int.class,
                        int.class
                );

                for (Map.Entry<String, FabricatedOverlayEntry> entry : overlay.getEntries().entrySet()) {
                    FabricatedOverlayEntry overlayEntry = entry.getValue();
                    setResourceValueMethod.invoke(
                            fobInstance,
                            overlayEntry.getResourceName(),
                            overlayEntry.getResourceType(),
                            overlayEntry.getResourceValue()
                    );
                }

                Object foInstance = fobClass.getMethod("build").invoke(fobInstance);

                Object omtbInstance = omtbClass.newInstance();

                omtbClass.getMethod(
                        "registerFabricatedOverlay",
                        foClass
                ).invoke(
                        omtbInstance,
                        foInstance
                );

                Object omtInstance = omtbClass.getMethod("build").invoke(omtbInstance);

                commit(omtInstance);
            } catch (Exception e) {
                Log.e(TAG, "registerFabricatedOverlay: ", e);
            }
        }

        /**
         * Disables and removes the overlay from the overlay manager for all users.
         *
         * @param overlay the overlay to disable and remove
         */
        @Override
        public void unregisterFabricatedOverlay(@NonNull String packageName) {
            try {
                OverlayIdentifier overlay = generateOverlayIdentifier(packageName);
                if (overlay == null) {
                    return;
                }

                Object omtbInstance = omtbClass.newInstance();
                omtbClass.getMethod(
                        "unregisterFabricatedOverlay",
                        OverlayIdentifier.class
                ).invoke(
                        omtbInstance,
                        overlay
                );

                Object omtInstance = omtbClass.getMethod(
                        "build"
                ).invoke(omtbInstance);

                commit(omtInstance);
            } catch (Exception e) {
                Log.e(TAG, "unregisterFabricatedOverlay: ", e);
            }
        }

        /**
         * Change the priority of the given overlay to the highest priority relative to
         * the other overlays with the same target and user.
         */
        @Override
        public boolean setHighestPriority(String packageName) throws RemoteException {
            return (boolean) getOMS().setHighestPriority(packageName, currentUserId);
        }

        /**
         * Change the priority of the given overlay to the lowest priority relative to
         * the other overlays with the same target and user.
         */
        @Override
        public boolean setLowestPriority(String packageName) throws RemoteException {
            return (boolean) getOMS().setLowestPriority(packageName, currentUserId);
        }

        /**
         * Uninstall any overlay updates for the given package name.
         */
        @Override
        public void uninstallOverlayUpdates(String packageName) throws RemoteException {
            runCommand(Collections.singletonList("pm uninstall " + packageName));
        }

        /**
         * Restart systemui.
         */
        @Override
        public void restartSystemUI() throws RemoteException {
            runCommand(Collections.singletonList("killall com.android.systemui"));
        }

        /**
         * Run list of commands as root.
         */
        @Override
        public String[] runCommand(List<String> command) {
            return Shell.cmd(command.toArray(new String[0])).exec().getOut().toArray(new String[0]);
        }

        public OverlayIdentifier generateOverlayIdentifier(String packageName) throws RemoteException {
            return generateOverlayIdentifier(packageName, "com.android.shell");
        }

        private static OverlayIdentifier generateOverlayIdentifier(String packageName, String sourcePackage) {
            try {
                return (OverlayIdentifier) oiClass.getConstructor(String.class, String.class).newInstance(sourcePackage, packageName);
            } catch (Exception e) {
                Log.e("FabricatedOverlay", "generateOverlayIdentifier: ", e);
                return null;
            }
        }

        private void commit(Object transaction) throws Exception {
            getOMS().commit((OverlayManagerTransaction) transaction);
        }

        /**
         * Run a method with root.
         */
        @Override
        public void runWithRoot(MethodInterface method) {
            method.run();
        }
    }
}
