package com.lxl.login.demo;

import android.app.ActivityManager;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;


import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.List;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;


public class Utils {

    static final String TAG = "Utils";

    /**
     * 判断service是否已经运行
     * 必须判断uid,因为可能有重名的Service,所以要找自己程序的Service
     *
     * @param className Service的全名,例如PushService.class.getName() *
     * @return true:Service已运行 false:Service未运行
     */
    public static boolean isServiceRunning(Context context, String className) {
        Log.i(TAG, "===check service state...");
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

    /**
     * Toast显示即时信息：
     * 每次把上一条取消以打达到显示最新一条消息
     */
    private static Toast mToast = null;

    public static void ToastShow(final Context context, final int pos, final String mainText, final String subText) {
        //拼接主信息和次信息
        String toastText = mainText + (TextUtils.isEmpty(subText) ? "" : ("\t" + subText));

        //如果上一条还没显示完也立即取消
        if (mToast != null)
            mToast.cancel();

        mToast = Toast.makeText(context, toastText, Toast.LENGTH_LONG);
        mToast.setGravity(pos, 0, 0);
        mToast.show();
    }


    // 使用命令keytool -printcert -rfc -file xxx.cer 导出证书为字符串，然后将字符串转换为输入流，
    // 如果使用的是OkHttp可以直接使用new Buffer().writeUtf8(s).inputStream()

    /**
     * 返回SSLSocketFactory
     *
     * @param certificates 证书的输入流
     * @return SSLSocketFactory
     */
    public static SSLSocketFactory getSSLSocketFactory(InputStream... certificates) {
        return getSSLSocketFactory(null, certificates);
    }

    /**
     * 双向认证
     *
     * @param keyManagers  KeyManager[]
     * @param certificates 证书的输入流
     * @return SSLSocketFactory
     */
    public static SSLSocketFactory getSSLSocketFactory(KeyManager[] keyManagers, InputStream... certificates) {
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            int index = 0;
            for (InputStream certificate : certificates) {
                String certificateAlias = Integer.toString(index++);
                keyStore.setCertificateEntry(certificateAlias, certificateFactory.generateCertificate(certificate));
                try {
                    if (certificate != null)
                        certificate.close();
                } catch (IOException e) {
                }
            }
            SSLContext sslContext = SSLContext.getInstance("TLS");
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            sslContext.init(keyManagers, trustManagerFactory.getTrustManagers(), new SecureRandom());
            SSLSocketFactory socketFactory = sslContext.getSocketFactory();
            return socketFactory;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获得双向认证所需的参数
     *
     * @param bks          bks证书的输入流
     * @param keystorePass 秘钥
     * @return KeyManager[]对象
     */
    public static KeyManager[] getKeyManagers(InputStream bks, String keystorePass) {
        KeyStore clientKeyStore = null;
        try {
            clientKeyStore = KeyStore.getInstance("BKS");
            clientKeyStore.load(bks, keystorePass.toCharArray());
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(clientKeyStore, keystorePass.toCharArray());
            KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();
            return keyManagers;
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

