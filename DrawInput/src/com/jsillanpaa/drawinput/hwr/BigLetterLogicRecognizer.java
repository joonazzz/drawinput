package com.jsillanpaa.drawinput.hwr;

import java.util.ArrayList;

import android.util.Log;

import com.jsillanpaa.drawinput.char_recognizers.CharRecognitionResult;

public class BigLetterLogicRecognizer extends LogicRecognizer {
	

	private static final String TAG = "BigLetterLogicRecognizer";
	private static final float H_HOR_VER_MEAN_Y_MAX_DIFF = 0.3f;


	public BigLetterLogicRecognizer(int canvasWidth, int canvasHeight) {
		super(canvasWidth, canvasHeight);

	}


	@Override
	public CharRecognitionResult tryRecognition(HwrCharacter ch) {
		Log.i(TAG, "tryRecognition()");
		
		int num_strokes = ch.strokes.size();
		switch (num_strokes) {
			case 1: return tryOneStrokeRecognition(ch);
			case 2: return tryTwoStrokeRecognition(ch);
			case 3: return tryThreeStrokeRecognition(ch);
			default: return null;
		}
		
	}

	private CharRecognitionResult tryOneStrokeRecognition(HwrCharacter ch) {
		Log.i(TAG, "tryOneStrokeRecognition()");
		HwrStroke stroke = ch.strokes.get(0);
		
		if(isVerticalLine(stroke)){
			return new CharRecognitionResult('I');
		}

		return null;
	}


	private CharRecognitionResult tryTwoStrokeRecognition(HwrCharacter ch) {
		Log.i(TAG, "tryTwoStrokeRecognition()");
		HwrStroke stroke1 = ch.strokes.get(0);
		HwrStroke stroke2= ch.strokes.get(1);
		
		
		if(is_x(stroke1, stroke2)){
			return new CharRecognitionResult('X');
		}
		else if(is_t(stroke1, stroke2)){
			return new CharRecognitionResult('T');
		}
		
		return null;
	}

	private CharRecognitionResult tryThreeStrokeRecognition(HwrCharacter ch) {
		Log.i(TAG, "tryTwoStrokeRecognition()");

		if(is_h(ch)){
			return new CharRecognitionResult('H');
		}
		
		
		return null;
	}


	private boolean is_h(HwrCharacter ch) {
		Log.i(TAG, "is_h()");
		/* Get a shallow copy of strokes list. */
		@SuppressWarnings("unchecked")
		ArrayList<HwrStroke> strokes = (ArrayList<HwrStroke>) ch.strokes.clone();
		
		HwrStroke hor1, ver1, ver2;
		
		if( (hor1 = popHorizontalFromStrokes(strokes)) == null){
			Log.i(TAG, "is_h() : no horizontal stroke!");
			return false;
		}
			
		if( (ver1 = popVerticalFromStrokes(strokes)) == null){
			Log.i(TAG, "is_h() : no vertical stroke!");
			return false;
		}
			
		if( (ver2 = popVerticalFromStrokes(strokes)) == null){
			Log.i(TAG, "is_h() : no second vertical stroke!");
			return false;
		}
		
		if(!areHorizontallyAligned(ver1, ver2)){
			Log.i(TAG, "is_h() : ver1 and ver2 not horizontally aligned!");
			return false;
		}
			
		/* Hor line should cross near ver1 and ver2 center point. */
		if(Math.abs(hor1.getMeanY() - ver1.getMeanY())/ver1.getHeight() > H_HOR_VER_MEAN_Y_MAX_DIFF){
			Log.i(TAG, "is_h() : hor line does not cross ver1, ver2 near center");
			return false;
		}
			
		
		/* Hor should go beyond, or at least close to left bar */
		if(!goesBeyondLeftOrAtLeastClose(hor1, getLeftMostFromStrokes(ver1, ver2))){
			Log.i(TAG, "is_h() : hor does not go left enough");
			return false;
		}
		/* Hor should go beyond, or at least close to right bar */
		if(!goesBeyondRightOrAtLeastClose(hor1, getRightMostFromStrokes(ver1, ver2))){
			Log.i(TAG, "is_h() : hor does not go right enough");
			return false;
		}
		
		return true;
	}







	

	

}
