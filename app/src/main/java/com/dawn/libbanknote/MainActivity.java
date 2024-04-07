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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Intent banknoteIntent = new Intent(this, BanknoteService.class);
//        startService(banknoteIntent);
//        BanknoteFactory.getInstance(this).startService(0);
//        BanknoteFactory.getInstance(this).setListener(new OnBanknoteListener() {
//            @Override
//            public void onReceiverMoney(int money) {
//                Log.e("dawn", "接收金额：" + money);
//            }
//        });

        BanknoteIctFactory.getInstance(this).startService(0);
        BanknoteIctFactory.getInstance(this).setListener(new OnBanknoteIctListener() {
            @Override
            public void onReceiverMoney(int moneyIndex) {
                Log.e("dawn", "接收金额：" + moneyIndex);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BanknoteFactory.getInstance(this).stopMoneyReceiver();
    }

    public void startReceiver(View view){
        BanknoteFactory.getInstance(this).startMoneyReceiver();
    }

    public void stopReceiver(View view){
        BanknoteFactory.getInstance(this).stopMoneyReceiver();
    }
}