package pw.rabit.otarecert;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by tim on 2017/10/10.
 */

public class ReCertHook implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!loadPackageParam.packageName.equals("android")) return;
        XposedBridge.log("Loading package: " + loadPackageParam.packageName);

        Class cls = XposedHelpers.findClass("com.android.server.pm.PackageManagerService", loadPackageParam.classLoader);

        XposedHelpers.findAndHookMethod(
                cls,
                "shouldCheckUpgradeKeySetLP",
                "com.android.server.pm.PackageSetting",
                int.class,
                XC_MethodReplacement.returnConstant(true));
        XposedHelpers.findAndHookMethod(
                cls,
                "checkUpgradeKeySetLP",
                "com.android.server.pm.PackageSetting",
                "android.content.pm.PackageParser$Package",
                new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                        Object sharedUser = XposedHelpers.getObjectField(methodHookParam.args[0], "sharedUser");
                        if (sharedUser != null) {
                            Object uSignatures = XposedHelpers.getObjectField(sharedUser, "signatures");
                            Object mSignatures = XposedHelpers.getObjectField(methodHookParam.args[1], "mSignatures");
                            XposedHelpers.setObjectField(uSignatures, "mSignatures", mSignatures);
                        }
                        return true;
                    }
                });
    }
}
