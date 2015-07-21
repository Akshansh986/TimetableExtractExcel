package com.example.excel;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SubCodesStore {
	public static final String TAG = "SubCodesSTore";

	List<SubCodePair> list;
	Pattern p1;
	Context context;

	public SubCodesStore(Context context) {
		list = new ArrayList<SubCodePair>();
		this.context = context;
		p1 = Pattern.compile("\\w+");
		StoreCodes();
		createFullSubjectCodeFile();
	}
	
	public String getFullCode(String shortCode) {
		shortCode = shortCode.trim().toUpperCase();
		for (SubCodePair sc : list) {
			if (sc.shortCode.equals(shortCode)) return sc.fullCode;
		}
		return shortCode;
	}

	private void StoreCodes() {
		try {
			InputStream inputStream = context.getResources().openRawResource(R.raw.subcode);
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

			String line;
			while (true) {
				line = br.readLine();
				if (line == null)
					break;
				SubCodePair a = extractCode(line);
				if (a!=null) list.add(a); 
			}
			br.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private SubCodePair extractCode(String line) {
		Log.d(TAG, "line : " + line);
		SubCodePair sc = new SubCodePair();
		Matcher matcher = p1.matcher(line);
		if (matcher.find())
			sc.shortCode = line.substring(matcher.start(), matcher.end()).toUpperCase().trim();
		else {
			Log.e(TAG, "Error in subcodes file");
			return null;
		}

		if (matcher.find())
			sc.fullCode = line.substring(matcher.start(), matcher.end()).toUpperCase().trim();
		else {
			Log.e(TAG, "Error in subcodes file 2");
			return null;
		}
		return sc;

	}
	
	
	public void createFullSubjectCodeFile()
	{       
		String str="";
		for (SubCodePair sc : list) {
			str = str + sc.fullCode + "-";
		}
		String PREFS_NAME = "MyPrefsFile";
		SharedPreferences settings;
		settings = context.getSharedPreferences(PREFS_NAME, 0);
		settings.edit().putString("SubCodes", str).commit();

	}

	class SubCodePair {
		String shortCode;
		String fullCode;
	}
}
