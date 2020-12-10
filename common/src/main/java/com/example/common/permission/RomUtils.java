package com.example.common.permission;

import android.os.Build;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 判断系统ROM工具类
 */

public class RomUtils {
    private static final String TAG = "RomUtils";

    private static String getSystemProperty(String propName) {
        String line;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                }
            }
        }
        return line;
    }

    /**
     * 判断是否为华为系统
     */
    public static boolean isHuaweiRom() {
        String manufacturer = Build.MANUFACTURER;
        return !TextUtils.isEmpty(manufacturer) && manufacturer.contains("HUAWEI");
    }

    /**
     * 获取华为EmotionUI版本号，失败默认返回-1
     */
    public static double getEmotionUiVersion() {
        try {
            String emuiVersion = getSystemProperty("ro.build.version.emui");
            String version = emuiVersion.substring(emuiVersion.indexOf("_") + 1);
            return Double.parseDouble(version);
        } catch (Exception e) {
        }
        return -1;
    }

    /**
     * 判断是否为小米系统
     */
    public static boolean isMiuiRom() {
        return !TextUtils.isEmpty(getSystemProperty("ro.miui.ui.version.name"));
    }

    /**
     * 获取小米MIUI版本号，失败默认返回-1
     */
    public static int getMiuiVersion() {
        String version = getSystemProperty("ro.miui.ui.version.name");
        if (TextUtils.isEmpty(version)) {
            try {
                return Integer.parseInt(version.substring(1));
            } catch (Exception e) {
            }
        }
        return -1;
    }

    /**
     * 判断是否为魅族系统
     */
    public static boolean isMeizuRom() {
        String meizuFlymeOSFlag = getSystemProperty("ro.build.display.id");
        return !TextUtils.isEmpty(meizuFlymeOSFlag) && meizuFlymeOSFlag.toLowerCase().contains("flyme");
    }

    /**
     * 判断是否为360系统
     */
    public static boolean is360Rom() {
        String manufacturer = Build.MANUFACTURER;
        return !TextUtils.isEmpty(manufacturer) && manufacturer.contains("QiKU");
    }

    /**
     * 判断是否为乐视系统
     */
    public static boolean isLetvRom() {
        return !TextUtils.isEmpty(getSystemProperty("ro.letv.eui"));
    }

    /**
     * 判断是否为Oppo系统
     */
    public static boolean isOppoRom() {
        String a = getSystemProperty("ro.product.brand");
        return !TextUtils.isEmpty(a) && a.toLowerCase().contains("oppo");
    }

    /**
     * 判断是否为Vivo系统
     */
    public static boolean isVivoRom() {
        String a = getSystemProperty("ro.vivo.os.name");
        return !TextUtils.isEmpty(a) && a.toLowerCase().contains("funtouch");
    }

    /**
     * 判断是否为联想系统
     */
    public static boolean isLenovoRom() {
        String fingerPrint = Build.FINGERPRINT;
        if (!TextUtils.isEmpty(fingerPrint)) {
            return fingerPrint.contains("VIBEUI_V2");
        }
        String a = getSystemProperty("ro.build.version.incremental");
        return !TextUtils.isEmpty(a) && a.contains("VIBEUI_V2");
    }

    /**
     * 判断是否为CoolPad系统
     */
    public static boolean isCoolPadRom() {
        String model = Build.MODEL;
        String fingerPrint = Build.FINGERPRINT;
        return (!TextUtils.isEmpty(model) && model.toLowerCase().contains("coolpad"))
                || (!TextUtils.isEmpty(fingerPrint) && fingerPrint.toLowerCase().contains("coolpad"));
    }

    /**
     * 判断是否为中兴系统
     */
    public static boolean isZTERom() {
        String manufacturer = Build.MANUFACTURER;
        if (!TextUtils.isEmpty(manufacturer)) {
            return manufacturer.toLowerCase().contains("nubia") || manufacturer.toLowerCase().contains("zte");
        }
        String fingerPrint = Build.FINGERPRINT;
        if (!TextUtils.isEmpty(fingerPrint)) {
            return fingerPrint.toLowerCase().contains("nubia") || fingerPrint.toLowerCase().contains("zte");
        }
        return false;
    }
}
