package com.example.common.permission;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import java.util.List;

public class PermissionUtils {

    public static void goManage(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//如果是6.0以上调用系统提供方法
            applyCommonPermission(context);
        } else {//4.4-5.1.1根据不同手机厂商进行跳转
            openFloatWindowSetting(context);
        }
    }

    /**
     * 6.0及以上调用
     *
     * @param context
     */
    private static void applyCommonPermission(Context context) {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            startSettingActivity(context, intent);
        } catch (Exception e) {
//            NoticeUtils.showToast(R.string.open_float_pemission_failed);
        }
    }

    /**
     * 成功跳转到设置界面
     */
    private static boolean toSettingActivitySuccess;

    /**
     * 6.0以下悬浮窗设置权限
     *
     * @param context
     */
    private static void openFloatWindowSetting(Context context) {

        toSettingActivitySuccess = false;

        if (RomUtils.isHuaweiRom()) {//华为
            applyHuaweiPermission(context);
        } else if (RomUtils.isMiuiRom()) {
            applyMiuiPermission(context);//小米
        } else if (RomUtils.isLenovoRom()) {
            applyLenovoPermission(context);//联想
        } else if (RomUtils.isOppoRom()) {
            applyOppoPermission(context);//Oppo
        } else if (RomUtils.isZTERom()) {
            applyZTEPermission(context);//中兴
        } else if (RomUtils.is360Rom()) {
            apply360Permission(context);//360
        } else if (RomUtils.isVivoRom()) {
            applyVivoPermission(context);//vivo
        } else if (RomUtils.isMeizuRom()) {
            applyMeizuPermission(context);//魅族
        } else if (RomUtils.isCoolPadRom()) {
            applyCoolpadPermission(context);//coolpad
        } else if (RomUtils.isLetvRom()) {//乐视
            applyLetvPermission(context);
        }

        if (!toSettingActivitySuccess) {//跳转失败
//            NoticeUtils.showToast(R.string.open_float_pemission_failed);
        }
    }

    /**
     * 通过intent来判断此intent是否存在
     *
     * @param context
     * @param intent
     * @return
     */
    public static boolean isIntentAvailable(Intent intent, Context context) {
        try {
            final PackageManager packageManager = context.getPackageManager();
            List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
                    PackageManager.MATCH_DEFAULT_ONLY);
            return list != null && list.size() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 360权限申请
     */
    private static void apply360Permission(Context context) {
        Intent intent = new Intent();
        intent.setClassName("com.android.settings", "com.android.settings.Settings$OverlaySettingsActivity");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (isIntentAvailable(intent, context)) {
            context.startActivity(intent);
            toSettingActivitySuccess = true;
        } else {
            intent.setClassName("com.qihoo360.mobilesafe", "com.qihoo360.mobilesafe.ui.index.AppEnterActivity");
            if (isIntentAvailable(intent, context)) {
                startSettingActivity(context, intent);
                toSettingActivitySuccess = true;
            } else {
                toSettingActivitySuccess = false;
            }
        }
    }

    /**
     * 小米权限申请
     */
    private static void applyMiuiPermission(Context context) {
        Intent intent = new Intent();
        intent.setAction("miui.intent.action.APP_PERM_EDITOR");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra("extra_pkgname", context.getPackageName());
        if (isIntentAvailable(intent, context)) {
            startSettingActivity(context, intent);
            toSettingActivitySuccess = true;
        } else {
            toSettingActivitySuccess = false;
        }
    }

    /**
     * 魅族权限申请
     */
    private static void applyMeizuPermission(Context context) {
        Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
        intent.setClassName("com.meizu.safe", "com.meizu.safe.security.AppSecActivity");
        intent.putExtra("packageName", context.getPackageName());
        if (isIntentAvailable(intent, context)) {
            startSettingActivity(context, intent);
            toSettingActivitySuccess = true;
        } else {
            toSettingActivitySuccess = false;
        }
    }

    /**
     * 华为权限申请
     */
    private static void applyHuaweiPermission(Context context) {
        try {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName comp = new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.addviewmonitor.AddViewMonitorActivity");
            intent.setComponent(comp);
            if (isIntentAvailable(intent, context)) {
                context.startActivity(intent);
                startSettingActivity(context, intent);
            } else {
                comp = new ComponentName("com.huawei.systemmanager", "com.huawei.notificationmanager.ui.NotificationManagmentActivity");
                intent.setComponent(comp);
                startSettingActivity(context, intent);
            }
            toSettingActivitySuccess = true;
        } catch (SecurityException e) {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName comp = new ComponentName("com.huawei.systemmanager",
                    "com.huawei.permissionmanager.ui.MainActivity");
            intent.setComponent(comp);
            startSettingActivity(context, intent);
            toSettingActivitySuccess = true;
        } catch (ActivityNotFoundException e) {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName comp = new ComponentName("com.Android.settings", "com.android.settings.permission.TabItem");
            intent.setComponent(comp);
            startSettingActivity(context, intent);
            toSettingActivitySuccess = true;
        } catch (Exception e) {
            e.printStackTrace();
            toSettingActivitySuccess = false;
        }
    }

    /**
     * CoolPad权限申请
     */
    private static void applyCoolpadPermission(Context context) {
        try {
            Intent intent = new Intent();
            intent.setClassName("com.yulong.android.seccenter", "com.yulong.android.seccenter.dataprotection.ui.AppListActivity");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (isIntentAvailable(intent, context)) {
                startSettingActivity(context, intent);
                toSettingActivitySuccess = true;
            } else {
                toSettingActivitySuccess = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            toSettingActivitySuccess = false;
        }
    }

    /**
     * 联想权限申请
     */
    private static void applyLenovoPermission(Context context) {
        try {
            Intent intent = new Intent();
            intent.setClassName("com.lenovo.safecenter", "com.lenovo.safecenter.MainTab.LeSafeMainActivity");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (isIntentAvailable(intent, context)) {
                startSettingActivity(context, intent);
                toSettingActivitySuccess = true;
            } else {
                toSettingActivitySuccess = false;
            }
        } catch (Exception e) {
            toSettingActivitySuccess = false;
            e.printStackTrace();
        }
    }

    /**
     * 中兴权限申请
     */
    private static void applyZTEPermission(Context context) {
        try {
            Intent intent = new Intent();
            intent.setAction("com.zte.heartyservice.intent.action.startActivity.PERMISSION_SCANNER");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (isIntentAvailable(intent, context)) {
                startSettingActivity(context, intent);
                toSettingActivitySuccess = true;
            } else {
                toSettingActivitySuccess = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            toSettingActivitySuccess = false;
        }
    }

    /**
     * 乐视权限申请
     */
    private static void applyLetvPermission(Context context) {
        try {
            Intent intent = new Intent();
            intent.setClassName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AppActivity");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (isIntentAvailable(intent, context)) {
                startSettingActivity(context, intent);
                toSettingActivitySuccess = true;
            } else {
                toSettingActivitySuccess = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            toSettingActivitySuccess = false;
        }
    }

    /**
     * Vivo权限申请
     */
    private static void applyVivoPermission(Context context) {
        try {
            Intent intent = new Intent();
            intent.setClassName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.FloatingWindowManager");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (isIntentAvailable(intent, context)) {
                startSettingActivity(context, intent);
                toSettingActivitySuccess = true;
            } else {
                toSettingActivitySuccess = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            toSettingActivitySuccess = false;
        }
    }

    /**
     * Oppo权限申请
     */
    private static void applyOppoPermission(Context context) {
        try {
            Intent intent = new Intent();
            intent.setClassName("com.oppo.safe", "com.oppo.safe.permission.PermissionAppListActivity");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (isIntentAvailable(intent, context)) {
                startSettingActivity(context, intent);
                toSettingActivitySuccess = true;
            } else {
                toSettingActivitySuccess = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            toSettingActivitySuccess = false;
        }
    }

    /**
     * 跳转到设置界面
     */
    private static void startSettingActivity(Context context, Intent intent) {

        if (context instanceof Activity) {//为了统计从设置界面返回时的结果
            try {
                ((Activity) context).startActivityForResult(intent, 0x1002);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            context.startActivity(intent);
        }
    }
}
