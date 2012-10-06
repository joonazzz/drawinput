package com.jsillanpaa.drawinput.hwr;

import java.io.Serializable;

public class HwrScaler implements Serializable{
	private static final long serialVersionUID = -995819117466432255L;
	
	private float[] mFeature_means;
	private float[] mFeature_stds;
	
	public HwrScaler(float[][] feature_vectors) {
		int n_vectors = feature_vectors.length;
		int n_features = feature_vectors[0].length;
		
		mFeature_means = new float[n_features];
		mFeature_stds = new float[n_features];
		
		for (int i = 0; i < n_features; i++) {
			float[] feature_samples = new float[n_vectors];
			for (int j = 0; j < feature_samples.length; j++) {
				feature_samples[j] = feature_vectors[j][i];
			}
			float feature_mean = HwrAlgorithms.mean(feature_samples);
			float feature_std = HwrAlgorithms.std(feature_samples, feature_mean);
			mFeature_means[i] = feature_mean;
			mFeature_stds[i] = feature_std;
		}
		
	}
	
	
	public void normalize(float[] feature_vec){
		for (int i = 0; i < feature_vec.length; i++) {
			feature_vec[i] = (feature_vec[i] - mFeature_means[i])/mFeature_stds[i];
		}
	}
	
	public void normalize(float[][] feature_vectors) {
		for (int i = 0; i < feature_vectors.length; i++) {
			normalize(feature_vectors[i]);
		}
		
	}
}
