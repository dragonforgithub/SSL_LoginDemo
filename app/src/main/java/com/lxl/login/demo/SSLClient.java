package com.lxl.login.demo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessControlException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class SSLClient {

    public static SSLContext ssl_ctx = null;
    public static SSLSocket ssl_socket = null;
    public static BufferedInputStream ssl_input = null;
    public static BufferedOutputStream ssl_output = null;
    //public static volatile BufferedWriter ssl_output = null;
    private static SSL_InitRead_Thread sslInitReadThread = null;
    private static SSL_Write_Thread sslWriteThread = null;

    private final String serverUrl = "47.98.206.54";
    private final String serverPort = "8081";
    /**消息类型定义*/
    public final int SSL_SENDMSG_TO_SERVER = 1; //发送消息给服务器端
    static  Handler mServiceHandler=null;

    public SSLClient(Context context)
    {
        try
        {
            //方式一：
            Log.i("SSL", "===Generate the CA Certificate from the raw resource file");
            InputStream caInput = context.getResources().openRawResource(R.raw.ca_cert);
            Certificate ca = CertificateFactory.getInstance("X.509").generateCertificate(caInput);

            Log.i("SSL", "===Load the key store using the CA");
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            Log.i("SSL", "===Initialize the TrustManager with this CA");
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);

            Log.i("SSL", "===Create an SSL context that uses the created trust manager");
            ssl_ctx = SSLContext.getInstance("TLS");
            ssl_ctx.init(null, tmf.getTrustManagers(), new SecureRandom());

            Log.i("SSL", "===getSocketFactory done!");

            /** ------------------------------------------------------------ */
            //方式二：
            /*
            KeyStore keyStore = KeyStore.getInstance("BKS"); // 访问keytool创建的Java密钥库
            InputStream keyStream = context.getResources().openRawResource(R.raw.test1);

            char keyStorePass[]="123321".toCharArray();  //证书密码
            keyStore.load(keyStream,keyStorePass);

            TrustManagerFactory trustManagerFactory =   TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);//保存服务端的授权证书

            ssl_ctx = SSLContext.getInstance("SSL");
            ssl_ctx.init(null, trustManagerFactory.getTrustManagers(), null);

            //return clientContext;
            */

            /** ------------------------------------------------------------ */
            //方式三：
            /*
            // Setup keystore
            KeyStore keyStore = KeyStore.getInstance("BKS");
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            InputStream keyStoreStream = context.getResources().openRawResource(R.raw.alitrust);
            keyStore.load(keyStoreStream, "123456".toCharArray());
            keyStoreStream.close(); //add

            keyManagerFactory.init(keyStore, "123456".toCharArray());

            // Setup truststore
            //KeyStore trustStore = KeyStore.getInstance("BKS");
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            //InputStream trustStoreStream = context.getResources().openRawResource(R.raw.alitrust);
            //trustStore.load(trustStoreStream, "123456".toCharArray());
            trustManagerFactory.init(keyStore);


            Log.d("SSL", "Key " + keyStore.size());
            //Log.d("SSL", "Trust " + trustStore.size());

            // Setup the SSL context to use the truststore and keystore
            ssl_ctx = SSLContext.getInstance("SSL"); //TLS
            ssl_ctx.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

            Log.d("SSL", "keyManagerFactory " + keyManagerFactory.getKeyManagers().length);
            Log.d("SSL", "trustManagerFactory " + trustManagerFactory.getTrustManagers().length);
            //---------------------
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
        }
        catch (NoSuchAlgorithmException nsae) {
            Log.e("SSL", nsae.getMessage());
        } catch (KeyStoreException kse) {
            Log.e("SSL", kse.getMessage());
        } catch (IOException ioe) {
            Log.e("SSL", ioe.getMessage());
        } catch (CertificateException ce) {
            Log.e("SSL", ce.getMessage());
        } catch (KeyManagementException kme) {
            Log.e("SSL", kme.getMessage());
        } catch(AccessControlException ace) {
            Log.e("SSL", ace.getMessage());
        } /*catch (UnrecoverableKeyException ube) {
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
                /*//(===socketFactory===)
                //1.创建监听指定服务器地址以及指定服务器监听的端口号
                SSLSocketFactory socketFactory = (SSLSocketFactory) ssl_ctx.getSocketFactory();
                ssl_socket = (SSLSocket) socketFactory.createSocket(serverUrl, Integer.parseInt(serverPort));
                //2.拿到客户端的socket对象的输出流发送给服务器数据
                ssl_input = new BufferedInputStream(ssl_socket.getInputStream());
                ssl_output = new BufferedOutputStream(ssl_socket.getOutputStream());
                Log.d("SSL", "Created the socket, input, and output done!!!");
                */

                //(===SSLSocket===)
                //1.创建监听指定服务器地址以及指定服务器监听的端口号
                ssl_socket = (SSLSocket) ssl_ctx.getSocketFactory().createSocket(serverUrl, Integer.parseInt(serverPort));
                //2.拿到客户端的socket对象的输出流发送给服务器数据
                ssl_output = new BufferedOutputStream(ssl_socket.getOutputStream());
                ssl_input = new BufferedInputStream(ssl_socket.getInputStream());

                // 开始监听服务器的消息
                do {
                    Log.d("SSL", "ssl_input.read .......");
                    byte[] buffer = new byte[128];
                    int length = ssl_input.read(buffer);
                    lineStr = new String(buffer, 0, length);
                    Log.d("SSL", "server : "+lineStr);

                } while (!lineStr.equals("exit|"));
                Log.w("SSL", "Read thread exit~");
            }
            catch (IOException ioe)
            {
                System.out.println(ioe);
            }
            finally
            {
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
                    String str2 = msg.getData().getString("text2");
                    String msgInte = str1 + str2;

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
                            break;
                    }
                    return false;
                }
            });
            Looper.loop();
        }
    }

    /**发送消息*/
    public void sendMessageToServer(String message)
    {
        /* //注：下面直接调用读写接口会crash，所以放到另一个线程处理网络交互
        Log.d("SSL", "send message : " + message);
        try {
            ssl_output.write(message.getBytes());
            ssl_output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        */

        Log.d("SSL", "send message : " + message);
        Message VendorMessage = new Message();
        //消息类型
        VendorMessage.what = SSL_SENDMSG_TO_SERVER;
        //消息内容
        Bundle bundle = new Bundle();
        bundle.putString("text1","msg from Client-");  //往Bundle中存放数据
        bundle.putString("text2","Said：" + message);  //往Bundle中put数据
        VendorMessage.setData(bundle);//mes利用Bundle传递数据
        //发送消息
        mServiceHandler.sendMessage(VendorMessage);
    }

    /**进程退出处理*/
    public void SSL_Close()
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