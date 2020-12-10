package com.example.common.permission;

import android.app.Activity;
import android.app.FragmentManager;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.List;

public class PermissionRequest {
    private static final String TAG = "PermissionsUtil";
    private PermissionFragment fragment;
    private static WeakReference<Activity> activity;

    public PermissionRequest(@NonNull Activity activity) {
        fragment = getPermissionsFragment(activity);
        PermissionRequest.activity = new WeakReference<>(activity);
    }

    private PermissionFragment getPermissionsFragment(Activity activity) {
        PermissionFragment fragment = (PermissionFragment) activity.getFragmentManager().findFragmentByTag(TAG);
        boolean isNewInstance = fragment == null;
        if (isNewInstance) {
            fragment = new PermissionFragment();
            FragmentManager fragmentManager = activity.getFragmentManager();
            fragmentManager
                    .beginTransaction()
                    .add(fragment, TAG)
                    .commit();
        }

        return fragment;
    }

    /**
     * 外部调用申请权限
     *
     * @param permissions 申请的权限
     * @param listener    监听权限接口
     */
    public void requestPermissions(String[] permissions, PermissionListener listener) {
        fragment.setListener(listener);
        fragment.requestPermissions(permissions);
    }

    //返回拒绝权限列表
    public static String deniedPermissionToMsg(List<String> deniedPermission) {
        return "";
    }
}
