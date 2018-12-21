package com.lxl.login.demo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.AccessControlException;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.X509Certificate;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class SSLClient {

    public static SSLContext ssl_ctx = null;
    public static SSLSocket ssl_socket = null;
    public static BufferedInputStream ssl_input = null;
    public static BufferedOutputStream ssl_output = null;
    private static SSL_InitRead_Thread sslInitReadThread = null;
    private static SSL_Write_Thread sslWriteThread = null;

    private final String serverUrl = "47.98.206.54";
    private final String serverPort = "8081";
    /**消息类型定义*/
    public final int SSL_SENDMSG_TO_SERVER = 1; //发送消息给服务器端
    static  Handler mServiceHandler=null;

    private static Context mThis=null;

    public SSLClient(Context context)
    {
        mThis = context;
        try
        {
            //方式一 绕过证书认证（不安全）：
            /*
            X509TrustManager trustManager = new X509TrustManager() {

                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {

                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {

                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[0];
                }
            };

            ssl_ctx = SSLContext.getInstance("SSL");
            ssl_ctx.init(null,new X509TrustManager[]{trustManager},null);
            */

            //方式二 使用证书：
            // 取到证书的输入流
            Log.i("SSL", "===Generate the CA Certificate from the raw resource file");
            InputStream caInput = context.getResources().openRawResource(R.raw.ca_cert);
            Certificate ca = CertificateFactory.getInstance("X.509").generateCertificate(caInput);

            // 创建 Keystore 包含我们的证书
            Log.i("SSL", "===Load the key store using the CA");
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // 创建一个 TrustManager 仅把 Keystore 中的证书 作为信任的锚点
            Log.i("SSL", "===Initialize the TrustManager with this CA");
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);

            // 用 TrustManager 初始化一个 SSLContext
            Log.i("SSL", "===Create an SSL context that uses the created trust manager");
            ssl_ctx = SSLContext.getInstance("TLS");
            ssl_ctx.init(null, tmf.getTrustManagers(), new SecureRandom());

            Log.i("SSL", "===getSocketFactory done!");

            /** ------------------------------------------------------------ */
            //方式三 使用BKS：
            /*
            KeyStore keyStore = KeyStore.getInstance("BKS"); // 访问keytool创建的Java密钥库
            InputStream keyStream = context.getResources().openRawResource(R.raw.alitrust);

            char keyStorePass[]="123456".toCharArray();  //证书密码
            keyStore.load(keyStream,keyStorePass);

            TrustManagerFactory trustManagerFactory =   TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);//保存服务端的授权证书

            ssl_ctx = SSLContext.getInstance("SSL");
            ssl_ctx.init(null, trustManagerFactory.getTrustManagers(), null);
            */

            Log.d("SSL", "===start ssl thread... ");
            //启动初始化和数据读取线程
            if(sslInitReadThread == null){
                sslInitReadThread = new SSL_InitRead_Thread();
                sslInitReadThread.start();
            }
            //启动数据发送线程
            if(sslWriteThread == null){
                sslWriteThread = new SSL_Write_Thread();
                sslWriteThread.start();
            }

            Log.d("SSL", "===start ssl all thread. ");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }  catch (KeyStoreException kse) {
            Log.e("SSL", kse.getMessage());
        } catch (IOException ioe) {
            Log.e("SSL", ioe.getMessage());
        } catch (CertificateException ce) {
            Log.e("SSL", ce.getMessage());
        }  catch(AccessControlException ace) {
            Log.e("SSL", ace.getMessage());
        } /*catch (KeyManagementException kme) {
            Log.e("SSL", kme.getMessage());
        }catch (NoSuchAlgorithmException nsae) {
            Log.e("SSL", nsae.getMessage());
        }catch (UnrecoverableKeyException ube) {
            Log.e("SSL", ube.getMessage());
        }*/
    }

    // 初始化连接，并开始读取服务器发来的数据
    private class SSL_InitRead_Thread extends Thread {  // 在线程的run()中进行处理
        @Override
        public void run()
        {
            String lineStr;
            Log.d("SSL", "Created SSL_InitRead_Thread------");

            try
            {
                /** socketFactory 或 SSLSocket 实现SSL认证 */
                //(===socketFactory===)
                //1.创建监听指定服务器地址以及指定服务器监听的端口号
                SSLSocketFactory socketFactory = ssl_ctx.getSocketFactory();
                ssl_socket = (SSLSocket) socketFactory.createSocket(serverUrl, Integer.parseInt(serverPort));
                //2.拿到客户端的socket对象的输出流发送给服务器数据
                ssl_input = new BufferedInputStream(ssl_socket.getInputStream());
                ssl_output = new BufferedOutputStream(ssl_socket.getOutputStream());
                Log.d("SSL", "Created the socket, input, and output done!!!");

                // (===通过工具类获得ssl认证后的socket===)
                //SSLSocketFactory socketFactory = Utils.getSSLSocketFactory(mThis.getAssets().open("ca_cert.pem"));
                ////ssl_socket = (SSLSocket) socketFactory.createSocket(serverUrl, Integer.parseInt(serverPort));

                // 开始监听服务器的消息
                do {
                    Log.d("SSL", "ssl_input.read .......");
                    byte[] buffer = new byte[128];
                    int length = ssl_input.read(buffer);
                    lineStr = new String(buffer, 0, length);
                    Log.d("SSL", "server : "+lineStr);
                } while (!lineStr.equals("exit|"));
                Log.w("SSL", "Read thread exit~");
            } catch (IOException ioe) {
                System.out.println(ioe);
            } finally {
                Log.e("SSL", "Exception: All close!");
                try {
                    if(ssl_input != null){
                        ssl_input.close();
                    }
                    if(ssl_output != null){
                        ssl_output.close();
                    }
                    if(ssl_socket != null){
                        ssl_socket.close();
                    }
                } catch(IOException ioe) {
                    Log.e("SSL", ioe.getMessage());
                }
            }
        }
    }

    // 向服务器发送数据
    private class SSL_Write_Thread extends Thread {  // 在线程的run()中进行处理
        @Override
        public void run()
        {
            Looper.prepare(); //子线程需要手动绑定Looper对象
            Log.d("SSL", "Created SSL_Write_Thread------");
            // 消息处理线程,处理UI进程发来的消息
            mServiceHandler = new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    //msg传递过来的参数类型
                    int msgType = msg.what;
                    //msg传递过来的参数内容
                    String str1 = msg.getData().getString("text1");
                    //String str2 = msg.getData().getString("text2");
                    String msgInte = str1;// + str2;

                    switch (msgType) {
                        case SSL_SENDMSG_TO_SERVER:
                            try {
                                if(ssl_output != null) {
                                    Log.d("SSL", "write message : " + msgInte);
                                    ssl_output.write(msgInte.getBytes());
                                    ssl_output.flush();
                                }
                            } catch (IOException e) {
                                //e.printStackTrace();
                                Log.e("SSL", e.getMessage());
                            }

                            //TODO : test=====================
                            /*
                            //用HttpClient发送请求，分为五步
                            //第一步：创建HttpClient对象
                            HttpClient httpCient = new DefaultHttpClient();
                            //第二步：创建代表请求的对象,参数是访问的服务器地址
                            HttpGet httpGet = new HttpGet("https://47.98.206.54/NewVersionReq.ashx");

                            try {
                                //第三步：执行请求，获取服务器发还的相应对象
                                HttpResponse httpResponse = httpCient.execute(httpGet);
                                //第四步：检查相应的状态是否正常：检查状态码的值是200表示正常
                                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                                    //第五步：从相应对象当中取出数据，放到entity当中
                                    HttpEntity entity = httpResponse.getEntity();
                                    String response = EntityUtils.toString(entity, "utf-8");//将entity当中的数据转换为字符串
                                    Log.d("SSL", "get response  : " + response);

                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }*/

                            ////////////////////////////////////////////////////
                            /*
                            OkHttpClient client = new OkHttpClient();
                            Request request = new Request.Builder()
                                    .url("https://47.98.206.54/NewVersionReq.ashx")  //"http://www.baidu.com"
                                    .build();

                            Response response = null;
                            try {
                                response = client.newCall(request).execute();
                                if (!response.isSuccessful())
                                    throw new IOException("Unexpected code " + response);
                                else
                                    Log.d("SSL", "newCall success!");

                                Headers responseHeaders = response.headers();
                                for (int i = 0; i < responseHeaders.size(); i++) {
                                    Log.d("SSL", "get response  : "
                                            + responseHeaders.name(i) + ": " + responseHeaders.value(i));
                                }
                                Log.d("SSL", "get response  : " + response.body().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            */
                            //TODO : ==============================

                            break;
                    }
                    return false;
                }
            });
            Looper.loop();
        }
    }

    /**发送消息*/
    protected void sendMessageToServer(String message)
    {
        Message VendorMessage = new Message();
        //消息类型
        VendorMessage.what = SSL_SENDMSG_TO_SERVER;
        //消息内容
        Bundle bundle = new Bundle();
        bundle.putString("text1",message);  //往Bundle中存放数据
        //bundle.putString("text2"," - by client");  //往Bundle中put数据
        VendorMessage.setData(bundle);//mes利用Bundle传递数据
        //发送消息
        mServiceHandler.sendMessage(VendorMessage);
    }

    /**进程退出处理*/
    protected void SSL_Close()
    {
        Log.d("SSL", "ssl close...");
        try {
            if(ssl_input != null){
                ssl_input.close();
            }
            if(ssl_output != null){
                ssl_output.close();
            }
            if(ssl_socket != null){
                ssl_socket.close();
            }
        } catch(IOException ioe) {
            Log.e("SSL", ioe.getMessage());
        }
    }
}