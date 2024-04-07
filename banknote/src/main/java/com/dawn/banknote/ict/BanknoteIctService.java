package com.dawn.banknote.ict;

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

import com.dawn.banknote.BanknoteConstant;
import com.dawn.serial.LSerialUtil;

public class BanknoteIctService extends Service {
    private LSerialUtil banknoteSerialUtil;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private BanknoteReceiverIct mReceiver;

    /**
     * 注册广播
     */
    private void registerReceiver() {
        mReceiver = new BanknoteReceiverIct();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BanknoteConstant.RECEIVER_BANKNOTE);
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver();
        startPort();
        Log.i("dawn", "banknote ict service on create");
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
        banknoteSerialUtil = new LSerialUtil(LSerialUtil.SerialNameType.TYPE_TTYS_WK, 1, 9600, 8, 2, 'N', LSerialUtil.SerialType.TYPE_HEX, new LSerialUtil.OnSerialListener() {
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
    }

    /**
     * 解析纸钞机接收字符串
     * @param str 接收字符串
     */
    private void resoluteBanknoteReceiverStr(String str){
        if(TextUtils.isEmpty(str))
            return;
        str = str.trim();
        if("808F".equals(str)){
            //开机启动，发送02，接收指令
            banknoteSerialUtil.sendHexMsg(toByteArray("02"));//发送开机回复
        }else if("8140".equals(str)){
            //通道1接收纸钞，02接收，0F拒收
            banknoteSerialUtil.sendHexMsg(toByteArray("02"));//发送接收回复
            if(BanknoteIctFactory.getInstance(this).getListener() != null)
                BanknoteIctFactory.getInstance(this).getListener().onReceiverMoney(1);
        }else if("8141".equals(str)) {
            //通道2接收纸钞，02接收，0F拒收
            banknoteSerialUtil.sendHexMsg(toByteArray("02"));//发送接收回复
            if(BanknoteIctFactory.getInstance(this).getListener() != null)
                BanknoteIctFactory.getInstance(this).getListener().onReceiverMoney(2);
        }else if("8142".equals(str)) {
            //通道3接收纸钞，02接收，0F拒收
            banknoteSerialUtil.sendHexMsg(toByteArray("02"));//发送接收回复
            if (BanknoteIctFactory.getInstance(this).getListener() != null)
                BanknoteIctFactory.getInstance(this).getListener().onReceiverMoney(3);
        }else if("8143".equals(str)) {
            //通道4接收纸钞，02接收，0F拒收
            banknoteSerialUtil.sendHexMsg(toByteArray("02"));//发送接收回复
            if(BanknoteIctFactory.getInstance(this).getListener() != null)
                BanknoteIctFactory.getInstance(this).getListener().onReceiverMoney(4);
        }else if("8144".equals(str)) {
            //通道5接收纸钞，02接收，0F拒收
            banknoteSerialUtil.sendHexMsg(toByteArray("02"));//发送接收回复
            if(BanknoteIctFactory.getInstance(this).getListener() != null)
                BanknoteIctFactory.getInstance(this).getListener().onReceiverMoney(5);
        }else if("3E".equals(str)){
            //开始接收纸钞

        }else if("5E".equals(str)) {
            //停止接收纸钞
        }

    }

    /**
     * 开始接收纸钞
     */
    private void startMoneyReceiver(){
        banknoteSerialUtil.sendHexMsg(toByteArray("3E"));
    }

    /**
     * 停滞接收纸钞
     */
    private void stopMoneyReceiver(){
        banknoteSerialUtil.sendHexMsg(toByteArray("5E"));
    }

    /**
     * 重置纸钞机
     */
    private void resetBanknote(){
        banknoteSerialUtil.sendHexMsg(toByteArray("30"));
    }

    /**
     * 获取纸钞机状态
     */
    private void getBanknoteStatus(){
        banknoteSerialUtil.sendHexMsg(toByteArray("0C"));
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
    private class BanknoteReceiverIct extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent == null)
                return;
            String command = intent.getStringExtra("command");
            if(TextUtils.isEmpty(command))
                return;
            switch (command){
                case "start_money_receiver"://开始纸币接收
                    startMoneyReceiver();
                    break;
                case "stop_money_receiver"://停止纸币接收
                    stopMoneyReceiver();
                    break;
            }
        }
    }
}

