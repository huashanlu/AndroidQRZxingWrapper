package com.hisun.qrdemo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.hisun.qrcode.scanner.ECQrCodeManager;
import com.hisun.qrcode.scanner.ECQrDecoderFromBitmapListener;



public class DecoderFromBitmapActivity extends Activity implements
		ECQrDecoderFromBitmapListener {

	private static final String TAG = "DecoderFromBitmapActivity";

	private ECQrCodeManager ecQrCodeManager;

	private TextView tvResult;

	private Bitmap bitmap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.qr_decoder_frombitmap_layout);

		tvResult = (TextView) findViewById(R.id.tv_result);

		bitmap = ((BitmapDrawable) getResources()
				.getDrawable(R.drawable.qrcode)).getBitmap();

		ecQrCodeManager = ECQrCodeManager.getInstance(this);

	}

	public void onClick(View v) {

		switch (v.getId()) {

		case R.id.bu_decode_bitmap:

			ecQrCodeManager.asyncDecoderFromBitap(bitmap, this);
			
			

			break;

		default:
			break;
		}
	}

	@Override
	public void onDecoderFailed() {
		// TODO Auto-generated method stub

		Log.e(TAG, "decoder failed");

	}

	@Override
	public void onDecoderSucceed(String result) {
		// TODO Auto-generated method stub

		Log.e(TAG, "decoder successed");
		Log.e(TAG, result);
		tvResult.setText(result);

	}

	@Override
	public void onDecoderStarted() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDecoderDoInBackGround() {
		// TODO Auto-generated method stub

	}

}
