package com.jsillanpaa.drawinput.hwr;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import libsvm.svm;
import libsvm.svm_model;

import android.content.Context;

public class HwrTools {


	
	public static void saveObject(Object o, String fname) {
		try {
			// use buffering
			OutputStream file = new FileOutputStream(fname);
			OutputStream buffer = new BufferedOutputStream(file);
			ObjectOutput output = new ObjectOutputStream(buffer);
			try {
				output.writeObject(o);
			} finally {
				output.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}

	public static Object loadObject(InputStream is) {
		Object o = null;
		try {
			// use buffering
			InputStream buffer = new BufferedInputStream(is);
			ObjectInput input = new ObjectInputStream(buffer);
			try {
				o = input.readObject();
			} finally {
				input.close();
			}
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return o;
	}
	
	public static Object loadObject(String fname) {

		Object o = null;
		try {
			// use buffering
			InputStream file = new FileInputStream(fname);
			InputStream buffer = new BufferedInputStream(file);
			ObjectInput input = new ObjectInputStream(buffer);
			try {
				o = input.readObject();

			} finally {
				input.close();
			}
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return o;
	}

	public static void saveObjectGZIP(Object o, String fname) throws IOException {
		
		FileOutputStream fos = new FileOutputStream(fname);
		GZIPOutputStream gz = new GZIPOutputStream(fos);
		ObjectOutput oos = new ObjectOutputStream(gz);

		oos.writeObject(o);
		oos.flush();
		oos.close();
		fos.close();

	}

	public static Object loadObjectGZIP(String fname) throws IOException, ClassNotFoundException {
		Object o = null;
		FileInputStream fis = new FileInputStream(fname);
		GZIPInputStream gs = new GZIPInputStream(fis);
		ObjectInputStream ois = new ObjectInputStream(gs);
		o = ois.readObject();
		ois.close();
		fis.close();
		return o;
	}
	
	public static Object loadObjectGZIP(InputStream is) throws IOException, ClassNotFoundException {
		Object o = null;
		GZIPInputStream gs = new GZIPInputStream(is);
		ObjectInputStream ois = new ObjectInputStream(gs);
		o = ois.readObject();
		ois.close();
		is.close();
		return o;
	}
	
	
	
	public static void saveSvmLight(String fname, char[] labels, float[][] featureVectors) {
		BufferedWriter bw;
		StringBuffer line_buf;
		
		try {
			bw = new BufferedWriter( new FileWriter(fname) );
			for (int i = 0; i < labels.length; i++) {
				line_buf = new StringBuffer();
				line_buf.append(""+labels[i]);
				float[] featureVec = featureVectors[i];
				for (int j = 0; j < featureVec.length; j++) {
					line_buf.append(" "+j+":"+featureVec[j]);
				}
				if( line_buf.indexOf("NaN") < 0){
					line_buf.append("\n");
					bw.write(line_buf.toString());
				}
			}
			bw.flush(); /* Some rows don't get to file without this!*/
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}



	public static void saveSvmLight(String fname,
			ArrayList<HwrCharacter> hwr_chars, float[][] featureVectors) {
		char[] labels = new char[hwr_chars.size()];
		for (int i = 0; i < hwr_chars.size(); i++) {
			labels[i] = hwr_chars.get(i).label;
		}
		saveSvmLight(fname, labels, featureVectors);
	}

	public static void saveModelAsSvmText(svm_model mSvmModel, String fname) throws IOException {
		svm.svm_save_model(fname, mSvmModel);
		
	}
	
	public static svm_model loadModelFromSvmText(InputStream is) throws IOException{
		BufferedReader br = new BufferedReader( new InputStreamReader(is));
		return svm.svm_load_model(br);
	}

	public static void listAssets(Context context) {
		String[] files;
		try {
			files = context.getAssets().list("");
			for (int i = 0; i < files.length; i++) {
				System.out.println("Asset: " + files[i]);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
