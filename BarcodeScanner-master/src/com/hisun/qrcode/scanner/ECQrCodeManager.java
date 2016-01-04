package com.hisun.qrcode.scanner;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidParameterException;
import java.util.Vector;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.result.ResultParser;
import com.google.zxing.common.BitMatrix;
import com.hisun.qrcode.scanner.R;
import com.hisun.qrcode.scanner.camera.CameraManager;
import com.hisun.qrcode.scanner.camera.FrontLightMode;
import com.hisun.qrcode.scanner.common.BitmapUtils;
import com.hisun.qrcode.scanner.view.ViewfinderView;

import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public final class ECQrCodeManager implements SurfaceHolder.Callback {

	private static final String TAG = "ECQrCodeManager";
	private static ECQrCodeManager instance = null;
	private Context context;
	private static boolean hasSurface = false;
	private boolean vibrate = false;
	private static final int IMAGE_HALFWIDTH = 20;

	private ECScannerActivityHandler activityHandler;
	private ViewfinderView viewfinderView;

	private ECQrScanDecoderListener ecRrScanDecoderListener;
	private MyTask myTask;

	protected boolean isVibrate() {
		return vibrate;
	}

	public void setVibrate(boolean vibrate) {
		this.vibrate = vibrate;
	}

	private ECQrDecoderFromBitmapListener decoderFromBitmapListener;

	private SurfaceView surfaceView;

	private CameraManager cameraManager;
	private SurfaceHolder.Callback surCallBack;
	private ECAmbientLightManager lightManager;

	private Result savedResultToShow;
	private FrontLightMode lightMode = FrontLightMode.AUTO;

	protected long scannerTime = 1000 * 60;

	public CameraManager getCameraManager() {
		return cameraManager;
	}

	public long getScannerTime() {
		return scannerTime;
	}

	public void setScannerTime(long scannerTime) {
		this.scannerTime = scannerTime;
	}

	private void setCameraManager(CameraManager cameraManager) {
		this.cameraManager = cameraManager;
	}

	protected ECQrScanDecoderListener getEcRrScanDecoderListener() {

		return ecRrScanDecoderListener;
	}

	public FrontLightMode getLightMode() {
		return lightMode;
	}

	protected void setLightMode(FrontLightMode lightMode) {
		this.lightMode = lightMode;
	}

	protected ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	private void setViewfinderView(ViewfinderView viewfinderView) {
		this.viewfinderView = viewfinderView;
	}

	private ECQrCodeManager(Context context) {
		this.context = context;
	}

	private boolean isHasSurface() {
		return hasSurface;
	}

	private void setHasSurface(boolean hasSurface) {
		this.hasSurface = hasSurface;
	}

	protected ECScannerActivityHandler getActivityHandler() {

		return activityHandler;
	}

	private void setActivityHandler(ECScannerActivityHandler activityHandler) {
		this.activityHandler = activityHandler;
	}

	public static ECQrCodeManager getInstance(Context context) {

		if (instance == null) {

			instance = new ECQrCodeManager(context);

		}

		return instance;

	}

	/**
	 * 
	 * @param ecRrScanDecoderListener
	 *            扫描的监听
	 * @param isVibrate
	 *            扫描成功后是否震动
	 * @param frontLightMode
	 *            闪光灯模式
	 */
	public void initListenetAndResultisVibrate(
			ECQrScanDecoderListener ecRrScanDecoderListener, boolean isVibrate) {
		this.ecRrScanDecoderListener = ecRrScanDecoderListener;
		this.vibrate = isVibrate;

	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		if (surfaceHolder == null) {
			throw new IllegalStateException("No SurfaceHolder provided");
		}

		if (cameraManager != null && cameraManager.isOpen()) {
			Log.w(TAG,
					"initCamera() while already open -- late SurfaceView callback?");
			return;
		}
		try {
			cameraManager.openDriver(surfaceHolder);

			if (activityHandler == null) {
				activityHandler = new ECScannerActivityHandler(context, null,
						null, null, cameraManager);
				setActivityHandler(activityHandler);
			}
			decodeOrStoreSavedBitmap(null, null);
		} catch (Exception ioe) {

		}

	}

	public void startScan(Context context, ViewfinderView view,
			SurfaceView surfaceView) {

		this.cameraManager = new CameraManager(context);
		view.setCameraManager(cameraManager);

		this.viewfinderView = view;
		this.surfaceView = surfaceView;
		this.surCallBack = this;

		if (hasSurface) {
			initCamera(surfaceView.getHolder());
		} else {

			surfaceView.getHolder().setType(
					SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

			surfaceView.getHolder().addCallback(this);
		}

	}

	public void stopScan() {

		if (activityHandler != null) {
			activityHandler.quitSynchronously();
			activityHandler = null;
		}

	}

	public void release() {

		if (activityHandler != null) {
			activityHandler.quitSynchronously();

			activityHandler = null;
		}

		if (cameraManager != null) {
			cameraManager.closeDriver();
		}
		if (!hasSurface) {

			SurfaceHolder surfaceHolder = surfaceView.getHolder();
			surfaceHolder.removeCallback(surCallBack);
		}
		hasSurface = false;

		if (myTask != null) {
			myTask.cancel(true);
			myTask = null;
		}

		// if(surfaceView!=null){
		// surfaceView=null;
		// }
		// if(viewfinderView!=null){
		// viewfinderView=null;
		// }

	}

	/**
	 * 
	 * @param delay重新开始预览图片进行扫描
	 *            delay延迟时间(毫秒)
	 */
	public void restartPreviewAfterDelay(long delay) {
		// TODO Auto-generated method stub

		if (activityHandler != null) {
			activityHandler.sendEmptyMessageDelayed(
					ECGlobalConstance.RESTART_PREVIEW, delay);
		}

		if (viewfinderView != null) {
			viewfinderView.setVisibility(View.VISIBLE);
		}

	}

	private void decodeOrStoreSavedBitmap(Bitmap bitmap, Result result) {
		// Bitmap isn't used yet -- will be used soon
		if (activityHandler == null) {

		} else {
			if (result != null) {
				savedResultToShow = result;
			}
			if (savedResultToShow != null) {
				Message message = Message.obtain(activityHandler,
						ECGlobalConstance.DECODE_SUCCEEDED, savedResultToShow);
				activityHandler.sendMessage(message);
			}
			savedResultToShow = null;
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub

		hasSurface = false;
		if (ecRrScanDecoderListener != null) {
			ecRrScanDecoderListener.onSurfaceChanged(holder, format, width,
					height);
		}

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub

		if (holder == null) {

		}

		if (!hasSurface) {
			hasSurface = true;

			initCamera(holder);
		}
		if (ecRrScanDecoderListener != null) {
			ecRrScanDecoderListener.onSurfaceCreated(holder);
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub

		if (ecRrScanDecoderListener != null) {
			ecRrScanDecoderListener.onSurfaceDestroyed(holder);
		}

	}

	/**
	 * 
	 * @param content
	 *            需要编码的内容
	 * @param width
	 *            编码后生成图片的宽度(像素)
	 * @param height
	 *            编码后生成图片的高度(像素)
	 * @return
	 */
	public Bitmap encoderToQrBitmap(String content, int width, int height) {

		if (TextUtils.isEmpty(content)) {

			throw new IllegalArgumentException(
					"content is empty or null,please check it");

		}

		return createQR(content, width, height);
	}

	private Bitmap createQR(String content, int width, int height) {

		BitMatrix bitMatrix = null;

		try {
			bitMatrix = new MultiFormatWriter().encode(
					new String(content.getBytes(), "ISO-8859-1"),
					BarcodeFormat.QR_CODE, width, height);

			int bitMapWidth = bitMatrix.getWidth();
			int bitMapHeight = bitMatrix.getHeight();

			int[] pixels = new int[bitMapWidth * bitMapHeight];
			for (int y = 0; y < bitMapHeight; y++) {
				for (int x = 0; x < bitMapWidth; x++) {
					if (bitMatrix.get(x, y)) {
						pixels[y * bitMapWidth + x] = 0xff000000;
					}

				}
			}
			Bitmap bitmap = Bitmap.createBitmap(bitMapWidth, bitMapHeight,
					Bitmap.Config.ARGB_8888);
			bitmap.setPixels(pixels, 0, bitMapWidth, 0, 0, bitMapWidth,
					bitMapHeight);
			return bitmap;
		} catch (Exception e) {

			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 
	 * @param color扫描区域四个角的颜色
	 */
	public void setRoundRectColor(int color) {

		if (viewfinderView != null) {
			viewfinderView.setRectColor(color);
			viewfinderView.invalidate();
		}
	}

	/**
	 * 
	 * @param color扫描线的颜色
	 */
	public void setLineColor(int color) {

		if (viewfinderView != null) {
			viewfinderView.setLineColor(color);
			viewfinderView.invalidate();
		}

	}

	/**
	 * 设置摄像头焦点缩小
	 */
	public void setCameraZoomIn() {

		if (cameraManager != null) {
			cameraManager.zoomOut();

		}

	}

	/**
	 * 设置摄像头焦点放大
	 */
	public void setCameraZoomOut() {

		if (cameraManager != null) {
			cameraManager.zoomIn();
			;
		}

	}

	/**
	 * 
	 * @param str
	 *            需要编码的内容
	 * @param resWidth
	 *            编码后生成图片的宽度(像素)
	 * @param resHeight
	 *            编码后生成图片的高度(像素)
	 * @param bitmap
	 *            编码进二维码图片中间的小图片
	 * @return
	 * @throws WriterException
	 */
	public Bitmap encoderToQrBitmapWrapBitmap(String str, int resWidth,
			int resHeight, Bitmap bitmap) throws WriterException {

		if (TextUtils.isEmpty(str)) {

			throw new IllegalArgumentException(
					"content is empty or null,please check it");

		}

		BitMatrix matrix = null;
		Matrix m = new Matrix();
		float sx = (float) 2 * IMAGE_HALFWIDTH / bitmap.getWidth();
		float sy = (float) 2 * IMAGE_HALFWIDTH / bitmap.getHeight();
		m.setScale(sx, sy);
		Bitmap bitmapTmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
				bitmap.getHeight(), m, false);
		try {
			matrix = new MultiFormatWriter().encode(new String(str.getBytes(),
					"ISO-8859-1"), BarcodeFormat.QR_CODE, resWidth, resHeight);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int width = matrix.getWidth();
		int height = matrix.getHeight();

		int halfW = width / 2;
		int halfH = height / 2;

		int[] pixels = new int[width * height];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (x > halfW - IMAGE_HALFWIDTH && x < halfW + IMAGE_HALFWIDTH
						&& y > halfH - IMAGE_HALFWIDTH
						&& y < halfH + IMAGE_HALFWIDTH) {
					pixels[y * width + x] = bitmapTmp.getPixel(x - halfW
							+ IMAGE_HALFWIDTH, y - halfH + IMAGE_HALFWIDTH);
				} else {
					if (matrix.get(x, y)) {
						pixels[y * width + x] = 0xff000000;
					}
				}

			}
		}
		Bitmap finalBitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);

		finalBitmap.setPixels(pixels, 0, width, 0, 0, width, height);

		return finalBitmap;
	}

	private class MyTask extends AsyncTask<Bitmap, Integer, String> {

		@Override
		protected void onPreExecute() {

		}

		@Override
		protected String doInBackground(Bitmap... params) {

			if (decoderFromBitmapListener != null) {

				decoderFromBitmapListener.onDecoderDoInBackGround();
			}

			ECBitmapDecoder bitmapDecoder = new ECBitmapDecoder(context);

			Result result = bitmapDecoder.getRawResult(params[0]);

			if (result != null) {

				return ResultParser.parseResult(result).toString();
			}

			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... progresses) {

		}

		@Override
		protected void onPostExecute(String result) {

			if (decoderFromBitmapListener != null) {
				if (result != null) {

					decoderFromBitmapListener.onDecoderSucceed(result);
				} else {

					decoderFromBitmapListener.onDecoderFailed();
				}
			}

		}

		@Override
		protected void onCancelled() {

		}
	}

	public String syncDecoderFromBitmap(Bitmap bitmap) {

		if (bitmap == null) {
			throw new InvalidParameterException();
		}
		ECBitmapDecoder bitmapDecoder = new ECBitmapDecoder(context);

		Result result = bitmapDecoder.getRawResult(bitmap);

		return ResultParser.parseResult(result).toString();

	}

	/**
	 * 
	 * @param bitmap
	 *            需要解码的bitmap
	 * @param decoderBitmapListener
	 *            解码过程的监听器
	 */
	public void asyncDecoderFromBitap(Bitmap bitmap,
			ECQrDecoderFromBitmapListener decoderBitmapListener) {

		if (bitmap == null || decoderBitmapListener == null) {

			throw new InvalidParameterException();

		}

		myTask = null;
		this.decoderFromBitmapListener = decoderBitmapListener;
		myTask = new MyTask();
		myTask.execute(bitmap);
		if (decoderFromBitmapListener != null) {
			decoderFromBitmapListener.onDecoderStarted();
		}

	}

}
