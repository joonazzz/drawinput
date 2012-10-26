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

	

	public boolean isHash(HwrCharacter ch){
		if(ch.strokes.size() != 4)
			return false;
		
		HwrStroke s0 = ch.strokes.get(0);
		HwrStroke s1 = ch.strokes.get(1);
		HwrStroke s2 = ch.strokes.get(2);
		HwrStroke s3 = ch.strokes.get(3);
		
		/* This is nice code:
		 * case1: ver, ver, hor, hor
		 * case2: ver, hor, ver, hor
		 * case3: ver, hor, hor, ver
		 * case4: hor, ver, ver, hor
		 * case5: hor, ver, hor, ver
		 * case6: hor, hor, ver, ver*/
		if(isVerticalLine(s0)){
			if(isVerticalLine(s1)){
				if(isHorizontalLine(s2) && isHorizontalLine(s3)){
						return true; // case1
				}
			}
			else if(isHorizontalLine(s1)){
				if(isVerticalLine(s2)){
					if(isHorizontalLine(s3)){
						return true; // case2
					}
				}
				else if(isHorizontalLine(s1)){
					if(isVerticalLine(s3)){
						return true; // case3
					}
				}
			}
		}
		else if(isHorizontalLine(s0)){
			if(isVerticalLine(s1)){
				if(isVerticalLine(s2)){
					if(isHorizontalLine(s3)){
						return true; // case4
					}
				}
				else if(isHorizontalLine(s2)){
					if(isVerticalLine(s3)){
						return true; // case5
					}
				}
			}
			else if(isHorizontalLine(s1)){
				if(isVerticalLine(s2) && isVerticalLine(s3)){
						return true; // case6
				}
			}
			
		}
		
		return false;
	}
	
	/**
	 * Tests if given stroke is a comma. Assumes that stroke
	 * was not a point.
	 *  
	 * @param stroke 
	 * @return
	 */
	public boolean isComma(HwrStroke stroke) {
		return isVerticalTick(stroke) && !isUpperTick(stroke);
	}
	
	public boolean isSingleQuote(HwrStroke stroke) {
		return isVerticalTick(stroke) && isUpperTick(stroke);
	}
	
	public boolean isMinus(HwrStroke stroke) {
		return isHorizontalLine(stroke) && !isOnLowerDrawLineOrBelow(stroke);
	}
	
	public boolean isUnderScore(HwrStroke stroke) {
		return isHorizontalLine(stroke) && isOnLowerDrawLineOrBelow(stroke);
	}
	
	
	public boolean isDoublePoint(HwrStroke stroke1, HwrStroke stroke2) {
		return isPoint(stroke1) && isPoint(stroke2) && areVerticallyAligned(stroke1, stroke2);
	}
	
	public boolean isSemiColon(HwrStroke stroke1, HwrStroke stroke2) {
		HwrStroke upper = getUpperFromStrokes(stroke1, stroke2);
		HwrStroke lower = getLowerFromStrokes(stroke1, stroke2);
		return isPoint(upper) && isVerticalTick(lower) && areVerticallyAligned(stroke1, stroke2);
	}
	
	public boolean isDoubleQuote(HwrStroke stroke1, HwrStroke stroke2) {
		return isSingleQuote(stroke1) && isSingleQuote(stroke2) && areHorizontallyAligned(stroke1, stroke2);
	}
	
	public boolean isEqualSign(HwrStroke stroke1, HwrStroke stroke2) {
		return isHorizontalLine(stroke1) && isHorizontalLine(stroke2) && areVerticallyAligned(stroke1, stroke2);
	}
	
	public boolean isPlusSign(HwrStroke stroke1, HwrStroke stroke2) {
		if(isHorizontalLine(stroke1)){
			return isVerticalLine(stroke2) && horVerCrossNearCenter(stroke1, stroke2);
		}
		else if(isVerticalLine(stroke1)){
			return isHorizontalLine(stroke2) && horVerCrossNearCenter(stroke2, stroke1);
		}
		return false;
	}
	
	public boolean isExclamationMark(HwrStroke stroke1, HwrStroke stroke2) {
		if(isVerticalLine(stroke1)){
			return isPointOrVerticalTick(stroke2) && areVerticallyAligned(stroke1, stroke2);
		}
		else if(isVerticalLine(stroke2)){
			return isPointOrVerticalTick(stroke2) && areVerticallyAligned(stroke1, stroke2);
		}
		return false;
	}
	
	
	public boolean isForwardSlash(HwrStroke stroke) {
		return isDiagonalLine(stroke, true) == 1.0f;
	}
	
	public boolean isBackSlash(HwrStroke stroke) {
		return isDiagonalLine(stroke, true) == -1.0f;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	


}
