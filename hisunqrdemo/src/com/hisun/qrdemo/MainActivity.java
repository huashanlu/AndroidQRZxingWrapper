package com.hisun.qrdemo;



import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.qr_main_layout);
	}
	
	
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.bu_gotodecoder:

			startActivity(new Intent(this, DecoderQRActivity.class));

			break;

		case R.id.bu_gotoencoder:

			startActivity(new Intent(this, EncoderQRActivity.class));

			break;
			
			
		case R.id.bu_decoderfromfile:
			
			startActivity(new Intent(this,DecoderFromBitmapActivity.class));
			
			break;

		
		default:
			break;
		}

	}

}
