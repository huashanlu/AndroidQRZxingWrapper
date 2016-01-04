package com.hisun.qrdemo;

import android.app.Activity;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.zxing.WriterException;
import com.hisun.qrcode.scanner.ECQrCodeManager;

public class EncoderQRActivity extends Activity {

	private ImageView qRImageView;

	

	
	private EditText editText;

	private Button buOnlyText;
	private Button buAddPic;




	private ECQrCodeManager ecQrCodeManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.qr_encoder_layout);

		buOnlyText = (Button) findViewById(R.id.bu_text);
		buAddPic = (Button) findViewById(R.id.bu_textandpic);
		editText = (EditText) findViewById(R.id.et_encoder);
		qRImageView = (ImageView) findViewById(R.id.qr_img);

		ecQrCodeManager = ECQrCodeManager.getInstance(this);

	}
	
	
	public void onClick(View v) {

		String text = editText.getText().toString().trim();

		if (TextUtils.isEmpty(text)) {

			return;
		}

		switch (v.getId()) {
		case R.id.bu_text:

			Bitmap bitmap = ecQrCodeManager.encoderToQrBitmap(text, 300, 300);
			qRImageView.setImageBitmap(bitmap);

			break;

		case R.id.bu_textandpic:

			Bitmap bitmapTmp = ((BitmapDrawable) getResources().getDrawable(
					R.drawable.qq)).getBitmap();

			Bitmap bitmapPic = null;
			try {
				bitmapPic = ecQrCodeManager.encoderToQrBitmapWrapBitmap(text,
						300, 300, bitmapTmp);
			} catch (WriterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (bitmapPic != null) {
				qRImageView.setImageBitmap(bitmapPic);
			}

			break;

		default:
			break;
		}

	}

	

	
}
