package com.lxl.login.demo;

import android.app.ActivityManager;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

public class Utils {

    static final String TAG = "Utils";

    /** * 判断service是否已经运行
     * 必须判断uid,因为可能有重名的Service,所以要找自己程序的Service
     * @param className Service的全名,例如PushService.class.getName() *
     * @return true:Service已运行 false:Service未运行 */
    public static boolean isServiceRunning(Context context, String className) {
        Log.i(TAG,"===check service state...");
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = am.getRunningServices(Integer.MAX_VALUE);
        int myUid = android.os.Process.myUid();
        for (ActivityManager.RunningServiceInfo runningServiceInfo : serviceList) {
            if (runningServiceInfo.uid == myUid && runningServiceInfo.service.getClassName().equals(className)) {
                return true;
            }
        }
        return false;
    }

    /**Toast显示即时信息：
     * 每次把上一条取消以打达到显示最新一条消息
     */
    private static Toast mToast = null;
    public static void ToastShow(final Context context, final int pos, final String mainText, final String subText){
        //拼接主信息和次信息
        String toastText = mainText + (TextUtils.isEmpty(subText) ? "" : ("\t"+subText));

        //如果上一条还没显示完也立即取消
        if(mToast != null)
            mToast.cancel();

        mToast = Toast.makeText(context, toastText, Toast.LENGTH_LONG);
        mToast.setGravity(pos, 0, 0);
        mToast.show();
    }
}
