package com.dawn.banknote;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.dawn.serial.LSerialUtil;

/**
 * 纸钞机服务
 */
public class BanknoteService extends Service {
    private LSerialUtil banknoteSerialUtil;
    private enum command {init, getProtocol, getConfigure, setConfigure, enable, disable, status};//初始化，获取协议，获取配置，设置配置，使能，禁能，状态
    private command currentCommand;
    private int multiple  = 1;//金额倍数
    private int[] denominationAry = new int[4];//保存通道的金额
    private final static int h_init = 0x101;//检查连接，初始化
    private final static int h_get_protocol = 0x102;//获取协议
    private final static int h_get_configure = 0x103;//获取配置
    private final static int h_set_configure = 0x104;//设置配置
    private final static int h_enable = 0x105;//使能
    private final static int h_disable = 0x106;//禁能
    private final static int h_status = 0x107;//状态
    private final static int d_delay = 200;//信息发送间隔时间
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case h_init:
                    cycleCheckConnect();//循环发送是否连接
                    break;
                case h_get_protocol:
                    getProtocol();
                    break;
                case h_get_configure:
                    getConfigure();
                    break;
                case h_set_configure:
                    setConfigure();
                    break;
                case h_enable://设置使能状态
                    setEnable();
                    break;
                case h_disable://设置禁能状态
                    setDisable();
                    break;
                case h_status://循环查询状态
                    cycleStatus();
                    break;
            }
        }
    };
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private BanknoteReceiver mReceiver;

    /**
     * 注册广播
     */
    private void registerReceiver() {
        mReceiver = new BanknoteReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BanknoteConstant.RECEIVER_BANKNOTE);
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver();
        startPort();
        Log.i("dawn", "banknote service on create");
    }

    private int autoMultiple = 0;//设置自定义倍数
    private int autoConfigure = 0;//设置多少币种

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mReceiver != null)
            unregisterReceiver(mReceiver);
    }

    /**
     * 纸钞机串口连接
     */
    private void startPort(){
        banknoteSerialUtil = new LSerialUtil(LSerialUtil.SerialNameType.TYPE_TTYS_WK, BanknoteConstant.BANKNOTE_SERIAL, 9600, 8, 2, 'N', LSerialUtil.SerialType.TYPE_HEX, new LSerialUtil.OnSerialListener() {
            @Override
            public void startError() {
                Log.e("dawn", "异常：纸钞机串口启动错误");
            }

            @Override
            public void receiverError() {
                Log.e("dawn", "异常：纸钞机接收错误");
            }

            @Override
            public void sendError() {
                Log.e("dawn", "异常：纸钞机发送错误");
            }

            @Override
            public void getReceiverStr(String str) {
                Log.i("dawn", "纸钞机串口接收数据：" + str);
                resoluteBanknoteReceiverStr(str);
            }
        });
        cycleCheckConnect();
    }

    /**
     * 解析纸钞机接收字符串
     * @param str 接收字符串
     */
    private void resoluteBanknoteReceiverStr(String str){
        if(TextUtils.isEmpty(str))
            return;
        str = str.trim();
//        Log.i("dawn", " banknote result str " + str);
        if(!str.startsWith("7F"))//7F开头
            return;
        if(str.length() < 8)
            return;
        String result = str.substring(6, 8);
        if("F0".equals(result)){//成功
//            LLog.i("banknote result str " + str);
            switch (currentCommand){
                case init://初始化成功
                    mHandler.removeMessages(h_init);
                    getConfigure();
                    break;
                case getProtocol://获取协议
                    mHandler.removeMessages(h_get_protocol);
                    getConfigure();
                    break;
                case getConfigure://获取配置
                    mHandler.removeMessages(h_get_configure);
                    multiple = Integer.parseInt(str.substring(28, 30), 16);//获取倍数
                    if(autoMultiple != 0)
                        multiple = autoMultiple;
                    int passageway = Integer.parseInt(str.substring(30, 32), 16);//通道个数
                    denominationAry = new int[passageway];
                    if(passageway > 0){
                        for(int i = 0; i < passageway; i ++){
                            int denomination = Integer.parseInt(str.substring(32 + i * 2, 34 + i * 2), 16);
                            Log.i("dawn", "通道" + (i+1) + "面额：" + (denomination * multiple));
                            denominationAry[i] = denomination;
                        }
                        setConfigure();//设置配置
                    }

                    break;
                case setConfigure://设置配置
                    mHandler.removeMessages(h_set_configure);
                    break;
                case enable://使能状态
                    Log.i("dawn", "receiver enable");
                    mHandler.removeMessages(h_enable);
                    cycleStatus();
                    break;
                case disable://禁能
                    Log.i("dawn", "receiver disable");
                    mHandler.removeMessages(h_disable);
                    mHandler.removeMessages(h_status);
                    break;
                case status://状态获取
                    mHandler.removeMessages(h_status);
                    String statusStr = str.substring(8, 10);
                    if("EF".equals(statusStr)){

                    }else if("EE".equals(statusStr)){
                        int denomination = Integer.parseInt(str.substring(10, 12), 16);//收到通道金额
                        Log.e("dawn", "接收纸币金额：" + (denominationAry[denomination - 1] * multiple) + "元");
                        if(BanknoteFactory.getInstance(BanknoteService.this).getListener() != null)
                            BanknoteFactory.getInstance(BanknoteService.this).getListener().onReceiverMoney(denominationAry[denomination - 1] * multiple);
                    }
                    cycleStatus();
                    break;
            }
        }
    }

    /**
     * 循环查询连接，初始化
     */
    private void cycleCheckConnect(){
        currentCommand = command.init;//初始化状态
        String sendData = BanknoteSerialUtil.getBanknoteConnect();
//        LLog.i("cycle check connect data : " + sendData);
        banknoteSerialUtil.sendHexMsg(toByteArray(sendData));//发送检查连接
        mHandler.removeMessages(h_init);
        mHandler.sendEmptyMessageDelayed(h_init, d_delay);//延迟发送
    }

    /**
     * 获取协议
     */
    private void getProtocol(){
        currentCommand = command.getProtocol;
        String sendData = BanknoteSerialUtil.getProtocol();
        Log.i("dawn", "get protocol data : " + sendData);
        banknoteSerialUtil.sendHexMsg(toByteArray(sendData));//发送协议
        mHandler.removeMessages(h_get_protocol);
        mHandler.sendEmptyMessageDelayed(h_get_protocol, d_delay);
    }

    /**
     * 获取配置
     */
    private void getConfigure(){
        currentCommand = command.getConfigure;
        String sendData = BanknoteSerialUtil.getConfigure();
        Log.i("dawn", "get configure data : " + sendData);
        banknoteSerialUtil.sendHexMsg(toByteArray(sendData));//发送获取配置
        mHandler.removeMessages(h_get_configure);
        mHandler.sendEmptyMessageDelayed(h_get_configure, d_delay);
    }

    /**
     * 发送设置配置指令
     */
    private void setConfigure(){
        currentCommand = command.setConfigure;
        String sendData = "";
        if(autoConfigure == 1){
            sendData = BanknoteSerialUtil.setConfigure1();
        }else if(autoConfigure == 2){
            sendData = BanknoteSerialUtil.setConfigure2();
        }else{
            sendData = BanknoteSerialUtil.setConfigure();
        }

        Log.i("dawn", "set configure data : " + sendData);
        banknoteSerialUtil.sendHexMsg(toByteArray(sendData));//发送设置配置
        mHandler.removeMessages(h_set_configure);
        mHandler.sendEmptyMessageDelayed(h_set_configure, d_delay);
    }

    /**
     * 设置使能状态
     */
    private void setEnable(){
        Log.i("dawn", "set enable");
        currentCommand = command.enable;//使能状态
        String sendData = BanknoteSerialUtil.setEnable();
        Log.i("dawn", "set enable data : " + sendData);
        banknoteSerialUtil.sendHexMsg(toByteArray(sendData));
        mHandler.removeMessages(h_enable);
        mHandler.sendEmptyMessageDelayed(h_enable, d_delay);
    }

    /**
     * 设置禁能状态
     */
    private void setDisable(){
        Log.i("dawn", "set disable");
        currentCommand = command.disable;//禁能状态
        String sendData = BanknoteSerialUtil.setDisable();
        Log.i("dawn", "set disable data : " + sendData);
        banknoteSerialUtil.sendHexMsg(toByteArray(sendData));
        mHandler.removeMessages(h_disable);
        mHandler.sendEmptyMessageDelayed(h_disable, d_delay);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String sendData = BanknoteSerialUtil.setDisable();
                Log.i("dawn", "set disable data : " + sendData);
                banknoteSerialUtil.sendHexMsg(toByteArray(sendData));
            }
        }, 500);
    }

    /**
     * 循环查询状态
     */
    private void cycleStatus(){
        currentCommand = command.status;//查询状态
        String sendData = BanknoteSerialUtil.getStatus();
//        LLog.i("cycle status data : " + sendData);
        banknoteSerialUtil.sendHexMsg(toByteArray(sendData));
        mHandler.removeMessages(h_status);
        mHandler.sendEmptyMessageDelayed(h_status, d_delay);
    }

    /**
     * 字符串转为字节数组
     */
    public static byte[] toByteArray(String hexString) {
        if (hexString == null)
            throw new IllegalArgumentException("this hexString must not be empty");

        hexString = hexString.toUpperCase();
        final byte[] byteArray = new byte[hexString.length() / 2];
        int k = 0;
        for (int i = 0; i < byteArray.length; i++) {
            // 因为是16进制，最多只会占用4位，转换成字节需要两个16进制的字符，高位在先
            byte high = (byte) (Character.digit(hexString.charAt(k), 16) & 0xFF);
            byte low = (byte) (Character.digit(hexString.charAt(k + 1), 16) & 0xFF);
            byteArray[i] = (byte) (high << 4 | low & 0xFF);
            k += 2;
        }
        return byteArray;
    }

    /**
     * 纸钞机广播
     */
    private class BanknoteReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent == null)
                return;
            String command = intent.getStringExtra("command");
            if(TextUtils.isEmpty(command))
                return;
            switch (command){
                case "start_money_receiver"://开始纸币接收
                    setEnable();
                    break;
                case "stop_money_receiver"://停止纸币接收
                    setDisable();
                    break;
                case "set_configure"://设置相关参数
                    autoConfigure = intent.getIntExtra("configure", 0);
                    autoMultiple = intent.getIntExtra("multiple", 0);
                    setConfigure();
                    break;
            }
        }
    }
}
