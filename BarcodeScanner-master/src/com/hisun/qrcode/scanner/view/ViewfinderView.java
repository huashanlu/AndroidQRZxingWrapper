package com.hisun.qrcode.scanner.view;

import java.util.ArrayList;


import java.util.Collection;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import android.util.AttributeSet;
import android.view.View;

import com.google.zxing.ResultPoint;

import com.hisun.qrcode.scanner.camera.CameraManager;


public final class ViewfinderView extends View {

	
	private static final long ANIMATION_DELAY = 10L;
	private static final int OPAQUE = 0xFF;

	

	
	private static int MIDDLE_LINE_WIDTH;

	
	private static int MIDDLE_LINE_PADDING;

	
	private static final int SPEEN_DISTANCE = 10;

	
	private Paint paint;
	private static float density;

	
	private int slideTop;

	
	private int slideBottom;

	private static final int MAX_RESULT_POINTS = 20;
	private static final int CORNER_WIDTH = 5;

	private Bitmap resultBitmap;
	private int lineColor =Color.RED ;
	private int rectColor=Color.WHITE;
	
	
	
	
	public int getRectColor() {
		return rectColor;
	}

	public void setRectColor(int rectColor) {
		this.rectColor = rectColor;
		
	}

	public int getLineColor() {
		return lineColor;
	}

	public void setLineColor(int lineColor) {
		this.lineColor = lineColor;
		
	}

	
	private final int maskColor;
	private final int resultColor;

	private final int resultPointColor;
	private List<ResultPoint> possibleResultPoints;

	private List<ResultPoint> lastPossibleResultPoints;
	
	

	
	boolean isFirst = true;

	private CameraManager cameraManager;
	private int ScreenRate;

	// This constructor is used when the class is built from an XML resource.
	public ViewfinderView(Context context, AttributeSet attrs) {
		super(context, attrs);

		
		MIDDLE_LINE_PADDING = dip2px(context, 20.0F);
		MIDDLE_LINE_WIDTH = dip2px(context, 3.0F);
		density = context.getResources().getDisplayMetrics().density;

		paint = new Paint(Paint.ANTI_ALIAS_FLAG); 

		Resources resources = getResources();
		maskColor = resources.getColor(android.R.color.transparent); 
		resultColor = resources.getColor(android.R.color.white);

		resultPointColor = resources.getColor(android.R.color.white);
		possibleResultPoints = new ArrayList<ResultPoint>(5);
		lastPossibleResultPoints = null;
		ScreenRate = (int) (30 * density);

	}

	public void setCameraManager(CameraManager cameraManager) {
		this.cameraManager = cameraManager;
	}

	@Override
	public void onDraw(Canvas canvas) {
		if (cameraManager == null) {
			return; // not ready yet, early draw before done configuring
		}
		Rect frame = cameraManager.getFramingRect();
		if (frame == null) {
			return;
		}

		
		drawCover(canvas, frame);

		if (resultBitmap != null) { 
			// Draw the opaque result bitmap over the scanning rectangle
			paint.setAlpha(0xA0);
			canvas.drawBitmap(resultBitmap, null, frame, paint);
		}
		else {

			
			drawRectEdges(canvas, frame);

			
			drawScanningLine(canvas, frame);

			List<ResultPoint> currentPossible = possibleResultPoints;
			Collection<ResultPoint> currentLast = lastPossibleResultPoints;
			if (currentPossible.isEmpty()) {
				lastPossibleResultPoints = null;
			}
			else {
				possibleResultPoints = new ArrayList<ResultPoint>(5);
				lastPossibleResultPoints = currentPossible;
				paint.setAlpha(OPAQUE);
				paint.setColor(resultPointColor);
				for (ResultPoint point : currentPossible) {
					canvas.drawCircle(frame.left + point.getX(), frame.top
							+ point.getY(), 6.0f, paint);
				}
			}
			if (currentLast != null) {
				paint.setAlpha(OPAQUE / 2);
				paint.setColor(resultPointColor);
				for (ResultPoint point : currentLast) {
					canvas.drawCircle(frame.left + point.getX(), frame.top
							+ point.getY(), 3.0f, paint);
				}
			}

			
			postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top,
					frame.right, frame.bottom);

		}
	}

	
	private void drawScanningLine(Canvas canvas, Rect frame) {

		
		if (isFirst) {
			isFirst = false;
			slideTop = frame.top;
			slideBottom = frame.bottom;
		}

		
		slideTop += SPEEN_DISTANCE;
		if (slideTop >= slideBottom) {
			slideTop = frame.top;
		}

		
		Rect lineRect = new Rect();
		lineRect.left = frame.left + MIDDLE_LINE_PADDING;
		lineRect.right = frame.right - MIDDLE_LINE_PADDING;
		lineRect.top = slideTop;
		lineRect.bottom = (slideTop + MIDDLE_LINE_WIDTH);
//		canvas.drawBitmap(((BitmapDrawable) (BitmapDrawable) getResources()
//				.getDrawable(R.drawable.scan_laser)).getBitmap(), null,
//				lineRect, paint);
		paint.setColor(lineColor);
		canvas.drawRect(lineRect.left, lineRect.top, lineRect.right, lineRect.bottom, paint);
		
			

	}

	
	private void drawCover(Canvas canvas, Rect frame) {

		
		int width = canvas.getWidth();
		int height = canvas.getHeight();

		
		paint.setColor(resultBitmap != null ? resultColor : maskColor);

		
		canvas.drawRect(0, 0, width, frame.top, paint);
		canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
		canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1,
				paint);
		canvas.drawRect(0, frame.bottom + 1, width, height, paint);
	}

	
	private void drawRectEdges(Canvas canvas, Rect frame) {

//		paint.setColor(Color.WHITE);
		paint.setAlpha(OPAQUE);

		paint.setColor(rectColor);
		canvas.drawRect(frame.left, frame.top, frame.left + ScreenRate,
				frame.top + CORNER_WIDTH, paint);
		canvas.drawRect(frame.left, frame.top, frame.left + CORNER_WIDTH,
				frame.top + ScreenRate, paint);
		canvas.drawRect(frame.right - ScreenRate, frame.top, frame.right,
				frame.top + CORNER_WIDTH, paint);
		canvas.drawRect(frame.right - CORNER_WIDTH, frame.top, frame.right,
				frame.top + ScreenRate, paint);
		canvas.drawRect(frame.left, frame.bottom - CORNER_WIDTH, frame.left
				+ ScreenRate, frame.bottom, paint);
		canvas.drawRect(frame.left, frame.bottom - ScreenRate, frame.left
				+ CORNER_WIDTH, frame.bottom, paint);
		canvas.drawRect(frame.right - ScreenRate, frame.bottom - CORNER_WIDTH,
				frame.right, frame.bottom, paint);
		canvas.drawRect(frame.right - CORNER_WIDTH, frame.bottom - ScreenRate,
				frame.right, frame.bottom, paint);

		
	}

	public void drawViewfinder() {
		Bitmap resultBitmap = this.resultBitmap;
		this.resultBitmap = null;
		if (resultBitmap != null) {
			resultBitmap.recycle();
		}
		invalidate();
	}

	
	public void drawResultBitmap(Bitmap barcode) {
		resultBitmap = barcode;
		invalidate();
	}

	public void addPossibleResultPoint(ResultPoint point) {
		List<ResultPoint> points = possibleResultPoints;
		synchronized (points) {
			points.add(point);
			int size = points.size();
			if (size > MAX_RESULT_POINTS) {
				// trim it
				points.subList(0, size - MAX_RESULT_POINTS / 2).clear();
			}
		}
	}

	
	public int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

}
