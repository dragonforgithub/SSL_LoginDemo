package com.lxl.login.demo;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.Gravity;
import android.view.View;
import android.view.ViewAnimationUtils;

import com.lxl.login.demo.okhttp.CallBackUtil;
import com.lxl.login.demo.okhttp.OkhttpUtil;
import com.lxl.login.demo.ui.LoginDialogFragment;
import com.lxl.login.demo.ui.PhoneDialogFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;


public class MainActivity extends AppCompatActivity {
    /**handler消息类型定义*/
    public final int SSL_SENDMSG_GET_PATH = 1;      //获取下载地址
    public final int SSL_SENDMSG_DOWNLOAD_FILE = 2; //下载文件

    private PhoneDialogFragment fragment_Register;
    private LoginDialogFragment fragment_Login;
    private SSLClient sslClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO : 初始化C层进程，可移除
        //JniApiCall.jni_NanoOpen();

        fragment_Login = LoginDialogFragment.newInstance("");
        //fragment_Register.show(getFragmentManager(), "login"); //显示注册界面
        fragment_Register= new PhoneDialogFragment();
        //fragment_Login.show(getFragmentManager(), "register"); //显示登录界面

        /** 创建和服务器的SSL连接 */
        sslClient = new SSLClient(getApplicationContext());

        /**
         * 登录按钮
         */
        findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment_Login.show(getFragmentManager(), "login");

                //TODO : c层测试服务器接口，可移除
                byte[] testData = "[sheldon]:test server!".getBytes();
                //JniApiCall.jni_NanoLogin(testData,testData.length);

                try {
                    // 创建json数据包
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("Model", "KK309");
                    jsonObject.put("Vid", "0x1234");
                    jsonObject.put("Pid", "0x5678");
                    jsonObject.put("Version", 99);

                    // 发送数据
                    sslClient.sendMessageToServer(jsonObject.toString(1));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        /**
         * 注册按钮
         */
        findViewById(R.id.btn_register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment_Register.show(getFragmentManager(), "register");

                // 发送服务器访问消息，获取下载路径
                sendHttpsMessage(SSL_SENDMSG_GET_PATH, null);
            }
        });
    }

    // 处理https消息更新UI
    private Handler mHttpsHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            //msg传递过来的参数类型
            int msgType = msg.what;
            //msg传递过来的参数内容
            String str1 = msg.getData().getString("text1");
            //String str2 = msg.getData().getString("text2");
            String msgInte = str1;// + str2;

            try {
                switch (msgType) {
                    case SSL_SENDMSG_GET_PATH:
                        // 服务器地址
                        String url = "https://47.98.206.54/NewVersionReq.ashx";

                        // 发送json格式数据，获取下载路径
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("Model", "KK309");
                        jsonObject.put("Vid", 0x1234);
                        jsonObject.put("Pid", 0x5678);
                        jsonObject.put("Version", 99);

                        // 回调获取执行结果
                        OkhttpUtil.okHttpPostJson(url, jsonObject.toString(1), new CallBackUtil.CallBackString() {
                            @Override
                            public void onFailure(Call call, Exception e) {
                                Log.e("SSL", "error:" + e);
                            }

                            @Override
                            public void onResponse(String response) {
                                Utils.ToastShow(getApplicationContext(),Gravity.CENTER_HORIZONTAL,"Post:","Success");
                                Log.d("SSL","Post ---> " + response);

                                // 发送文件路径消息，下载文件
                                sendHttpsMessage(SSL_SENDMSG_DOWNLOAD_FILE, response);
                            }
                        });
                        break;

                    case SSL_SENDMSG_DOWNLOAD_FILE:
                        //解析下载路径，例如："https://47.98.206.54/KK309/V1234_P5678/101/remote.bin"
                        JSONObject jsObj = new JSONObject(msgInte); //转换成json对象
                        String jsPath = jsObj.getString("Path"); //获取Path值
                        String fileURL = jsPath.replaceAll("\\\\",""); //去除反斜杠转义
                        Log.d("SSL","Parse   ---> " + fileURL);

                        //设置本地保存路径
                        String fileDir  = "/sdcard/";
                        String fileName = "remote.bin";

                        // 回调获取执行结果
                        OkhttpUtil.okHttpDownloadFile(fileURL, new CallBackUtil.CallBackFile(fileDir, fileName) {
                            @Override
                            public void onFailure(Call call, Exception e) {
                                Log.e("SSL", "DownloadFile error:" + e);
                            }

                            @Override
                            public void onResponse(String response) {
                                Utils.ToastShow(getApplicationContext(),Gravity.CENTER_HORIZONTAL,"Post:","Success");
                                Log.d("SSL","DownloadFile(String) ---> " + response);
                            }

                            @Override
                            public void onResponse(File response) {
                                Log.d("SSL","DownloadFile(File) ---> " + response);
                            }
                        });

                        break;
                }
            } catch (JSONException e) {
                    e.printStackTrace();
            }
            return false;
        }
    });

    /**发送消息*/
    private void sendHttpsMessage(int msgType, String message)
    {
        Message VendorMessage = new Message();
        //消息类型
        VendorMessage.what = msgType;
        //消息内容
        Bundle bundle = new Bundle();
        bundle.putString("text1",message);  //往Bundle中存放数据
        //bundle.putString("text2"," - by client");  //后面可增加参数
        VendorMessage.setData(bundle);  //mes利用Bundle传递数据
        //发送消息
        mHttpsHandler.sendMessage(VendorMessage);
    }

    @Override
    protected void onDestroy() {
        sslClient.SSL_Close();
        super.onDestroy();
    }
}
