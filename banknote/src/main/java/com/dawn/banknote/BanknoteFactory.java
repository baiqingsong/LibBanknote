package com.dawn.banknote;

import android.content.Context;
import android.content.Intent;

public class BanknoteFactory {
    private Context mContext;
    //单例模式
    private static BanknoteFactory instance;
    private BanknoteFactory(Context context){
        this.mContext = context;
    }
    public static BanknoteFactory getInstance(Context context){
        if(instance == null)
            instance = new BanknoteFactory(context);
        return instance;
    }

    private OnBanknoteListener mListener;

    /**
     * 设置纸币监听
     */
    public void setListener(OnBanknoteListener listener){
        this.mListener = listener;
    }

    /**
     * 获取纸币监听
     */
    public OnBanknoteListener getListener(){
        return mListener;
    }

    /**
     * 开启服务
     */
    public void startService(){
        Intent banknoteIntent = new Intent(mContext, BanknoteService.class);
        mContext.startService(banknoteIntent);
    }

    /**
     * 开始纸币接收
     */
    public void startMoneyReceiver(){
        Intent intent = new Intent(BanknoteConstant.RECEIVER_BANKNOTE);
        intent.putExtra("command", "start_money_receiver");
        mContext.sendBroadcast(intent);
    }

    /**
     * 停止纸币接收
     */
    public void stopMoneyReceiver(){
        Intent intent = new Intent(BanknoteConstant.RECEIVER_BANKNOTE);
        intent.putExtra("command", "stop_money_receiver");
        mContext.sendBroadcast(intent);
    }

}
