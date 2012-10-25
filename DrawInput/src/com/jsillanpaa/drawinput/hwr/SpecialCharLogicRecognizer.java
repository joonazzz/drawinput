package com.jsillanpaa.drawinput.hwr;

import android.util.Log;

import com.jsillanpaa.drawinput.char_recognizers.CharRecognitionResult;

public class SpecialCharLogicRecognizer extends LogicRecognizer {

	public SpecialCharLogicRecognizer(int canvasWidth, int canvasHeight) {
		super(canvasWidth, canvasHeight);
	}

	private static final String TAG = "SpecialCharHwrRecognizerByLogic";

	@Override
	public CharRecognitionResult tryRecognition(HwrCharacter ch) {
		Log.i(TAG, "tryRecognition()");
		
		
		Log.i(TAG, "canvasWidth= " +mCanvasWidth);
		Log.i(TAG, "canvasHeight= " +mCanvasHeight);
		
		int num_strokes = ch.strokes.size();
		switch (num_strokes) {
			case 4: return tryFourStrokeRecognition(ch);
			case 2: return tryTwoStrokeRecognition(ch);
			case 1: return tryOneStrokeRecognition(ch);
			default: return null;
		}
		
	}

	private CharRecognitionResult tryFourStrokeRecognition(HwrCharacter ch) {
		Log.i(TAG, "tryFourStrokeRecognition()");
		
		if(isHash(ch)){
			return new CharRecognitionResult('#');
		}
		return null;
	}

	private CharRecognitionResult tryOneStrokeRecognition(HwrCharacter ch) {
		Log.i(TAG, "tryOneStrokeRecognition()");
		HwrStroke stroke = ch.strokes.get(0);
		
		if(isPoint(stroke)){
			return new CharRecognitionResult('.');
		}
		else if(isComma(stroke)){
			return new CharRecognitionResult(',');
		}
		else if(isSingleQuote(stroke)){
			return new CharRecognitionResult('\'');
		}
		else if(isMinus(stroke)){
			return new CharRecognitionResult('-');
		}
		else if(isUnderScore(stroke)){
			return new CharRecognitionResult('_');
		}
		else if(isForwardSlash(stroke)){
			return new CharRecognitionResult('/');
		}
		else if(isBackSlash(stroke)){
			return new CharRecognitionResult('\\');
		}
		
		return null;
	}

	private CharRecognitionResult tryTwoStrokeRecognition(HwrCharacter ch) {
		Log.i(TAG, "tryTwoStrokeRecognition()");
		HwrStroke stroke1 = ch.strokes.get(0);
		HwrStroke stroke2 = ch.strokes.get(1);
		
		if(isDoublePoint(stroke1, stroke2)){
			return new CharRecognitionResult(':');
		}
		else if(isSemiColon(stroke1, stroke2)){
			return new CharRecognitionResult(';');
		}
		else if(isDoubleQuote(stroke1, stroke2)){
			return new CharRecognitionResult('\"');
		}
		else if(isEqualSign(stroke1, stroke2)){
			return new CharRecognitionResult('=');
		}
		else if(isPlusSign(stroke1, stroke2)){
			return new CharRecognitionResult('+');
		}
		else if(isExclamationMark(stroke1, stroke2)){
			return new CharRecognitionResult('!');
		}
		return null;
	}

	


}
