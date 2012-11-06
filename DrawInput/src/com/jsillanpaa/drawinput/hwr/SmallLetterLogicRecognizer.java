package com.jsillanpaa.drawinput.hwr;

import android.util.Log;

import com.jsillanpaa.drawinput.char_recognizers.CharRecognitionResult;

public class SmallLetterLogicRecognizer extends LogicRecognizer {
	

	private static final String TAG = "SmallLetterLogicRecognizer";


	public SmallLetterLogicRecognizer(int canvasWidth, int canvasHeight) {
		super(canvasWidth, canvasHeight);
	}

	@Override
	public CharRecognitionResult tryRecognition(HwrCharacter ch) {
		Log.d(TAG, "tryRecognition()");
		
		
		Log.d(TAG, "canvasWidth= " +mCanvasWidth);
		Log.d(TAG, "canvasHeight= " +mCanvasHeight);
		
		int num_strokes = ch.strokes.size();
		switch (num_strokes) {
			case 1: return tryOneStrokeRecognition(ch);
			case 2: return tryTwoStrokeRecognition(ch);
			default: return null;
		}
		
	}

	private CharRecognitionResult tryOneStrokeRecognition(HwrCharacter ch) {
		Log.d(TAG, "tryOneStrokeRecognition()");
		HwrStroke stroke = ch.strokes.get(0);
		
		if(isVerticalLine(stroke)){
			return new CharRecognitionResult('l');
		}

		return null;
	}


	private CharRecognitionResult tryTwoStrokeRecognition(HwrCharacter ch) {
		Log.d(TAG, "tryTwoStrokeRecognition()");
		HwrStroke stroke1 = ch.strokes.get(0);
		HwrStroke stroke2= ch.strokes.get(1);
		
		if(is_i(stroke1, stroke2)){
			return new CharRecognitionResult('i');
		}
		else if(is_x(stroke1, stroke2)){
			return new CharRecognitionResult('x');
		}
		else if(is_t(stroke1, stroke2)){
			return new CharRecognitionResult('t');
		}
		
		return null;
	}



	private boolean is_i(HwrStroke stroke1, HwrStroke stroke2) {
		HwrStroke upper = getUpperFromStrokes(stroke1, stroke2);
		HwrStroke lower = getLowerFromStrokes(stroke1, stroke2);
		return isPointOrVerticalTick(upper) && isVerticalLine(lower);
	}


}
