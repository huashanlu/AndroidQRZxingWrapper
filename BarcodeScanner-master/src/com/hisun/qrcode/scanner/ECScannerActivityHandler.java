/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hisun.qrcode.scanner;

import java.util.Collection;
import java.util.Map;

import android.app.Activity;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.Browser;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.hisun.qrcode.scanner.R;
import com.hisun.qrcode.scanner.camera.CameraManager;
import com.hisun.qrcode.scanner.view.ViewfinderResultPointCallback;

/**
 * This class handles all the messaging which comprises the state machine for
 * capture.
 * 
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class ECScannerActivityHandler extends Handler {

	private static final String TAG = ECScannerActivityHandler.class
			.getSimpleName();

	private final Context activity;

	/**
	 * 真正负责扫描任务的核心线程
	 */
	private final ECDecodeThread decodeThread;

	private State state;

	private final CameraManager cameraManager;

	private long startTime;
	private long endTime;

	/**
	 * 当前扫描的状态
	 */
	private enum State {
		/**
		 * 预览
		 */
		PREVIEW,
		/**
		 * 扫描成功
		 */
		SUCCESS,
		/**
		 * 结束扫描
		 */
		DONE
	}

	public ECScannerActivityHandler(Context activity,
			Collection<BarcodeFormat> decodeFormats,
			Map<DecodeHintType, ?> baseHints, String characterSet,
			CameraManager cameraManager) {
		this.activity = activity;

		// 启动扫描线程
		decodeThread = new ECDecodeThread(activity, decodeFormats, baseHints,
				characterSet, new ViewfinderResultPointCallback(ECQrCodeManager
						.getInstance(activity).getViewfinderView()));
		decodeThread.start();

		startTime = System.currentTimeMillis();

		state = State.SUCCESS;

		// Start ourselves capturing previews and decoding.
		this.cameraManager = cameraManager;

		cameraManager.startPreview();

		restartPreviewAndDecode();
	}

	@Override
	public void handleMessage(Message message) {
		switch (message.what) {
		case ECGlobalConstance.RESTART_PREVIEW: // 准备进行下一次扫描

			restartPreviewAndDecode();
			break;
		case ECGlobalConstance.DECODE_SUCCEEDED:

			state = State.SUCCESS;
			Bundle bundle = message.getData();
			Bitmap barcode = null;
			float scaleFactor = 1.0f;
			if (bundle != null) {
				byte[] compressedBitmap = bundle
						.getByteArray(ECDecodeThread.BARCODE_BITMAP);
				if (compressedBitmap != null) {
					barcode = BitmapFactory.decodeByteArray(compressedBitmap,
							0, compressedBitmap.length, null);
					// Mutable copy:
					barcode = barcode.copy(Bitmap.Config.ARGB_8888, true);
				}
				scaleFactor = bundle
						.getFloat(ECDecodeThread.BARCODE_SCALED_FACTOR);
			}

			Log.e(TAG, ((Result) message.obj).getText());
			// *********************************
			ECQrScanDecoderListener decoderListener = ECQrCodeManager
					.getInstance(activity).getEcRrScanDecoderListener();

			if (decoderListener != null) {

				decoderListener.onScanFinished((Result) message.obj, barcode);
			}

			if (ECQrCodeManager.getInstance(activity).isVibrate()) {

				Vibrator vib = (Vibrator) activity
						.getSystemService(Service.VIBRATOR_SERVICE);
				vib.vibrate(100);

			}

			break;
		case ECGlobalConstance.DECODE_FAILED:

			state = State.PREVIEW;

			endTime = System.currentTimeMillis();
			ECQrScanDecoderListener decoderListenerTwo = ECQrCodeManager
					.getInstance(activity).getEcRrScanDecoderListener();

			if (decoderListenerTwo != null
					&& (endTime - startTime) > ECQrCodeManager.getInstance(
							activity).getScannerTime()) {

				decoderListenerTwo.onScanFailedTimeOut();

				quitSynchronously();

				// Message message2 = new Message();
				// message2.what = ECGlobalConstance.RESTART_PREVIEW;
				// decodeThread.getHandler().sendMessage(message2);
				// startTime = System.currentTimeMillis();
			}

			cameraManager.requestPreviewFrame(decodeThread.getHandler(),
					ECGlobalConstance.DECODE);
			break;
		case ECGlobalConstance.RETURN_SCAN_RESULT:
			// Log.d(TAG, "Got return scan result message");
			// activity.setResult(Activity.RESULT_OK, (Intent) message.obj);
			// activity.finish();
			break;
		case ECGlobalConstance.LAUNCH_PRODUCT_QUERY:

			break;
		}
	}

	public void quitSynchronously() {
		state = State.DONE;
		cameraManager.stopPreview();
		Message quit = Message.obtain(decodeThread.getHandler(),
				ECGlobalConstance.QUIT);
		quit.sendToTarget();

		try {
			// Wait at most half a second; should be enough time, and onPause()
			// will timeout quickly
			decodeThread.join(500L);
		} catch (InterruptedException e) {
			// continue
		}

		// Be absolutely sure we don't send any queued up messages
		removeMessages(ECGlobalConstance.DECODE_SUCCEEDED);
		removeMessages(ECGlobalConstance.DECODE_FAILED);
	}

	/**
	 * 完成一次扫描后，只需要再调用此方法即可
	 */
	private void restartPreviewAndDecode() {
		if (state == State.SUCCESS) {
			state = State.PREVIEW;

			
			cameraManager.requestPreviewFrame(decodeThread.getHandler(),
					ECGlobalConstance.DECODE);
			ECQrCodeManager.getInstance(activity).getViewfinderView()
					.drawViewfinder();
		}
	}

}
