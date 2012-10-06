package com.jsillanpaa.drawinput.hwr;

import java.util.List;

import android.util.FloatMath;

public class MyUtils {

	
	public static int arraySum(int[] array){
		int sum = 0;
		for (int i = 0; i < array.length; i++) {
			sum += array[i];
		}
		return sum;
	}
	
	public static int listSum(List<Integer> list){
		int sum = 0;
		for (int n : list) {
			sum += n;
		}
		return sum;
	}

	public static float getEuclideanDistance(float[] v1, float[] v2) {
		float sum = 0.0f;
		for (int i = 0; i < v1.length; i++) {
			sum += (v1[i]-v2[i])*(v1[i]-v2[i]);
		}
		return FloatMath.sqrt(sum);
	}
	
	public static float getEuclideanDistanceNoSqrt(float[] v1, float[] v2) {
		if(v1.length != v2.length)
			throw new IllegalArgumentException("vectors have different lengths! v1="+v1.length+" and v2="+v2.length);
		
		float sum = 0.0f;
		for (int i = 0; i < v1.length; i++) {
			sum += (v1[i]-v2[i])*(v1[i]-v2[i]);
		}
		return sum;
	}
	
	
}
