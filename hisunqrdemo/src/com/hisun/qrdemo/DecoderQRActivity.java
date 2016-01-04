package com.hisun.qrdemo;

import java.util.Timer;
import java.util.TimerTask;

import com.google.zxing.Result;
import com.hisun.qrcode.scanner.ECQrCodeManager;
import com.hisun.qrcode.scanner.ECQrScanDecoderListener;
import com.hisun.qrcode.scanner.camera.FrontLightMode;
import com.hisun.qrcode.scanner.view.ViewfinderView;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

public class DecoderQRActivity extends Activity implements
		ECQrScanDecoderListener {

	private static final String TAG = "DecoderQRActivity";
	private ECQrCodeManager ecQrCodeManager;
	private SurfaceView surfaceView;
	private ViewfinderView viewfinderView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.qr_decoder_layout);
		ecQrCodeManager = ECQrCodeManager.getInstance(this);

		surfaceView = (SurfaceView) findViewById(R.id.sv_preview_view);
		viewfinderView = (ViewfinderView) findViewById(R.id.qr_viewfinder_view);

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {

		case KeyEvent.KEYCODE_VOLUME_UP:
			ecQrCodeManager.setCameraZoomOut();
			;
			return true;

		case KeyEvent.KEYCODE_VOLUME_DOWN:
			ecQrCodeManager.setCameraZoomIn();
			return true;

		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub

		super.onResume();
		ecQrCodeManager.initListenetAndResultisVibrate(this, true);
		ecQrCodeManager.startScan(DecoderQRActivity.this, viewfinderView,
				surfaceView);


	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		ecQrCodeManager.release();
	}

	@Override
	public void onScanFinished(Result result, Bitmap bitmap) {
		// TODO Auto-generated method stub

		Toast.makeText(this, result.getText(), 0).show();
		ecQrCodeManager.restartPreviewAfterDelay(1000);

	}

	@Override
	public void onSurfaceDestroyed(SurfaceHolder surfaceHolder) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSurfaceCreated(SurfaceHolder surfaceHolder) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSurfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScanFailedTimeOut() {
		// TODO Auto-generated method stub

		Log.e(TAG, "ɨ�賬ʱ");
		ecQrCodeManager.restartPreviewAfterDelay(2000);

	}

}
