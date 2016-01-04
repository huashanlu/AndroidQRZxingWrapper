package com.hisun.qrcode.scanner;

public interface ECQrDecoderFromBitmapListener {
	
	
	
	void onDecoderSucceed(String result);
	void onDecoderFailed();
	void onDecoderStarted();
	void onDecoderDoInBackGround();

}
