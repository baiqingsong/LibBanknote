package com.dawn.banknote.ict;

import android.content.Context;
import android.content.Intent;

import com.dawn.banknote.BanknoteConstant;
import com.dawn.banknote.BanknoteService;

public class BanknoteIctFactory {
    private Context mContext;
    //单例模式
    private static BanknoteIctFactory instance;
    private BanknoteIctFactory(Context context){
        this.mContext = context;
    }
    public static BanknoteIctFactory getInstance(Context context){
        if(instance == null)
            instance = new BanknoteIctFactory(context);
        return instance;
    }

    private OnBanknoteIctListener mListener;

    /**
     * 设置纸币监听
     */
    public void setListener(OnBanknoteIctListener listener){
        this.mListener = listener;
    }

    /**
     * 获取纸币监听
     */
    public OnBanknoteIctListener getListener(){
        return mListener;
    }

    /**
     * 开启服务
     */
    public void startService(int serial){
        BanknoteConstant.BANKNOTE_SERIAL = serial;
        Intent banknoteIntent = new Intent(mContext, BanknoteIctService.class);
        mContext.startService(banknoteIntent);
    }

    /**
     * 开始纸币接收
     */
    public void startMoneyReceiver(){
        Intent intent = new Intent(BanknoteConstant.RECEIVER_BANKNOTE_ICT);
        intent.putExtra("command", "start_money_receiver");
        mContext.sendBroadcast(intent);
    }

    /**
     * 停止纸币接收
     */
    public void stopMoneyReceiver(){
        Intent intent = new Intent(BanknoteConstant.RECEIVER_BANKNOTE_ICT);
        intent.putExtra("command", "stop_money_receiver");
        mContext.sendBroadcast(intent);
    }
}
