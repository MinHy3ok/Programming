
package com.example.exsm9m2cds2;

import com.example.jnidriver.JNIDriver;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile; 

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity { 

	ReceiveThread mSegThread;
	boolean mThreadRun = true;
	
	JNIDriver mDriver = new JNIDriver();
	
	byte[] data1 = {1,0,0,0,0,0,0,0};
	byte[] data2 = {0,1,0,0,0,0,0,0};
	byte[] data3 = {0,0,0,0,0,0,0,0};
	
	boolean stop_flg = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Button btn1 = (Button) findViewById(R.id.button1);
		btn1.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				mSegThread = new ReceiveThread();
				mSegThread.start();
			}
		});
		
		Button btn2 = (Button) findViewById(R.id.button2);
		btn2.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				stop_flg = true;
			}
		});
	}

	private class ReceiveThread extends Thread {
		@Override
		public void run() {
			super.run();
			while (mThreadRun) {

				Message text = Message.obtain();

				handler.sendMessage(text);

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public Handler handler = new Handler() {

		public void handleMessage(Message msg) {
			TextView tv;
			FileReader in;
			int in_cda;
			try {
				in = new FileReader(
						"/sys/devices/12d10000.adc/iio:device0/in_voltage3_raw");
				BufferedReader br = new BufferedReader(in);
				String data = br.readLine();
				tv = (TextView) findViewById(R.id.textView1);
				
				in_cda = Integer.parseInt(data);
				
				if (stop_flg == true) {
					tv.setText("CDS :");
					mDriver.write(data3);
					mThreadRun = false;
				}
				else if (in_cda < 3000) {
					tv.setText("CDS :" + data + " ( Street0 - ON )");
					mDriver.write(data1);
				}
				else if (in_cda >= 3000) {
					tv.setText("CDS :" + data + " ( Street1 - ON )");
					mDriver.write(data2);
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	};
	@Override
	protected void onPause() {
		mDriver.close();
		super.onPause();
	}
	@Override
	protected void onResume() {
		if(mDriver.open("/dev/sm9s5422_led")<0){
			Toast.makeText(MainActivity.this, "LED_Driver Open Failed", Toast.LENGTH_SHORT).show();
		}
		super.onResume();
	}
}