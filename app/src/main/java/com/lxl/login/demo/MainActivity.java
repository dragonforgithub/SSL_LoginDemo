package com.lxl.login.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;


public class MainActivity extends AppCompatActivity {
    private PhoneDialogFragment fragment_Register;
    private LoginDialogFragment fragment_Login;
    private SSLClient sslClient;

    //消息类型定义
    public final int SSL_SENDMSG_TO_SERVER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /** 初始化C层进程 */
        //JniApiCall.jni_NanoOpen();

        fragment_Login = LoginDialogFragment.newInstance("");
        //fragment_Register.show(getFragmentManager(), "login");
        fragment_Register= new PhoneDialogFragment();
        //fragment_Login.show(getFragmentManager(), "register");

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

                // Java层实现ssl认证
                sslClient.sendMessageToServer("要在子线程处理网络交互！！！");
            }
        });

        /**
         * 注册按钮
         */
        findViewById(R.id.btn_register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment_Register.show(getFragmentManager(), "register");
            }
        });
    }

    @Override
    protected void onDestroy() {
        sslClient.SSL_Close();
        super.onDestroy();
    }
}
