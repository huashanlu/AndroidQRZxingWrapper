package com.hisun.qrcode.scanner;

import java.util.Hashtable;
import java.util.Vector;

import android.content.Context;
import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;



public class ECBitmapDecoder {

	MultiFormatReader multiReader;

	public ECBitmapDecoder(Context context) {

		multiReader = new MultiFormatReader();

		
		Hashtable<DecodeHintType, Object> hints = new Hashtable<DecodeHintType, Object>(
				2);
		
		Vector<BarcodeFormat> decodeFormats = new Vector<BarcodeFormat>();
		if (decodeFormats == null || decodeFormats.isEmpty()) {
			decodeFormats = new Vector<BarcodeFormat>();

			
			decodeFormats.addAll(ECDecodeFormatManager.ONE_D_FORMATS);
			decodeFormats.addAll(ECDecodeFormatManager.QR_CODE_FORMATS);
			decodeFormats.addAll(ECDecodeFormatManager.DATA_MATRIX_FORMATS);
		}
		hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);

		
		hints.put(DecodeHintType.CHARACTER_SET, "UTF8");

		
		multiReader.setHints(hints);

	}

	
	public Result getRawResult(Bitmap bitmap) {
		if (bitmap == null) {
			return null;
		}

		try {
			return multiReader.decodeWithState(new BinaryBitmap(
					new HybridBinarizer(new ECBitmapLuminanceSource(bitmap))));
		}
		catch (NotFoundException e) {
			e.printStackTrace();
		}

		return null;
	}
}
