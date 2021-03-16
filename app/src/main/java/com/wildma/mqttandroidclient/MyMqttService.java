package com.wildma.mqttandroidclient;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Author       wildma
 * Github       https://github.com/wildma
 * CreateDate   2018/11/08
 * Desc	        ${MQTT服务}
 */

public class MyMqttService extends Service {
    public static final String TAG = MyMqttService.class.getSimpleName();
    private static MqttAndroidClient mqttAndroidClient;
    private MqttConnectOptions mMqttConnectOptions;
    public String HOST = "tcp://dev.emqx.sanyevi.cn:10220";//服务器地址（协议+地址+端口号）
    public String USERNAME = "EVICLOUD\\penght";//用户名
    public String PASSWORD = "witsight_eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJwZW5naHQiLCJjcmVhdGVkIjoxNTk3MTI2NjM0MDAwLCJ1c2VySWQiOiJkMDUyNDVhOS04ZDU4LTQ3YjctYjU3NS1lMzAzNjYwZmEyYTkiLCJhcHBDb2RlIjoiaHVheCIsImF1ZCI6IndlYiJ9.XNfe-KVw_GEDLDsLbPNFKAT6_KXNwP2c4wTqmCd-xNA2vNrEHk6FR0aCN_JAt0bPpG1vYsf-BHXsLaf7Za1eSw-76sasGUopvD5RE8wed2IwCEdEm3BDwS9kQjhChDz3gt9f1_ZiAMXvOdJ3EGbAcCng_m74t796DkFfrbj07IQ";//密码

    public int ConnectionTimeout = 100; //设置超时时间，单位：秒
    public int KeepAliveInterval = 10; //设置心跳包发送间隔，单位：秒

    public static String PUBLISH_TOPIC = "Device/app/test";//发布主题
    public static String RESPONSE_TOPIC = "Device/app/test";//响应主题
    @SuppressLint("MissingPermission")
    //客户端ID，一般以客户端唯一标识符表示，这里用设备序列号表示
    public String CLIENTID = "AppTestClient1211";

    /**
     *
     Server:tcp:test.emqx.sanyevi.cn:10222
     clientId:AppTestClient1211
     username:EVICLOUD\\gongzj
     password:witsight_eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJnb25nemoiLCJjcmVhdGVkIjoxNjAzODY4NjczMDAwLCJ1c2VySWQiOiI5NGE2MTNiMy04NWIzLTQwNmQtOTQ3MS00NmVjNTZhNGJhOTgiLCJhcHBDb2RlIjoiaHVheCIsImF1ZCI6IndlYiJ9.NFhA-R_sMjynwI4klBOC2kyQ4IKT30kRSuiac4bTrqHDYkin8kR73oen4DNhXFWH4K4l3iB16YVxxPuHSRNptu7wJT5Y_pACPXyF8NOddlRTrxWoFXWX0zlpQz2mljoknxJEI-mp_poAnCHSiK0E-agVG9j8jstZtTsZoi5Fs20
     */

    /**
     * 各位好,如果走外网，请使用以下地址
     * 地址：tcp://dev.emqx.sanyevi.cn:10220
     * 用户名：EVICLOUD\penght
     * 密码：witsight_eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJwZW5naHQiLCJjcmVhdGVkIjoxNTk3MTI2NjM0MDAwLCJ1c2VySWQiOiJkMDUyNDVhOS04ZDU4LTQ3YjctYjU3NS1lMzAzNjYwZmEyYTkiLCJhcHBDb2RlIjoiaHVheCIsImF1ZCI6IndlYiJ9.XNfe-KVw_GEDLDsLbPNFKAT6_KXNwP2c4wTqmCd-xNA2vNrEHk6FR0aCN_JAt0bPpG1vYsf-BHXsLaf7Za1eSw-76sasGUopvD5RE8wed2IwCEdEm3BDwS9kQjhChDz3gt9f1_ZiAMXvOdJ3EGbAcCng_m74t796DkFfrbj07IQ
     * 注意：topic必须是Device/+具体业务。如：Device/app/test
     */

    /**
     * 本地服务
     * public String HOST = "tcp://192.168.0.139:61613";//服务器地址（协议+地址+端口号）
     * public String USERNAME = "admin";//用户名
     * public String PASSWORD = "password";//密码
     */


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand: ");
        init();
        return super.onStartCommand(intent, flags, startId);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static long sendTime, receiveTime;

    /**
     * 发布 （模拟其他客户端发布消息）
     *
     * @param message 消息
     */
    public static void publish(String message) {
        Log.e(TAG, "publish: message==" + message);
        String topic = PUBLISH_TOPIC;
        Integer qos = 2;
        Boolean retained = false;
        try {
            sendTime = System.currentTimeMillis();
            //参数分别为：主题、消息的字节数组、服务质量、是否在服务器保留断开连接后的最后一条消息
            mqttAndroidClient.publish(topic, message.getBytes(), qos.intValue(), retained.booleanValue());
        } catch (MqttException e) {
            e.printStackTrace();
            Log.e(TAG, "publish: " + e.getMessage());
        }
    }

    /**
     * 响应 （收到其他客户端的消息后，响应给对方告知消息已到达或者消息有问题等）
     *
     * @param message 消息
     */
    public void response(String message) {
        receiveTime = System.currentTimeMillis();
        Log.e(TAG, "response: 收到消息:" + message + ",dt==" + (receiveTime - sendTime));
        String topic = RESPONSE_TOPIC;
        Integer qos = 2;
        Boolean retained = false;
        try {
            //参数分别为：主题、消息的字节数组、服务质量、是否在服务器保留断开连接后的最后一条消息
            sendTime = System.currentTimeMillis();
            Log.e(TAG, "response: 发送消息");
            mqttAndroidClient.publish(topic, message.getBytes(), qos.intValue(), retained.booleanValue());
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化
     */
    private void init() {
        Log.e(TAG, "init: ");
        String serverURI = HOST; //服务器地址（协议+地址+端口号）
        mqttAndroidClient = new MqttAndroidClient(this, serverURI, CLIENTID);
        mqttAndroidClient.setCallback(mqttCallback); //设置监听订阅消息的回调
        mMqttConnectOptions = new MqttConnectOptions();
        mMqttConnectOptions.setCleanSession(true); //设置是否清除缓存
        mMqttConnectOptions.setConnectionTimeout(ConnectionTimeout); //设置超时时间，单位：秒
        mMqttConnectOptions.setKeepAliveInterval(KeepAliveInterval); //设置心跳包发送间隔，单位：秒
        mMqttConnectOptions.setUserName(USERNAME); //设置用户名
        mMqttConnectOptions.setPassword(PASSWORD.toCharArray()); //设置密码

        // last will message
        boolean doConnect = true;
        String message = "{\"terminal_uid\":\"" + CLIENTID + "\"}";
        String topic = PUBLISH_TOPIC;
        Integer qos = 2;
        Boolean retained = false;
        if ((!message.equals("")) || (!topic.equals(""))) {
            // 最后的遗嘱
            try {
                mMqttConnectOptions.setWill(topic, message.getBytes(), qos.intValue(), retained.booleanValue());
            } catch (Exception e) {
                Log.i(TAG, "Exception Occured", e);
                doConnect = false;
                iMqttActionListener.onFailure(null, e);
            }
        }
        if (doConnect) {
            doClientConnection();
        }
    }

    /**
     * 连接MQTT服务器
     */
    private void doClientConnection() {
        Log.e(TAG, "doClientConnection: ");
        if (!mqttAndroidClient.isConnected() && isConnectIsNomarl()) {
            try {
                mqttAndroidClient.connect(mMqttConnectOptions, null, iMqttActionListener);
            } catch (MqttException e) {
                e.printStackTrace();
                Log.e(TAG, "doClientConnection: " + e.getMessage());
            }
        }
    }

    /**
     * 判断网络是否连接
     */
    private boolean isConnectIsNomarl() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            String name = info.getTypeName();
            Log.i(TAG, "当前网络名称：" + name);
            return true;
        } else {
            Log.i(TAG, "没有可用网络");
            /*没有可用网络的时候，延迟3秒再尝试重连*/
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doClientConnection();
                }
            }, 3000);
            return false;
        }
    }

    //MQTT是否连接成功的监听
    private IMqttActionListener iMqttActionListener = new IMqttActionListener() {

        @Override
        public void onSuccess(IMqttToken arg0) {
            Log.i(TAG, "连接成功 :" + arg0.toString());
            try {
                mqttAndroidClient.subscribe(PUBLISH_TOPIC, 2);//订阅主题，参数：主题、服务质量
            } catch (MqttException e) {
                e.printStackTrace();
                Log.e(TAG, "onSuccess: ");
            }
        }

        @Override
        public void onFailure(IMqttToken arg0, Throwable arg1) {
            arg1.printStackTrace();
            Log.i(TAG, "连接失败 " + arg1.getMessage());
            doClientConnection();//连接失败，重连（可关闭服务器进行模拟）
        }
    };

    //订阅主题的回调
    private MqttCallback mqttCallback = new MqttCallback() {

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            Log.i(TAG, "收到消息：messageArrived :topic==" + topic + "," + new String(message.getPayload()));
            //收到消息，这里弹出Toast表示。如果需要更新UI，可以使用广播或者EventBus进行发送
//            Toast.makeText(getApplicationContext(), "messageArrived: " + new String(message.getPayload()), Toast.LENGTH_LONG).show();
            //收到其他客户端的消息后，响应给对方告知消息已到达或者消息有问题等
            response("message arrived");
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken arg0) {
            Log.e(TAG, "deliveryComplete: " + arg0);

        }

        @Override
        public void connectionLost(Throwable arg0) {
            Log.i(TAG, "连接断开 "+arg0.getMessage());
            doClientConnection();//连接断开，重连
        }
    };

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy: " );
        try {
            mqttAndroidClient.disconnect(); //断开连接
        } catch (MqttException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}
