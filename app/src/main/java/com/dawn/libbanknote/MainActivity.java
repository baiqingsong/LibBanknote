package com.dawn.libbanknote;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.dawn.banknote.BanknoteFactory;
import com.dawn.banknote.BanknoteService;
import com.dawn.banknote.OnBanknoteListener;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Intent banknoteIntent = new Intent(this, BanknoteService.class);
//        startService(banknoteIntent);
        BanknoteFactory.getInstance(this).startService(4);
        BanknoteFactory.getInstance(this).setListener(new OnBanknoteListener() {
            @Override
            public void onReceiverMoney(int money) {
                Log.e("dawn", "接收金额：" + money);
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