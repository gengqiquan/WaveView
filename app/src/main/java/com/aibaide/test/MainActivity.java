package com.aibaide.test;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.aibaide.waveview.WaveView;

public class MainActivity extends AppCompatActivity {
    private int mBorderColor = Color.parseColor("#44FFFFFF");
    private int mBorderWidth = 10;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WaveView waveView= (WaveView) findViewById(R.id.wave);
        waveView.setFlowNum(50);

      }
}
