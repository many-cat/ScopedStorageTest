package com.example.common.permission;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;


public class PermissionFragment extends Fragment {
    /**
     * 申请权限的requestCode
     */
    private static final int PERMISSIONS_REQUEST_CODE = 1;

    /**
     * 权限监听接口
     */
    private PermissionListener listener;
    private boolean isRequestPermissionsOnAttch = false;
    private String[] permissions;

    public void setListener(PermissionListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        //解决API低于23的手机，没有执行onAttach(Context context)，导致权限没有请求(或者使用已经废弃的onAttach(Activity activity)方法)
        if (isRequestPermissionsOnAttch) {
            requestPermisions(permissions);
            isRequestPermissionsOnAttch = false;
        }
    }

    /**
     * 申请权限
     *
     * @param permissions 需要申请的权限
     */
    public void requestPermissions(@NonNull String[] permissions) {
        this.permissions = permissions;
        if (isAdded()) {
            requestPermisions(permissions);
        } else {
            isRequestPermissionsOnAttch = true;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermisions(String[] permissions) {
        if (permissions == null) {
            return;
        }
        List<String> requestPermissionList = new ArrayList<>();
        //找出所有未授权的权限
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(getActivity(), permission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionList.add(permission);
            }
        }
        if (requestPermissionList.isEmpty()) {
            //已经全部授权
            permissionAllGranted();
        } else {
            //申请授权
            requestPermissions(requestPermissionList.toArray(new String[requestPermissionList.size()]), PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * It's not called because this method has been added in API 23. If you run your application
     * on a device with API 23 (marshmallow) then onAttach(Context) will
     * be called. On all previous Android Versions onAttach(Activity) will
     * be called.
     *
     * @param context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (isRequestPermissionsOnAttch) {
            requestPermisions(permissions);
            isRequestPermissionsOnAttch = false;
        }
    }

    /**
     * fragment回调处理权限的结果
     *
     * @param requestCode  请求码 要等于申请时候的请求码
     * @param permissions  申请的权限
     * @param grantResults 对应权限的处理结果
     */
    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != PERMISSIONS_REQUEST_CODE) {
            return;
        }

        if (grantResults.length > 0) {
            List<String> deniedPermissionList = new ArrayList<>();
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissionList.add(permissions[i]);
                }
            }

            if (deniedPermissionList.isEmpty()) {
                //已经全部授权
                permissionAllGranted();
            } else {
                //勾选了对话框中”Don’t ask again”的选项, 返回false
                for (String deniedPermission : deniedPermissionList) {
                    boolean flag = shouldShowRequestPermissionRationale(deniedPermission);
                    if (!flag) {
                        //拒绝授权并勾选了不再询问
                        permissionShouldShowRationale(deniedPermissionList);
                        return;
                    }
                }
                //拒绝授权
                permissionHasDenied(deniedPermissionList);

            }

        }

    }


    /**
     * 权限全部已经授权
     */
    private void permissionAllGranted() {
        if (listener != null) {
            listener.onGranted();
        }
    }

    /**
     * 有权限被拒绝
     *
     * @param deniedList 被拒绝的权限
     */
    private void permissionHasDenied(List<String> deniedList) {
        if (listener != null) {
            listener.onDenied(deniedList);
        }
    }

    /**
     * 权限被拒绝并且勾选了不在询问
     *
     * @param deniedList 勾选了不在询问的权限
     */
    private void permissionShouldShowRationale(List<String> deniedList) {
        if (listener != null) {
            listener.onShouldShowRationale(deniedList);
        }
    }

}
