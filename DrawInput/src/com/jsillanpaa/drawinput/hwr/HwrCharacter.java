package com.jsillanpaa.drawinput.hwr;

import java.io.Serializable;
import java.util.ArrayList;

import com.jsillanpaa.drawinput.hwr.HwrStroke;

public class HwrCharacter implements Serializable{
	
	private static final long serialVersionUID = -1889268747482852300L;
	
	public ArrayList<HwrStroke> strokes;
	public char label;
	
	public HwrCharacter(char label_par) {
		strokes = new ArrayList<HwrStroke>();
		label = label_par;
	}

	public HwrCharacter(char label_par, int size) {
		strokes = new ArrayList<HwrStroke>(size);
		label = label_par;
	}
	
	public int getNumberOfPoints() {
		int n = 0;
		for (HwrStroke stroke : strokes) {
			n += stroke.points.size();
		}
		return n;
	}
	
	@Override
	public boolean equals(Object obj) {
		try {
			HwrCharacter other = (HwrCharacter) obj;
			if( strokes.size() != other.strokes.size() ){
				return false;
			}
			for (int i = 0; i < strokes.size(); i++) {
				HwrStroke myStroke = strokes.get(i);
				HwrStroke otherStroke = other.strokes.get(i);
				if(myStroke.equals(otherStroke)==false){
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
	
	
	
} // eof HwrCharacter class