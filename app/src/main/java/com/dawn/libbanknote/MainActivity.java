package com.dawn.libbanknote;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.dawn.banknote.BanknoteFactory;
import com.dawn.banknote.BanknoteService;
import com.dawn.banknote.OnBanknoteListener;
import com.dawn.banknote.ict.BanknoteIctFactory;
import com.dawn.banknote.ict.OnBanknoteIctListener;

public class MainActivity extends AppCompatActivity {
    private boolean isIct = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Intent banknoteIntent = new Intent(this, BanknoteService.class);
//        startService(banknoteIntent);
        if(isIct){
            BanknoteIctFactory.getInstance(this).startService(4);
            BanknoteIctFactory.getInstance(this).setListener(new OnBanknoteIctListener() {
                @Override
                public void onReceiverMoney(int moneyIndex) {
                    Log.e("dawn", "接收金额：" + moneyIndex);
                }
            });
        }else{
            BanknoteFactory.getInstance(this).startService(1);
            BanknoteFactory.getInstance(this).setListener(new OnBanknoteListener() {
                @Override
                public void onReceiverMoney(int money) {
                    Log.e("dawn", "接收金额：" + money);
                }
            });
        }




    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(isIct){
            BanknoteIctFactory.getInstance(this).stopMoneyReceiver();
        }else{
            BanknoteFactory.getInstance(this).stopMoneyReceiver();
        }
    }

    public void startReceiver(View view){
        if(isIct){
            BanknoteIctFactory.getInstance(this).startMoneyReceiver();
        }else{
            BanknoteFactory.getInstance(this).startMoneyReceiver();
        }
    }

    public void stopReceiver(View view){
        if(isIct){
            BanknoteIctFactory.getInstance(this).stopMoneyReceiver();
        }else{
            BanknoteFactory.getInstance(this).stopMoneyReceiver();
        }
    }
}