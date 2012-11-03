package com.jsillanpaa.drawinput.hwr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import android.graphics.PointF;
import android.util.FloatMath;

import com.jsillanpaa.drawinput.char_recognizers.CharRecognitionResult;


//import com.joonas.sillanpaa.drawinput.MyUtils;

//import android.graphics.PointF;
//import android.util.FloatMath;

/**
 * This class will hold public static classifier algorithms.
 * 
 * @author joonas
 * 
 */
public class HwrAlgorithms {

	public static class BoundingBox {
		float mXmin;
		float mXmax;
		float mYmin;
		float mYmax;

		public BoundingBox() {
			this(0.0f, 0.0f, 0.0f, 0.0f);
		}

		public BoundingBox(float x_min, float y_min, float x_max, float y_max) {
			mXmin = x_min;
			mXmax = x_max;
			mYmin = y_min;
			mYmax = y_max;
		}
	}

	public static final int TARGET_SAMPLE_COUNT = 20;
	public static final int N_FEATURES = 4;
	public static final int FEATURE_VECTOR_LENGTH = TARGET_SAMPLE_COUNT
			* N_FEATURES;

	public static final boolean DEBUG = false;

	public static HwrCharacter preProcessChar(HwrCharacter ch) {
		ch = removeDuplicates(ch);
		ch = resample(ch);
		ch = normalize(ch);
		return ch;
	}

	public static ArrayList<HwrCharacter> preProcessChars(
			ArrayList<HwrCharacter> mCharacters) {
		ArrayList<HwrCharacter> preprocessed_chars = new ArrayList<HwrCharacter>();
		for (HwrCharacter ch : mCharacters) {
			preprocessed_chars.add(preProcessChar(ch));
		}

		return preprocessed_chars;
	}

	public static HwrStroke removeDuplicates(HwrStroke stroke) {
		
		if(stroke.points.size() < 1)
			return stroke;
		
		HwrStroke filteredStroke = new HwrStroke(stroke.points.size());
		PointF prev = stroke.points.get(0);
		filteredStroke.points.add(prev);
		for (PointF p : stroke.points) {
			if (p.x != prev.x || p.y != prev.y) {
				filteredStroke.points.add(p);
				prev = p;
			}
		}
		return filteredStroke;
	}
	
	public static HwrCharacter removeDuplicates(HwrCharacter ch) {

		HwrCharacter filtered_ch = new HwrCharacter(ch.label);

		for (HwrStroke stroke : ch.strokes) {
			HwrStroke filtered_stroke = removeDuplicates(stroke); 
			if (filtered_stroke.points.size() > 0) {
				filtered_ch.strokes.add(filtered_stroke);
			}
		}

		return filtered_ch;
	}

	public static HwrStroke resample(HwrStroke old_stroke, int target_count) {
		
		HwrStroke new_stroke = new HwrStroke(target_count);
		
		/* First handle all special cases... */
		if (target_count == 0) {
			;
		} else if (old_stroke.points.size() == 1) {
			for (int j = 0; j < target_count; j++) {
				new_stroke.points.add(old_stroke.points.get(0));
			}
		} else if (target_count == 1) {
			PointF first = old_stroke.points.get(0);
			PointF last = old_stroke.points
					.get(old_stroke.points.size() - 1);
			PointF new_point = new PointF(0.5f * first.x + 0.5f * last.x,
					0.5f * first.y + 0.5f * last.y);
			new_stroke.points.add(new_point);
		} else if (target_count == 2) {
			PointF first = old_stroke.points.get(0);
			PointF last = old_stroke.points
					.get(old_stroke.points.size() - 1);
			new_stroke.points.add(first);
			new_stroke.points.add(last);
		}
		/*
		 * The "normal" case. Assumes old has at least two points, and that
		 * target count is at least 3.
		 */
		else {
			float index_step = (float) (old_stroke.points.size() - 1)
					/ (target_count - 1);
			for (int j = 0; j < target_count; j++) {
				float index_now = j * index_step;
				int lower_index = (int) index_now;
				int upper_index = lower_index + 1;
				float upper_weight = index_now - lower_index;
				float lower_weight = 1.0f - upper_weight;

				if (upper_index > old_stroke.points.size() - 1) {
					upper_index = lower_index;
					upper_weight = 0.0f;
					lower_weight = 1.0f;
				}

				float new_x = old_stroke.points.get(lower_index).x
						* lower_weight
						+ old_stroke.points.get(upper_index).x
						* upper_weight;
				float new_y = old_stroke.points.get(lower_index).y
						* lower_weight
						+ old_stroke.points.get(upper_index).y
						* upper_weight;
				new_stroke.points.add(new PointF(new_x, new_y));

			}
		}
		
		return new_stroke;
		
	}
	/**
	 * Resamples strokes to TARGET_SAMPLE_COUNT.
	 * 
	 * @param strokes
	 *            strokes to be resampled
	 * @return resampled strokes
	 */
	public static HwrCharacter resample(HwrCharacter ch) {
		int nStrokes = ch.strokes.size();
		int nSamples = 0;
		for (HwrStroke stroke : ch.strokes) {
			nSamples += stroke.points.size();
		}

		float[] target_sample_counts = new float[nStrokes];
		int[] allocated_sample_counts = new int[nStrokes];

		/* Helper class for array list and sorting of index-to-delta mappings. */
		final class IndexAndFloat {
			int mIndex;
			float mFloat;

			public IndexAndFloat(int index, float f) {
				mIndex = index;
				mFloat = f;
			}
		}
		ArrayList<IndexAndFloat> delta_map = new ArrayList<IndexAndFloat>();

		for (int i = 0; i < nStrokes; i++) {
			target_sample_counts[i] = (ch.strokes.get(i).points.size())
					/ (float) (nSamples) * TARGET_SAMPLE_COUNT;
			allocated_sample_counts[i] = (int) target_sample_counts[i];
			float delta = target_sample_counts[i] - allocated_sample_counts[i];
			delta_map.add(new IndexAndFloat(i, delta));
		}

		/*
		 * Sort the deltas so we can allocate additional samples to those with
		 * biggest deltas.
		 */
		Collections.sort(delta_map, new Comparator<IndexAndFloat>() {
			@Override
			public int compare(IndexAndFloat lhs, IndexAndFloat rhs) {
				if (lhs.mFloat >= rhs.mFloat) {
					return -1;
				} else
					return 1;
			}
		});

		/* Allocation of extra samples. */
		int nExtraSamples = TARGET_SAMPLE_COUNT
				- arraySum(allocated_sample_counts);

		for (int i = 0; i < nExtraSamples; i++) {
			int who_gets_extra_index = delta_map.get(i).mIndex;
			allocated_sample_counts[who_gets_extra_index] += 1;
		}

		if(DEBUG){
			for (int i = 0; i < ch.strokes.size(); i++) {
				System.out.println("  Stroke " + i + ": original count: "
						+ ch.strokes.get(i).points.size());
				System.out.println("  Stroke " + i + ": new count: "
						+ allocated_sample_counts[i]);
			}	
		}
		

		/* Resample all strokes to their target sample counts. */
		HwrCharacter new_ch = new HwrCharacter(ch.label, ch.strokes.size());

		for (int i = 0; i < ch.strokes.size(); i++) {
			int target_count = allocated_sample_counts[i];
			HwrStroke old_stroke = ch.strokes.get(i);
			HwrStroke new_stroke = resample(old_stroke, target_count);
		
			/* Finally add to stroke list if we got meaningful stroke. */
			if (new_stroke.points.size() > 0)
				new_ch.strokes.add(new_stroke);
		}

		if (DEBUG) {
			System.out.println("*** Old strokes and points ***");
			for (HwrStroke stroke : ch.strokes) {
				for (PointF p : stroke.points) {
					System.out.println("x = " + p.x + " , y = " + p.y);
				}
			}
			System.out.println("*** New strokes and points ***");
			for (HwrStroke hmm : new_ch.strokes) {
				for (PointF p : hmm.points) {
					System.out.println("x = " + p.x + " , y = " + p.y);
				}
			}
		}

		return new_ch;
	}

	public static HwrCharacter normalize(HwrCharacter ch) {
		HwrCharacter new_ch = new HwrCharacter(ch.label, ch.strokes.size());

		BoundingBox bbox = getBoundingBox(ch.strokes);
		float W_0 = Math.max(1.0f, bbox.mXmax - bbox.mXmin);
		float H_0 = Math.max(1.0f, bbox.mYmax - bbox.mYmin);
		float W_1;
		float H_1;
		if (H_0 >= W_0) {
			H_1 = 1.0f;
			W_1 = 1.0f * (W_0 / H_0);
		} else {
			W_1 = 1.0f;
			H_1 = 1.0f * (H_0 / W_0);
		}

		float half_free_space_x = (1.0f - W_1) / 2.0f;
		float half_free_space_y = (1.0f - H_1) / 2.0f;

		for (HwrStroke stroke : ch.strokes) {
			HwrStroke new_stroke = new HwrStroke(stroke.points.size());
			for (PointF p : stroke.points) {
				float new_x = half_free_space_x + (p.x - bbox.mXmin) / W_0
						* W_1;
				float new_y = half_free_space_y + (p.y - bbox.mYmin) / H_0
						* H_1;
				new_stroke.points.add(new PointF(new_x, new_y));
			}
			new_ch.strokes.add(new_stroke);
		}

		if (DEBUG) {
			System.out.println("*** Normalised character ***");
			for (HwrStroke stroke : new_ch.strokes) {
				System.out.println("  Stroke  ");
				for (PointF p : stroke.points) {
					System.out.println("x = " + p.x + " , y = " + p.y);
				}
			}
		}

		return new_ch;

	}

	public static BoundingBox getBoundingBox(List<HwrStroke> ch_strokes) {
		float x_min = Float.MAX_VALUE;
		float y_min = Float.MAX_VALUE;
		float x_max = Float.MIN_VALUE;
		float y_max = Float.MIN_VALUE;

		for (HwrStroke stroke : ch_strokes) {
			for (PointF p : stroke.points) {
				if (p.x < x_min)
					x_min = p.x;
				if (p.x > x_max)
					x_max = p.x;
				if (p.y < y_min)
					y_min = p.y;
				if (p.y > y_max)
					y_max = p.y;

			}
		}

		return new BoundingBox(x_min, y_min, x_max, y_max);

	}

	public static float[] extractFeatures(HwrCharacter ch) {

		assert ch.getNumberOfPoints() + 1 == TARGET_SAMPLE_COUNT;

		ArrayList<HwrStroke> strokes = ch.strokes;

		PointF cur_p;
		float dx, dy, r;

		float[] feature_vec = new float[TARGET_SAMPLE_COUNT * N_FEATURES];
		int i = 0;
		for (HwrStroke stroke : strokes) {
			if (stroke.points.size() == 1) {
				feature_vec[i++] = stroke.points.get(0).x;
				feature_vec[i++] = stroke.points.get(0).y;
				feature_vec[i++] = 0.0f;
				feature_vec[i++] = 0.0f;
				continue;
			}
			for (int j = 0; j < stroke.points.size(); j++) {

				if (j == 0) {
					cur_p = stroke.points.get(j);
					PointF next_p = stroke.points.get(j + 1);

					dx = next_p.x - cur_p.x;
					dy = next_p.y - cur_p.y;
				} else if (j == stroke.points.size() - 1) {
					cur_p = stroke.points.get(j);
					PointF prev_p = stroke.points.get(j - 1);

					dx = cur_p.x - prev_p.x;
					dy = cur_p.y - prev_p.y;
				} else {
					/* There must be previous and next point. */
					PointF prev_p = stroke.points.get(j - 1);
					cur_p = stroke.points.get(j);
					PointF next_p = stroke.points.get(j + 1);

					dx = next_p.x - prev_p.x;
					dy = next_p.y - prev_p.y;

				}

				r = FloatMath.sqrt(dx * dx + dy * dy);
				// r = (float) FloatMath.sqrt(dx * dx + dy * dy);
				feature_vec[i++] = cur_p.x;
				feature_vec[i++] = cur_p.y;
				feature_vec[i++] = (r == 0.0f ? 0.0f : dx / r); // cos(x)
				feature_vec[i++] = (r == 0.0f ? 0.0f : dy / r); // sin(x)
			}
		}
		if (DEBUG) {
			System.out.println("*** Feature fector ***");
			for (int j = 0; j < feature_vec.length; j += 4) {
				System.out.printf("x=%f, y=%f, cos(x)=%f, sin(x)=%f\n",
						feature_vec[j], feature_vec[j + 1], feature_vec[j + 2],
						feature_vec[j + 3]);
			}
		}
		return feature_vec;

	}

	public static float[][] extractFeatures(ArrayList<HwrCharacter> characters) {
		int rows = characters.size();
		int cols = FEATURE_VECTOR_LENGTH;
		float[][] feature_vectors = new float[rows][cols];

		for (int i = 0; i < rows; i++) {
			feature_vectors[i] = extractFeatures(characters.get(i));
		}



		return feature_vectors;
	}
	

	

	
	
	
	
	
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
		//return FloatMath.sqrt(sum);
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
	
	public static float mean(float[] vec){
		float mean = 0.0f;
		for (int i = 0; i < vec.length; i++) {
			mean += vec[i];
		}
		return mean / vec.length;
	}

	public static float std(float[] vec, Float mean_value) {
		if (vec.length < 2) {
			return 1.0f;
		}
		if (mean_value == null)
			mean_value = mean(vec);

		float std = 0.0f;
		for (int i = 0; i < vec.length; i++) {
			std += (vec[i] - mean_value) * (vec[i] - mean_value);
		}
		std = std / (vec.length - 1);
		std = FloatMath.sqrt(std);
		return std;
	}

	public static CharRecognitionResult predict(float[] feature_vector, svm_model model) {
		svm_node[] z = new svm_node[feature_vector.length];
		for (int i = 0; i < feature_vector.length; i++) {
			z[i] = new svm_node();
			z[i].index = i + 1;
			z[i].value = feature_vector[i];
		}
		double result = svm.svm_predict(model, z);
		return new CharRecognitionResult((char) result, 1.0f);
		
	}
}
