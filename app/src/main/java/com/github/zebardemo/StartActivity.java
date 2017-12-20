package com.github.zebardemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.github.zebardemo.util.ZebarUtil;


public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
    }
    /*
    * 开始
    * */
    public void start(View view) {
        ZebarUtil.startScan(getBaseContext());
    }

    /*
    * 结束
    * */
    public void end(View view) {
     ZebarUtil.endScan();
    }

}
