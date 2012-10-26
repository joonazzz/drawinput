package com.jsillanpaa.drawinput.hwr;

import java.io.Serializable;
import java.util.ArrayList;

import android.graphics.PointF;
import android.graphics.RectF;

public class HwrStroke implements Serializable {

	private static final long serialVersionUID = 2191789130981378972L;

	public ArrayList<PointF> points;

	private RectF mRect;

	private PointF mLeftMost;
	private PointF mRightMost;

	private PointF mLowest;
	private PointF mUpmost;

	public HwrStroke() {
		points = new ArrayList<PointF>();
	}

	public HwrStroke(int target_count) {
		points = new ArrayList<PointF>(target_count);
	}

	@Override
	public boolean equals(Object obj) {
		try {
			HwrStroke other = (HwrStroke) obj;
			if (points.size() != other.points.size()) {
				return false;
			}
			for (int i = 0; i < points.size(); i++) {
				PointF myPoint = points.get(i);
				PointF otherPoint = other.points.get(i);
				if (myPoint.x != otherPoint.x || myPoint.y != otherPoint.y) {
					return false;
				}
			}
		} catch (Exception e) {
			/*
			 * Goes here if: 1. other==null 2. other is not HwrCharacter
			 */
			return false;
		}
		return true;
	}

	public RectF getRect() {
		if (mRect == null) {
			mRect = new RectF(Float.MAX_VALUE, Float.MAX_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
			// remember y grows down
			for (PointF p : points) {
				if (p.x < mRect.left)
					mRect.left = p.x;
				if (p.x > mRect.right)
					mRect.right = p.x;
				if (p.y < mRect.top)
					mRect.top = p.y;
				if (p.y > mRect.bottom)
					mRect.bottom = p.y;
			}
		}
		return mRect;
	}

	public float getMeanX() {
		float mean = 0.0f;
		for (PointF p : points) {
			mean += p.x;
		}
		return mean / points.size();
	}
	public float getMeanY() {
		float mean = 0.0f;
		for (PointF p : points) {
			mean += p.y;
		}
		return mean / points.size();
	}

	public PointF getLeftMost() {
		if(mLeftMost == null){
			for (PointF p : points) {
				if(mLeftMost == null || p.x < mLeftMost.x){
					mLeftMost = p;
				}
			}
		}

		return mLeftMost;
	}
	public PointF getRightMost() {
		if(mRightMost == null){
			for (PointF p : points) {
				if(mRightMost == null || p.x > mRightMost.x){
					mRightMost = p;
				}
			}
		}
		return mRightMost;
	}

	public PointF getLowest() {
		if(mLowest == null){
			for (PointF p : points) {
				if(mLowest == null || p.y > mLowest.y){ // y grows down
					mLowest = p;
				}
			}
		}
		return mLowest;
	}
	
	public PointF getUpmost() {
		if(mUpmost == null){
			for (PointF p : points) {
				if(mUpmost == null || p.y < mUpmost.y){ // y grows down
					mUpmost = p;
				}
			}
		}
		return mUpmost;
	}

	public HwrStroke[] splitMiddle() {
		HwrStroke[] strokes = new HwrStroke[2];
		strokes[0] = new HwrStroke();
		strokes[1] = new HwrStroke();
		int half_points = points.size()/2;
		for (PointF p : points) {
			if(strokes[0].points.size() < half_points)
				strokes[0].points.add(p);
			else
				strokes[1].points.add(p);
		}
		return strokes;
	}

	public float getHeight() {
		PointF lowest = getLowest();
		PointF upmost = getUpmost();
		return lowest.y - upmost.y; // y grows down
	}

	public float getWidth() {
		PointF leftmost = getLeftMost();
		PointF rightmost = getRightMost();
		return rightmost.x - leftmost.x; 
	}

}
