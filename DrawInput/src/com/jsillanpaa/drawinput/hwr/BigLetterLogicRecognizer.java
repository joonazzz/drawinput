package com.jsillanpaa.drawinput.hwr;

import java.util.ArrayList;

import android.util.Log;

import com.jsillanpaa.drawinput.char_recognizers.CharRecognitionResult;

public class BigLetterLogicRecognizer extends LogicRecognizer {
	

	private static final String TAG = "BigLetterLogicRecognizer";
	private static final float H_HOR_VER_MEAN_Y_MAX_DIFF = 0.3f;
	//private static final float //F__VER_MEAN_Y_MAX_DIFF = 0.3f;
	private static final float F_UPPER_HOR_Y_DIFF = 0.2f;
	private static final float F_LOWER_HOR_Y_DIFF = 0.35f;
	private static final float E_UPPER_HOR_Y_DIFF = 0.2f;
	private static final float E_LOWER_HOR_Y_DIFF = 0.2f;


	public BigLetterLogicRecognizer(int canvasWidth, int canvasHeight) {
		super(canvasWidth, canvasHeight);

	}


	@Override
	public CharRecognitionResult tryRecognition(HwrCharacter ch) {
		Log.d(TAG, "tryRecognition()");
		
		int num_strokes = ch.strokes.size();
		switch (num_strokes) {
			case 1: return tryOneStrokeRecognition(ch);
			case 2: return tryTwoStrokeRecognition(ch);
			case 3: return tryThreeStrokeRecognition(ch);
			case 4: return tryFourStrokeRecognition(ch);
			default: return null;
		}
		
	}

	private CharRecognitionResult tryOneStrokeRecognition(HwrCharacter ch) {
		Log.d(TAG, "tryOneStrokeRecognition()");
		HwrStroke stroke = ch.strokes.get(0);
		
		if(isVerticalLine(stroke)){
			return new CharRecognitionResult('I');
		}

		return null;
	}


	private CharRecognitionResult tryTwoStrokeRecognition(HwrCharacter ch) {
		Log.d(TAG, "tryTwoStrokeRecognition()");
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
		if(is_H(ch)){
			return new CharRecognitionResult('H');
		}
		if(is_F(ch)){
			return new CharRecognitionResult('F');
		}
		
		
		return null;
	}

	private CharRecognitionResult tryFourStrokeRecognition(HwrCharacter ch) {

		if(is_E(ch)){
			return new CharRecognitionResult('E');
		}
		
		
		return null;
	}

	private boolean is_H(HwrCharacter ch) {
		
		/* Get a shallow copy of strokes list. */
		@SuppressWarnings("unchecked")
		ArrayList<HwrStroke> strokes = (ArrayList<HwrStroke>) ch.strokes.clone();
		
		HwrStroke hor1, ver1, ver2;
		
		if( (hor1 = popHorizontalFromStrokes(strokes)) == null){
			Log.d(TAG, "is_h() : no horizontal stroke!");
			return false;
		}
			
		if( (ver1 = popVerticalFromStrokes(strokes)) == null){
			Log.d(TAG, "is_h() : no vertical stroke!");
			return false;
		}
			
		if( (ver2 = popVerticalFromStrokes(strokes)) == null){
			Log.d(TAG, "is_h() : no second vertical stroke!");
			return false;
		}
		
		if(!areHorizontallyAligned(ver1, ver2)){
			Log.d(TAG, "is_h() : ver1 and ver2 not horizontally aligned!");
			return false;
		}
			
		/* Hor line should cross near ver1 and ver2 center point. */
		if(Math.abs(hor1.getMeanY() - ver1.getMeanY())/ver1.getHeight() > H_HOR_VER_MEAN_Y_MAX_DIFF){
			Log.d(TAG, "is_h() : hor line does not cross ver1, ver2 near center");
			return false;
		}
			
		
		/* Hor should go beyond, or at least close to left bar */
		if(!goesBeyondLeftOrAtLeastClose(hor1, getLeftMostFromStrokes(ver1, ver2))){
			Log.d(TAG, "is_h() : hor does not go left enough");
			return false;
		}
		/* Hor should go beyond, or at least close to right bar */
		if(!goesBeyondRightOrAtLeastClose(hor1, getRightMostFromStrokes(ver1, ver2))){
			Log.d(TAG, "is_h() : hor does not go right enough");
			return false;
		}
		
		return true;
	}


	private boolean is_F(HwrCharacter ch) {

		/* Get a shallow copy of strokes list. */
		@SuppressWarnings("unchecked")
		ArrayList<HwrStroke> strokes = (ArrayList<HwrStroke>) ch.strokes.clone();
		
		HwrStroke ver1, hor1, hor2;
		
		if( (ver1 = popVerticalFromStrokes(strokes)) == null){
			Log.d(TAG, "is_F() : no vertical stroke!");
			return false;
		}
			
		if( (hor1 = popHorizontalFromStrokes(strokes)) == null){
			Log.d(TAG, "is_F() : no horizontal stroke!");
			return false;
		}
			
		if( (hor2 = popHorizontalFromStrokes(strokes)) == null){
			Log.d(TAG, "is_F() : no second horizontal stroke!");
			return false;
		}
		
		if(!areVerticallyAligned(hor1, hor2)){
			Log.d(TAG, "is_F() : hor1 and hor2 not vertically aligned!");
			return false;
		}
		
		HwrStroke upperHor = getUpperFromStrokes(hor1, hor2);
		HwrStroke lowerHor = getLowerFromStrokes(hor1, hor2);
			
		if(upperHor.getMeanX() < ver1.getMeanX()){
			Log.d(TAG, "is_F() : upperHor mean x is smaller than ver1");
			return false;
		}
		if(lowerHor.getMeanX() < ver1.getMeanX()){
			Log.d(TAG, "is_F() : lowerHor mean x is smaller than ver1");
			return false;
		}
		
		if( (Math.abs(upperHor.getMeanY() - ver1.getUpmost().y))/ver1.getHeight() > F_UPPER_HOR_Y_DIFF){
			Log.d(TAG, "is_F() : Upper hor y is not good");
			return false;
		}
		
		if( (Math.abs(lowerHor.getMeanY() - ver1.getMeanY()))/ver1.getHeight() > F_LOWER_HOR_Y_DIFF){
			Log.d(TAG, "is_F() : Lower hor y is not good");
			return false;
		}
		
		if(!goesBeyondLeftOrAtLeastClose(hor1, ver1)){
			Log.d(TAG, "is_F() : hor1 is not left enough");
			return false;
		}
		if(!goesBeyondLeftOrAtLeastClose(hor2, ver1)){
			Log.d(TAG, "is_F() : hor2 is not left enough");
			return false;
		}
		
		return true;
	}

	private boolean is_E(HwrCharacter ch) {

		/* Get a shallow copy of strokes list. */
		@SuppressWarnings("unchecked")
		ArrayList<HwrStroke> strokes = (ArrayList<HwrStroke>) ch.strokes.clone();
		
		HwrStroke ver1, hor1, hor2, hor3;
		
		if( (ver1 = popVerticalFromStrokes(strokes)) == null){
			Log.d(TAG, "is_F() : no vertical stroke!");
			return false;
		}
			
		if( (hor1 = popHorizontalFromStrokes(strokes)) == null){
			Log.d(TAG, "is_F() : no horizontal stroke!");
			return false;
		}
		if( (hor2 = popHorizontalFromStrokes(strokes)) == null){
			Log.d(TAG, "is_F() : no second horizontal stroke!");
			return false;
		}
		if( (hor3 = popHorizontalFromStrokes(strokes)) == null){
			Log.d(TAG, "is_F() : no third horizontal stroke!");
			return false;
		}
		
		if(!areVerticallyAligned(hor1, hor2)){
			Log.d(TAG, "is_F() : hor1 and hor2 not vertically aligned!");
			return false;
		}
		if(!areVerticallyAligned(hor2, hor3)){
			Log.d(TAG, "is_F() : hor2 and hor3 not vertically aligned!");
			return false;
		}
		
		HwrStroke upperTemp = getUpperFromStrokes(hor1, hor2);
		HwrStroke lowerTemp = getLowerFromStrokes(hor1, hor2);
		
		HwrStroke upperHor = getUpperFromStrokes(upperTemp, hor3);
		HwrStroke lowerHor = getLowerFromStrokes(lowerTemp, hor3);
			
		if(hor1.getMeanX() < ver1.getMeanX()){
			Log.d(TAG, "is_F() : hor1 mean x is smaller than ver1");
			return false;
		}
		if(hor2.getMeanX() < ver1.getMeanX()){
			Log.d(TAG, "is_F() : hor2 mean x is smaller than ver1");
			return false;
		}
		if(hor3.getMeanX() < ver1.getMeanX()){
			Log.d(TAG, "is_F() : hor3 mean x is smaller than ver1");
			return false;
		}
		
		if( (Math.abs(upperHor.getMeanY() - ver1.getUpmost().y))/ver1.getHeight() > E_UPPER_HOR_Y_DIFF){
			Log.d(TAG, "is_F() : Upper hor y is not good");
			return false;
		}
		
		if( (Math.abs(lowerHor.getMeanY() - ver1.getLowest().y))/ver1.getHeight() > E_LOWER_HOR_Y_DIFF){
			Log.d(TAG, "is_F() : Lower hor y is not good");
			return false;
		}
		
		if(!goesBeyondLeftOrAtLeastClose(hor1, ver1)){
			Log.d(TAG, "is_F() : hor1 is not left enough");
			return false;
		}
		if(!goesBeyondLeftOrAtLeastClose(hor2, ver1)){
			Log.d(TAG, "is_F() : hor2 is not left enough");
			return false;
		}
		if(!goesBeyondLeftOrAtLeastClose(hor3, ver1)){
			Log.d(TAG, "is_F() : hor3 is not left enough");
			return false;
		}
		
		return true;
	}




	

	

}
