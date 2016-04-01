package com.skw.seekbartest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * SeekBar 测试程序。
 *
 * @desc 该测试程序包含3部分：
 * (01) Demo1：SeekBar的基本用法。
 * (02) Demo2：使用自定义的shape来绘制SeekBar。
 * (03) Demo3：使用图片来绘制SeekBar。
 *
 * @author skywang
 * @e-mail kuiwu-wang@163.com
 */
public class SeekbarTest extends Activity 
    implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        findViewById(R.id.btn_demo1).setOnClickListener(this);
        findViewById(R.id.btn_demo2).setOnClickListener(this);
        findViewById(R.id.btn_demo3).setOnClickListener(this);
    }   
        
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        case R.id.btn_demo1:
            startActivity(new Intent(this, Demo1.class));
            break;
        case R.id.btn_demo2:
            startActivity(new Intent(this, Demo2.class));
            break;
        case R.id.btn_demo3:
            startActivity(new Intent(this, Demo3.class));
            break;
        }
    }   
}
