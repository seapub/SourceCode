package com.manning.androidhacks.hack002;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewStub;
import android.view.ViewStub.OnInflateListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Toast;

public class MainActivity extends Activity {
	private Button btn1, btn2,btn3;
	private ViewStub viewStub;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		//��ȡ�ؼ������¼�
		btn1 = (Button) findViewById(R.id.btn1);
		btn2 = (Button) findViewById(R.id.btn2);
		btn3 = (Button) findViewById(R.id.btn3);
		viewStub = (ViewStub) findViewById(R.id.stub);
		viewStub.setOnInflateListener(inflateListener);
		btn1.setOnClickListener(click);
		btn2.setOnClickListener(click);
		btn3.setOnClickListener(click);
	}

	private OnInflateListener inflateListener=new OnInflateListener() {
		
		@Override
		public void onInflate(ViewStub stub, View inflated) {
			// inflaye ViewStub��ʱ����ʾ
			Toast.makeText(MainActivity.this, "ViewStub is loaded!", Toast.LENGTH_SHORT).show();
		}
	};
	
	private View.OnClickListener click = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn1:
				try {
					//���û�б�inflate����ʹ��inflate����
					LinearLayout layout=(LinearLayout)viewStub.inflate();
					RatingBar bar=(RatingBar)layout.findViewById(R.id.ratingBar1);
					bar.setNumStars(4);
				} catch (Exception e) {
					//���ʹ��inflate���ͱ�����˵���Ѿ������͹��ˣ�ʹ��setVisibility������ʾ
					viewStub.setVisibility(View.VISIBLE);
				}				
				break;

			case R.id.btn2:
				//����ViewStub
				viewStub.setVisibility(View.GONE);
				break;
			case R.id.btn3:
				//������inflate�Ŀؼ�����Ҫ�õ���ǰ���ֵĶ���
				//Ȼ��ͨ���������ȥ�ҵ���inflate�Ŀؼ���
				//��Ϊ���������ʾ���У����ҵ�include��ǩ����Ŀؼ�
				LinearLayout linearLayout=(LinearLayout)findViewById(R.id.inflatedStart);
				RatingBar rBar=(RatingBar)linearLayout.findViewById(R.id.ratingBar1);
				float numStart=rBar.getRating();
				numStart++;
				if(numStart>4)
				{
					numStart=0;
				}
				rBar.setRating(numStart);
				break;
			}

		}
	};
}
