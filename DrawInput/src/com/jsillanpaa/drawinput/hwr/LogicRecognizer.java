package com.jsillanpaa.drawinput.hwr;

import android.graphics.PointF;
import android.graphics.RectF;
import android.util.FloatMath;
import android.util.Log;

import com.jsillanpaa.drawinput.char_recognizers.CharRecognitionResult;

public abstract class LogicRecognizer {

	private static final String TAG = "LogicRecognizer";
	
	public static final float LOWER_DRAWLINE_Y = 0.75f;
	public static final float UPPER_DRAWLINE_Y = 0.25f;
	
	private static final float POINT_WIDTH_MAX = 0.05f;
	private static final float POINT_HEIGHT_MAX = 0.05f;
	
	private static final float COMMA_WIDTH_MAX = 0.10f;
	private static final float COMMA_HEIGHT_MAX = 0.14f;
	
	private static final float HOR_VER_LINE_MIN_LENGTH = 0.13f;
	private static final float HOR_VER_LINE_MAX_ASPECT_RATIO = 1.0f/3.0f;

	private static final float DIAGONAL_LINE_MIN_X_WIDTH = HOR_VER_LINE_MIN_LENGTH;
	
	private static final float ALIGNED_STROKES_MAX_DIFF = 0.15f;
	private static final float NEAR_CENTER_DIFF = 0.15f;
	private static final float VERTICAL_LINE_K = 100.0f;

	private static final float MAX_ANGLE_DIFF_DIAGONAL_K = 25.0f;
	private static final float MAX_DIAGONAL_LINE_SCATTER = 0.15f;

	
	protected int mCanvasWidth;
	protected int mCanvasHeight;
	
	public LogicRecognizer(int canvasWidth, int canvasHeight) {
		mCanvasWidth = canvasWidth;
		mCanvasHeight = canvasHeight;
	}
	public abstract CharRecognitionResult tryRecognition(HwrCharacter ch);
	
	
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
	public boolean isPoint(HwrStroke stroke) {
		RectF bbox = stroke.getRect();
		float normalized_width = (bbox.right - bbox.left) / mCanvasWidth;
		float normalized_height = (bbox.bottom - bbox.top) / mCanvasHeight;
		
		Log.i(TAG, "isPoint(), normalized_width = " + normalized_width);
		Log.i(TAG, "isPoint(), normalized_height = " + normalized_height);
		
		if(normalized_width < POINT_WIDTH_MAX && normalized_height < POINT_HEIGHT_MAX){
			return true;
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
			return (isPoint(stroke2) || isVerticalTick(stroke2)) && areVerticallyAligned(stroke1, stroke2);
		}
		else if(isVerticalLine(stroke2)){
			return (isPoint(stroke1) || isVerticalTick(stroke1)) && areVerticallyAligned(stroke1, stroke2);
		}
		return false;
	}
	
	
	public boolean horVerCrossNearCenter(HwrStroke hor_stroke, HwrStroke ver_stroke) {
		Log.i(TAG, "horVerCrossNearCenter");
		
		PointF hor_p0 = hor_stroke.points.get(0);
		PointF hor_p1 = hor_stroke.points.get(hor_stroke.points.size()-1);
		
		PointF ver_p0 = ver_stroke.points.get(0);
		PointF ver_p1 = ver_stroke.points.get(ver_stroke.points.size()-1);
		
		float hor_midpoint_x = (hor_p0.x + hor_p1.x)/2.0f;
		float ver_midpoint_y = (ver_p0.y + ver_p1.y)/2.0f;
		
		float diff_hor_midpoint = Math.abs(hor_midpoint_x-ver_p0.x)/mCanvasWidth;
		float diff_ver_midpoint = Math.abs(ver_midpoint_y-hor_p0.y)/mCanvasHeight;
		
		Log.i(TAG, "diff_hor_midpoint = " + diff_hor_midpoint);
		Log.i(TAG, "diff_ver_midpoint = " + diff_ver_midpoint);
		
		return diff_hor_midpoint < NEAR_CENTER_DIFF && diff_ver_midpoint < NEAR_CENTER_DIFF;
	}
	
	/**
	 * Assumes both are lines.
	 * 
	 * @param stroke1
	 * @param stroke2
	 * @return
	 */
	public static boolean crossNearCenter(HwrStroke stroke1, HwrStroke stroke2) {
		PointF s1_p0 = stroke1.points.get(0);
		PointF s1_p1 = stroke1.points.get(stroke1.points.size()-1);
		PointF s2_p0 = stroke2.points.get(0);
		PointF s2_p1 = stroke2.points.get(stroke2.points.size()-1);
		
		float k_s1 = (s1_p1.y - s1_p0.y)/(s1_p1.x - s1_p0.x);
		float k_s2 = (s2_p1.y - s2_p0.y)/(s2_p1.x - s2_p0.x);
		
		
		if(k_s1 == k_s2)
			return false;
		
		float x=Float.MIN_VALUE;
		if(Math.abs(k_s1) > VERTICAL_LINE_K){
			x = s1_p0.x;
		}
		else if(Math.abs(k_s2) > VERTICAL_LINE_K){
			x = s2_p0.x;
		}
		else{
			x = (k_s1*s1_p0.x - k_s2*s2_p0.x + s2_p0.y - s1_p0.y)/(k_s1 - k_s2);
		}
		
		if(isNearCenter(x, stroke1) && isNearCenter(x, stroke2) )
			return true;
		
		return false;
	}
	
	public static boolean isNearCenter(float x, HwrStroke stroke){
		Log.i(TAG, "isNearCenter");
		
		PointF p0 = stroke.points.get(0);
		
		if(stroke.points.size() == 1){
			return Math.abs(p0.x-x) < NEAR_CENTER_DIFF;
		}
		PointF p1 = stroke.points.get(stroke.points.size()-1);
		
		
		float x_mid = (p1.x+p0.x)/2.0f;
		float diff = Math.abs(x_mid-x);
		
		Log.i(TAG, "x = " + x);
		Log.i(TAG, "x_mid = " + x_mid);
		Log.i(TAG, "diff="+diff);
		return Math.abs(x_mid-x) < NEAR_CENTER_DIFF;
	}
	
	public boolean areHorizontallyAligned(HwrStroke stroke1, HwrStroke stroke2) {
		return Math.abs(stroke1.getMeanY()-stroke2.getMeanY())/mCanvasHeight < ALIGNED_STROKES_MAX_DIFF;
	}
	public boolean areVerticallyAligned(HwrStroke stroke1, HwrStroke stroke2) {
		return Math.abs(stroke1.getMeanX()-stroke2.getMeanX())/mCanvasWidth < ALIGNED_STROKES_MAX_DIFF;
	}
	
	public boolean isForwardSlash(HwrStroke stroke) {
		return isDiagonalLine(stroke, true) == 1.0f;
	}
	
	public boolean isBackSlash(HwrStroke stroke) {
		return isDiagonalLine(stroke, true) == -1.0f;
	}
	
	/**
	 * Line is considered diagonal if:
	 * 
	 * 0. Line is long enough in x- direction
	 * 1. Line l1 from p0 -> p1 is about 45 degs
	 * 2. No point is too far from l1
	 * 
	 * @param stroke
	 * @return direction of diagonal line: +1 upwards, -1 downwards, 0 when neither
	 */
	public float isDiagonalLine(HwrStroke stroke, boolean y_grows_down) {
		Log.i(TAG, "isDiagonalLine");
		
		PointF p0 = stroke.getLeftMost();
		PointF p1 = stroke.getRightMost();
		
		float k = (p1.y - p0.y)/(p1.x - p0.x);
		float A = k;
		float B = -1.0f;
		float C = p0.y - k*p0.x;
		float line_width = Math.abs(p1.x - p0.x);
		
		/* Check that line is long enough */
		if(line_width/mCanvasWidth < DIAGONAL_LINE_MIN_X_WIDTH){
			return 0.0f;
		}
		/* Check that angle is about 45 degs */
		float positive_angle = (float) ((180.0/Math.PI)*Math.atan(Math.abs(k)));
		Log.i(TAG, "isDiagonalLine: angle was "+positive_angle);
		if(Math.abs(positive_angle - 45.0) > MAX_ANGLE_DIFF_DIAGONAL_K){
			Log.i(TAG, "isDiagonalLine: => is not diagonal line");
			return 0.0f;
		}
		
		/* Check that all points are close to this line, l1 : Ax + Bx + C = 0 */
		float d;
		for (PointF p : stroke.points) {
			d = Math.abs(A*p.x + B*p.y + C) / (FloatMath.sqrt( A*A + B*B));
			//Log.i(TAG, "isDiagonalLine: normalized d was "+d/line_width);
			if(d/line_width > MAX_DIAGONAL_LINE_SCATTER){
				//Log.i(TAG, "isDiagonalLine: => is not diagonal line");
				return 0.0f;
			}
		}
		
		if(y_grows_down)
			return k < 0.0 ? 1.0f : -1.0f;
		else
			return k > 0.0 ? 1.0f : -1.0f;
	}
	public boolean isHorizontalLine(HwrStroke stroke) {
		RectF bbox = stroke.getRect();
		float normalized_width = (bbox.right - bbox.left) / mCanvasWidth;
		float normalized_height = (bbox.bottom - bbox.top) / mCanvasHeight;
		float line_aspect_ratio = normalized_height / normalized_width;
		return normalized_width > HOR_VER_LINE_MIN_LENGTH && line_aspect_ratio < HOR_VER_LINE_MAX_ASPECT_RATIO; 

	}
	private boolean isVerticalLine(HwrStroke stroke) {
		RectF bbox = stroke.getRect();
		float normalized_height = (bbox.bottom - bbox.top) / mCanvasHeight;
		float normalized_width = (bbox.right - bbox.left) / mCanvasWidth;
		float line_aspect_ratio = normalized_width / normalized_height;
		
		return normalized_height > HOR_VER_LINE_MIN_LENGTH && line_aspect_ratio < HOR_VER_LINE_MAX_ASPECT_RATIO;
	}
	public boolean isOnLowerDrawLineOrBelow(HwrStroke stroke) {
		RectF bbox = stroke.getRect();
		return bbox.top/mCanvasHeight > (LOWER_DRAWLINE_Y - 0.25*(LOWER_DRAWLINE_Y - UPPER_DRAWLINE_Y));
	}
	
	public boolean isUpperTick(HwrStroke stroke) {
		float normalized_mean_y = stroke.getMeanY() / mCanvasHeight;
		return normalized_mean_y < 0.5f; // remember y grows down
	}
	
	/**
	 * Tests if given stroke is a small vertical tick, like '
	 * 
	 *  
	 * @param stroke 
	 * @return
	 */
	public boolean isVerticalTick(HwrStroke stroke) {
		RectF bbox = stroke.getRect();
		float normalized_width = (bbox.right - bbox.left) / mCanvasWidth;
		float normalized_height = (bbox.bottom - bbox.top) / mCanvasHeight;
		
		if(normalized_width < COMMA_WIDTH_MAX && normalized_height < COMMA_HEIGHT_MAX){
			return true;
		}
		return false;
		
	}
	
	public static HwrStroke getLowerFromStrokes(HwrStroke left,
			HwrStroke right) {
		// y grows down
		if(left.points.get(0).y > right.points.get(0).y){
			return left;
		}
		else{
			return right;
		}
	}

	public static HwrStroke getUpperFromStrokes(HwrStroke left,
			HwrStroke right) {
		// y grows down
		if(left.points.get(0).y <= right.points.get(0).y){
			return left;
		}
		else{
			return right;
		}
	}
}
