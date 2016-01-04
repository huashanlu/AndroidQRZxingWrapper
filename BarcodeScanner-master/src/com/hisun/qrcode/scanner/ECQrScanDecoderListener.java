package com.hisun.qrcode.scanner;

import android.graphics.Bitmap;
import android.view.SurfaceHolder;

import com.google.zxing.Result;

public interface ECQrScanDecoderListener {

	/**
	 * 
	 * @param result
	 *            扫描成功后返回的result
	 * @param bitmap
	 *            扫描成功的bitmap
	 */
	void onScanFinished(Result result, Bitmap bitmap);

	/**
	 * 扫描超时的回调默认600秒
	 */
	void onScanFailedTimeOut();

	/**
	 * 
	 * @param surfaceHolder
	 *            surfaceview销毁的时候回调
	 */
	void onSurfaceDestroyed(SurfaceHolder surfaceHolder);

	/**
	 * 
	 * @param surfaceHolder
	 *            surfaceview创建的时候回调
	 */
	void onSurfaceCreated(SurfaceHolder surfaceHolder);

	void onSurfaceChanged(SurfaceHolder holder, int format, int width,
			int height);

}
