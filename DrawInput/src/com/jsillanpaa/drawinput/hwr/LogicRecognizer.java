package com.jsillanpaa.drawinput.hwr;

import java.util.ArrayList;

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
	private static final float HOR_LINE_MIN_LENGTH = 0.11f;

	private static final float HOR_VER_LINE_MAX_ASPECT_RATIO = 1.0f / 3.0f;

	private static final float DIAGONAL_LINE_MIN_X_WIDTH = HOR_VER_LINE_MIN_LENGTH;

	private static final float ALIGNED_STROKES_MAX_DIFF = 0.15f;
	private static final float NEAR_CENTER_DIFF = 0.15f;
	private static final float VERTICAL_LINE_K = 100.0f;

	private static final float MAX_ANGLE_DIFF_DIAGONAL_K = 25.0f;
	private static final float MAX_DIAGONAL_LINE_SCATTER = 0.17f;
	private static final float X_DIAGONAL_LINES_XMEAN_DIFF = 0.15f;

	private static final float GOES_BEYOND_OR_IS_CLOSE_LIMIT = -0.15f;

	private static final int NUM_CURVATURE_MEASUREMENT_POINTS = 10;
	private static final int CURVATURE_IGNORE_N_LAST_POINTS = 1;
	private static final int CURVATURE_IGNORE_N_FIRST_POINTS = 1;

	private static final float LINE_CURVATURE_MAX = 1.5f;


	protected int mCanvasWidth;
	protected int mCanvasHeight;

	public LogicRecognizer(int canvasWidth, int canvasHeight) {
		mCanvasWidth = canvasWidth;
		mCanvasHeight = canvasHeight;
	}

	public abstract CharRecognitionResult tryRecognition(HwrCharacter ch);

	public boolean isPoint(HwrStroke stroke) {
		RectF bbox = stroke.getRect();
		float normalized_width = (bbox.right - bbox.left) / mCanvasWidth;
		float normalized_height = (bbox.bottom - bbox.top) / mCanvasHeight;

		Log.d(TAG, "isPoint(), normalized_width = " + normalized_width);
		Log.d(TAG, "isPoint(), normalized_height = " + normalized_height);

		if (normalized_width < POINT_WIDTH_MAX
				&& normalized_height < POINT_HEIGHT_MAX) {
			return true;
		}
		return false;

	}

	public boolean isPointOrVerticalTick(HwrStroke stroke) {
		return isPoint(stroke) || isVerticalTick(stroke);
	}

	public boolean horVerCrossNearCenter(HwrStroke hor_stroke,
			HwrStroke ver_stroke) {
		Log.d(TAG, "horVerCrossNearCenter");

		PointF hor_p0 = hor_stroke.points.get(0);
		PointF hor_p1 = hor_stroke.points.get(hor_stroke.points.size() - 1);

		PointF ver_p0 = ver_stroke.points.get(0);
		PointF ver_p1 = ver_stroke.points.get(ver_stroke.points.size() - 1);

		float hor_midpoint_x = (hor_p0.x + hor_p1.x) / 2.0f;
		float ver_midpoint_y = (ver_p0.y + ver_p1.y) / 2.0f;

		float diff_hor_midpoint = Math.abs(hor_midpoint_x - ver_p0.x)
				/ mCanvasWidth;
		float diff_ver_midpoint = Math.abs(ver_midpoint_y - hor_p0.y)
				/ mCanvasHeight;

		Log.d(TAG, "diff_hor_midpoint = " + diff_hor_midpoint);
		Log.d(TAG, "diff_ver_midpoint = " + diff_ver_midpoint);

		return diff_hor_midpoint < NEAR_CENTER_DIFF
				&& diff_ver_midpoint < NEAR_CENTER_DIFF;
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
		PointF s1_p1 = stroke1.points.get(stroke1.points.size() - 1);
		PointF s2_p0 = stroke2.points.get(0);
		PointF s2_p1 = stroke2.points.get(stroke2.points.size() - 1);

		float k_s1 = (s1_p1.y - s1_p0.y) / (s1_p1.x - s1_p0.x);
		float k_s2 = (s2_p1.y - s2_p0.y) / (s2_p1.x - s2_p0.x);

		if (k_s1 == k_s2)
			return false;

		float x = Float.MIN_VALUE;
		if (Math.abs(k_s1) > VERTICAL_LINE_K) {
			x = s1_p0.x;
		} else if (Math.abs(k_s2) > VERTICAL_LINE_K) {
			x = s2_p0.x;
		} else {
			x = (k_s1 * s1_p0.x - k_s2 * s2_p0.x + s2_p0.y - s1_p0.y)
					/ (k_s1 - k_s2);
		}

		if (isNearCenter(x, stroke1) && isNearCenter(x, stroke2))
			return true;

		return false;
	}

	public static boolean isNearCenter(float x, HwrStroke stroke) {
		Log.d(TAG, "isNearCenter");

		PointF p0 = stroke.points.get(0);

		if (stroke.points.size() == 1) {
			return Math.abs(p0.x - x) < NEAR_CENTER_DIFF;
		}
		PointF p1 = stroke.points.get(stroke.points.size() - 1);

		float x_mid = (p1.x + p0.x) / 2.0f;
		float diff = Math.abs(x_mid - x);

		Log.d(TAG, "x = " + x);
		Log.d(TAG, "x_mid = " + x_mid);
		Log.d(TAG, "diff=" + diff);
		return Math.abs(x_mid - x) < NEAR_CENTER_DIFF;
	}

	public boolean areHorizontallyAligned(HwrStroke stroke1, HwrStroke stroke2) {
		return Math.abs(stroke1.getMeanY() - stroke2.getMeanY())
				/ mCanvasHeight < ALIGNED_STROKES_MAX_DIFF;
	}

	public boolean areVerticallyAligned(HwrStroke stroke1, HwrStroke stroke2) {
		return Math.abs(stroke1.getMeanX() - stroke2.getMeanX()) / mCanvasWidth < ALIGNED_STROKES_MAX_DIFF;
	}

	/**
	 * Line is considered diagonal if:
	 * 
	 * 0. Line is long enough in x- direction 1. Line l1 from p0 -> p1 is about
	 * 45 degs 2. No point is too far from l1
	 * 
	 * @param stroke
	 * @return direction of diagonal line: +1 upwards, -1 downwards, 0 when
	 *         neither
	 */
	public float isDiagonalLine(HwrStroke stroke, boolean y_grows_down) {
		Log.d(TAG, "isDiagonalLine");

		PointF p0 = stroke.getLeftMost();
		PointF p1 = stroke.getRightMost();

		float k = (p1.y - p0.y) / (p1.x - p0.x);
		float A = k;
		float B = -1.0f;
		float C = p0.y - k * p0.x;
		float line_width = Math.abs(p1.x - p0.x);

		/* Check that line is long enough */
		if (line_width / mCanvasWidth < DIAGONAL_LINE_MIN_X_WIDTH) {
			return 0.0f;
		}
		/* Check that angle is about 45 degs */
		float positive_angle = (float) ((180.0 / Math.PI) * Math.atan(Math
				.abs(k)));
		Log.d(TAG, "isDiagonalLine: angle was " + positive_angle);
		if (Math.abs(positive_angle - 45.0) > MAX_ANGLE_DIFF_DIAGONAL_K) {
			Log.d(TAG, "isDiagonalLine: => is not diagonal line");
			return 0.0f;
		}

		/* Check that all points are close to this line, l1 : Ax + Bx + C = 0 */
		float d;
		for (PointF p : stroke.points) {
			d = Math.abs(A * p.x + B * p.y + C)
					/ (FloatMath.sqrt(A * A + B * B));
			Log.d(TAG, "isDiagonalLine: normalized d was " + d / mCanvasWidth);
			if (d / mCanvasWidth > MAX_DIAGONAL_LINE_SCATTER) {
				Log.d(TAG, "isDiagonalLine: => is not diagonal line");
				return 0.0f;
			}
		}

		/* Check that curvature is not too high*/
		if(lineCurvatureTooHigh(stroke)){
			return 0.0f;
		}
		
		if (y_grows_down)
			return k < 0.0 ? 1.0f : -1.0f;
		else
			return k > 0.0 ? 1.0f : -1.0f;
	}

	public boolean isHorizontalLine(HwrStroke stroke) {
		RectF bbox = stroke.getRect();
		float normalized_width = (bbox.right - bbox.left) / mCanvasWidth;
		float normalized_height = (bbox.bottom - bbox.top) / mCanvasHeight;
		float line_aspect_ratio = normalized_height / normalized_width;
		/* Check that curvature is not too high*/
		if(lineCurvatureTooHigh(stroke)){
			return false;
		}
		
		return normalized_width > HOR_LINE_MIN_LENGTH
				&& line_aspect_ratio < HOR_VER_LINE_MAX_ASPECT_RATIO;

	}

	public boolean isVerticalLine(HwrStroke stroke) {
		RectF bbox = stroke.getRect();
		float normalized_height = (bbox.bottom - bbox.top) / mCanvasHeight;
		float normalized_width = (bbox.right - bbox.left) / mCanvasWidth;
		float line_aspect_ratio = normalized_width / normalized_height;

		if(lineCurvatureTooHigh(stroke)){
			return false;
		}
		
		
		return normalized_height > HOR_VER_LINE_MIN_LENGTH
				&& line_aspect_ratio < HOR_VER_LINE_MAX_ASPECT_RATIO;
	}

	public boolean lineCurvatureTooHigh(HwrStroke stroke) {
		
		HwrStroke noDuplicates = HwrAlgorithms.removeDuplicates(stroke);
		HwrStroke resampled = HwrAlgorithms.resample(noDuplicates, NUM_CURVATURE_MEASUREMENT_POINTS+2+CURVATURE_IGNORE_N_FIRST_POINTS+CURVATURE_IGNORE_N_LAST_POINTS);
		
		
		float x0, y0, x1, y1, x2, y2;
		float k1, k2;
		
		float curvature;
		float max_curvature = 0.0f;
		for (int i = 0+CURVATURE_IGNORE_N_FIRST_POINTS; i < resampled.points.size()-2-CURVATURE_IGNORE_N_LAST_POINTS; i++) {
			x0 = resampled.points.get(i).x;
			y0 = resampled.points.get(i).y;
			x1 = resampled.points.get(i+1).x;
			y1 = resampled.points.get(i+1).y;
			x2 = resampled.points.get(i+2).x;
			y2 = resampled.points.get(i+2).y;
			
			k1 = (float) Math.atan2(y1-y0, x1-x0);
			k2 = (float) Math.atan2(y2-y1, x2-x1);
			
			
			curvature = Math.abs(k2-k1);
			
			
			if(curvature > LINE_CURVATURE_MAX)
				return true;
			
			if (curvature > max_curvature)
				max_curvature = curvature;
		}
		
		Log.d("curvature", "max_curvature = " + max_curvature);
		
		
		return false;
	}

	public boolean isOnLowerDrawLineOrBelow(HwrStroke stroke) {
		RectF bbox = stroke.getRect();
		return bbox.top / mCanvasHeight > (LOWER_DRAWLINE_Y - 0.25 * (LOWER_DRAWLINE_Y - UPPER_DRAWLINE_Y));
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

		if (normalized_width < COMMA_WIDTH_MAX
				&& normalized_height < COMMA_HEIGHT_MAX) {
			return true;
		}
		return false;

	}

	public static HwrStroke getLowerFromStrokes(HwrStroke left, HwrStroke right) {
		// y grows down
		if (left.getLowest().y > right.getLowest().y) {
			return left;
		} else {
			return right;
		}
	}

	public HwrStroke getHorizontalFromStrokes(
			ArrayList<HwrStroke> strokes) {
		
		for (HwrStroke s : strokes) {
			if (isHorizontalLine(s))
				return s;
		}
		return null;
	}

	public HwrStroke getVerticalFromStrokes(
			ArrayList<HwrStroke> strokes) {
		
		for (HwrStroke s : strokes) {
			if (isVerticalLine(s))
				return s;
		}
		return null;
	}

	public HwrStroke popHorizontalFromStrokes(
			ArrayList<HwrStroke> strokes) {
		
		for (int i = 0; i < strokes.size(); i++) {
			if (isHorizontalLine(strokes.get(i))){
				return strokes.remove(i);
			}
		}
		return null;
	}

	public HwrStroke popVerticalFromStrokes(
			ArrayList<HwrStroke> strokes) {
		
		for (int i = 0; i < strokes.size(); i++) {
			if (isVerticalLine(strokes.get(i))){
				return strokes.remove(i);
			}
		}
		return null;
	}

	public static HwrStroke getLeftMostFromStrokes(HwrStroke left,
			HwrStroke right) {
		return left.getLeftMost().x <= right.getLeftMost().x ? left : right;
	}

	public static HwrStroke getRightMostFromStrokes(HwrStroke left,
			HwrStroke right) {
		return left.getRightMost().x > right.getRightMost().x ? left : right;
	}

	public static HwrStroke getUpperFromStrokes(HwrStroke left, HwrStroke right) {
		// y grows down
		if (left.getUpmost().y <= right.getUpmost().y) {
			return left;
		} else {
			return right;
		}
	}

	public static PointF getCrossingPointForHorAndVerStroke(HwrStroke hor,
			HwrStroke ver) {
		return new PointF(ver.getMeanX(), hor.getMeanY());
	}

	public boolean is_t(HwrStroke stroke1, HwrStroke stroke2) {
		Log.d(TAG, "is_t()");

		HwrStroke ver = getLowerFromStrokes(stroke1, stroke2);
		HwrStroke hor = getLeftMostFromStrokes(stroke1, stroke2);

		if (!isVerticalLine(ver) || !isHorizontalLine(hor)) {
			Log.d(TAG, "ver : isVerticalLine() = " + isVerticalLine(ver));
			Log.d(TAG, "hor : isHorizontalLine() = " + isHorizontalLine(hor));
			return false;
		}

		PointF crossing_point = getCrossingPointForHorAndVerStroke(hor, ver);
		Log.d(TAG, "hor : crossing_point.y = " + crossing_point.y);
		Log.d(TAG, "hor : ver.getMeanY() = " + ver.getMeanY());
		return crossing_point.y < ver.getMeanY()
				//&& (crossing_point.y - ver.getUpmost().y) / mCanvasHeight > T_HOR_LINE_TOO_HIGH
				&& goesBeyondAboveOrAtLeastClose(ver, hor)
				&& hor.getRightMost().x > ver.getRightMost().x;
	}

	public boolean is_x(HwrStroke stroke1, HwrStroke stroke2) {
		Log.d(TAG, "is_x()");
		if (isDiagonalLine(stroke1, true) == -1.0f) {
			if (isDiagonalLine(stroke2, true) == 1.0f) {
				if (Math.abs(stroke1.getMeanX() - stroke2.getMeanX())
						/ mCanvasWidth < X_DIAGONAL_LINES_XMEAN_DIFF) {
					return true;
				}
			}
		}
		if (isDiagonalLine(stroke2, true) == -1.0f) {
			if (isDiagonalLine(stroke1, true) == 1.0f) {
				if (Math.abs(stroke1.getMeanX() - stroke2.getMeanX())
						/ mCanvasWidth < X_DIAGONAL_LINES_XMEAN_DIFF) {
					return true;
				}
			}
		}

		Log.d(TAG, "(isDiagonalLine(stroke1, true) == -1.0f) = "
				+ (isDiagonalLine(stroke1, true)));
		Log.d(TAG, "(isDiagonalLine(stroke2, true) == 1.0f) = "
				+ (isDiagonalLine(stroke2, true)));
		Log.d(TAG,
				"(Math.abs(stroke1.getMeanX() - stroke2.getMeanX())/mCanvasWidth = "
						+ Math.abs(stroke1.getMeanX() - stroke2.getMeanX())
						/ mCanvasWidth);
		return false;
	}
	
	public boolean goesBeyondLeftOrAtLeastClose(HwrStroke goes_left, HwrStroke test_stroke) {
		return (test_stroke.getLeftMost().x-goes_left.getLeftMost().x)/mCanvasWidth > GOES_BEYOND_OR_IS_CLOSE_LIMIT;
	}
	public boolean goesBeyondRightOrAtLeastClose(HwrStroke goes_right, HwrStroke test_stroke) {
		return (goes_right.getRightMost().x-test_stroke.getRightMost().x)/mCanvasWidth > GOES_BEYOND_OR_IS_CLOSE_LIMIT;
	}
	public boolean goesBeyondAboveOrAtLeastClose(HwrStroke goes_up, HwrStroke test_stroke) {
		return (test_stroke.getUpmost().y-goes_up.getUpmost().y)/mCanvasHeight > GOES_BEYOND_OR_IS_CLOSE_LIMIT;
	}
	public boolean goesBeyondBelowOrAtLeastClose(HwrStroke goes_below, HwrStroke test_stroke) {
		return (goes_below.getLowest().y-test_stroke.getLowest().y)/mCanvasHeight > GOES_BEYOND_OR_IS_CLOSE_LIMIT;
	}

}
