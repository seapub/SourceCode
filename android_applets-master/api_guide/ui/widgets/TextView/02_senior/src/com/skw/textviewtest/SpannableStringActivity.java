package com.skw.textviewtest;

import android.app.Activity;  
import android.graphics.Color;  
import android.graphics.Typeface;  
import android.graphics.drawable.Drawable;  
import android.os.Bundle;  
import android.text.method.LinkMovementMethod;
import android.text.Spannable;  
import android.text.SpannableString;  
import android.text.style.AbsoluteSizeSpan;  
import android.text.style.BackgroundColorSpan;  
import android.text.style.ForegroundColorSpan;  
import android.text.style.ImageSpan;  
import android.text.style.SubscriptSpan;
import android.text.style.SuperscriptSpan;
import android.text.style.StrikethroughSpan;  
import android.text.style.StyleSpan;  
import android.text.style.URLSpan;  
import android.text.style.UnderlineSpan;  
import android.view.View;  
import android.view.View.OnClickListener;  
import android.widget.Button;  
import android.widget.TextView;  
  
/**
 * TextView和SpannableString结合，显示各种特效
 *
 * @desc 例如，TextView中文字的上标、下标、超链接等等
 */
public class SpannableStringActivity extends Activity implements  OnClickListener {  
    private TextView mTextView;  

    @Override  
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.activity_spannable);  
      
        mTextView = (TextView) findViewById(R.id.tv);  
        findViewById(R.id.underline_btn).setOnClickListener(this);
        findViewById(R.id.strike_btn).setOnClickListener(this);
        findViewById(R.id.style_btn).setOnClickListener(this);
        findViewById(R.id.font_btn).setOnClickListener(this);
        findViewById(R.id.color_btn1).setOnClickListener(this);
        findViewById(R.id.color_btn2).setOnClickListener(this);
        findViewById(R.id.url_btn).setOnClickListener(this);
        findViewById(R.id.image_btn).setOnClickListener(this);
        findViewById(R.id.subscript).setOnClickListener(this);
        findViewById(R.id.superscript).setOnClickListener(this);
    }  

    @Override  
    public void onClick(View v) {
        switch (v.getId()) {  
        case R.id.underline_btn:  
            addUnderLineSpan();  
            break;  
        case R.id.strike_btn:  
            addStrikeSpan();  
            break;  
        case R.id.style_btn:  
            addStyleSpan();  
            break;  
        case R.id.font_btn:  
            addFontSpan();  
            break;  
        case R.id.color_btn1:  
            addForeColorSpan();  
            break;  
        case R.id.color_btn2:  
            addBackColorSpan();  
            break;  
        case R.id.url_btn:  
            addUrlSpan();  
            break;  
        case R.id.image_btn:  
            addImageSpan();  
            break;  
        case R.id.subscript:
            addSubscript();
            break;  
        case R.id.superscript:  
            addSuperscript();
            break;  
        }
    }

    /** 
    * 超链接 
    */  
    private void addUrlSpan() {  
        SpannableString spanString = new SpannableString("超链接");  
        URLSpan span = new URLSpan("http://www.baidu.com");
        spanString.setSpan(span, 0, spanString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);  
        mTextView.append(spanString);  
        mTextView.setMovementMethod(new LinkMovementMethod());
    }  
      
    /** 
    * 文字背景颜色 
    */  
    private void addBackColorSpan() {  
        SpannableString spanString = new SpannableString("颜色2");  
        BackgroundColorSpan span = new BackgroundColorSpan(Color.YELLOW);  
        spanString.setSpan(span, 0, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);  
        mTextView.append(spanString);  
    }  
      
    /** 
    * 文字颜色 
    */  
    private void addForeColorSpan() {  
        SpannableString spanString = new SpannableString("颜色1");  
        ForegroundColorSpan span = new ForegroundColorSpan(Color.BLUE);  
        spanString.setSpan(span, 0, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);  
        mTextView.append(spanString);  
    }  
      
    /** 
    * 字体大小 
    */  
    private void addFontSpan() {  
        SpannableString spanString = new SpannableString("36号字体");  
        AbsoluteSizeSpan span = new AbsoluteSizeSpan(36);  
        spanString.setSpan(span, 0, 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);  
        mTextView.append(spanString);  
    }  
      
    /** 
    * 粗体，斜体 
    */  
    private void addStyleSpan() {  
        SpannableString spanString = new SpannableString("BIBI");  
        StyleSpan span = new StyleSpan(Typeface.BOLD_ITALIC);  
        spanString.setSpan(span, 0, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);  
        mTextView.append(spanString);  
    }  
      
    /** 
    * 删除线 
    */  
    private void addStrikeSpan() {  
        SpannableString spanString = new SpannableString("删除线");  
        StrikethroughSpan span = new StrikethroughSpan();  
        spanString.setSpan(span, 0, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);  
        mTextView.append(spanString);  
    }  
      
    /** 
    * 下划线 
    */  
    private void addUnderLineSpan() {  
        SpannableString spanString = new SpannableString("下划线");  
        UnderlineSpan span = new UnderlineSpan();  
        spanString.setSpan(span, 0, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);  
        mTextView.append(spanString);  
    }  
      
    /** 
    * 图片 
    */  
    private void addImageSpan() {  
        SpannableString spanString = new SpannableString(" ");  
        Drawable d = getResources().getDrawable(R.drawable.ic_launcher);  
        d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());  
        ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);  
        spanString.setSpan(span, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);  
        mTextView.append(spanString);  
    }  

    /**
     * 下标（数学公式会用到）
     */
    private void addSubscript() {
        SpannableString spanText = new SpannableString("x2");
        spanText.setSpan(new SubscriptSpan(), 1, 2, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        mTextView.append(spanText);
    }

    /**
     * 上标
     */
    private void addSuperscript() {
        SpannableString spanText = new SpannableString("B0");
        spanText.setSpan(new SuperscriptSpan(), 1, 2, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        mTextView.append(spanText);
    }
}
