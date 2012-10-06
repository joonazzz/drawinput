package com.jsillanpaa.drawinput.hwr;

import java.io.Serializable;
import java.util.ArrayList;

import android.graphics.PointF;

public class HwrStroke implements Serializable{

	private static final long serialVersionUID = 2191789130981378972L;
	public ArrayList<PointF> points;
	
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
			if( points.size() != other.points.size() ){
				return false;
			}
			for (int i = 0; i < points.size(); i++) {
				PointF myPoint = points.get(i);
				PointF otherPoint = other.points.get(i);
				if( myPoint.x != otherPoint.x || myPoint.y != otherPoint.y){
					return false;
				}
			}
		} catch (Exception e) {
			/* Goes here if:
			 * 1. other==null
			 * 2. other is not HwrCharacter
			 * */
			return false;
		}
		return true;
	}
}
