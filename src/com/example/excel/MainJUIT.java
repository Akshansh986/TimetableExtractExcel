package com.example.excel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class MainJUIT extends Activity {
	
	int X_START = 2;
	int Y_START = 3;
	int X_END = 10;
	int Y_END = 100;

	final String INPUT_FILENAME = "juit.xls";
	final String OUTPUT_FILE_NAME = "a4";
	private static final int SHEET = 1;
	

	final String OUTPUT_DB_NAME = OUTPUT_FILE_NAME + ".db";
	final String OUTPUT_TEXT_NAME = OUTPUT_FILE_NAME + ".txt";
	//FULL SUBJECT CODE FILE IS PRESENT IN RAW FOLDER.

	public static final String C_DAY = "day";

	public static final int MON = 1;
	public static final int TUE = 2;
	public static final int WED = 3;
	public static final int THU = 4;
	public static final int FRI = 5;
	public static final int SAT = 6;

	// int yskip1 = -2;
	
	static final String TAG = "EXCEL_MAIN";
	public static final int DB_VERSION = 1;

	DbHelper dbHelper;
	List<String> overallBatchList = new ArrayList<String>();
	SubCodesStore subCodeStore;

	boolean pause = false;
	List<String> subCodeList = new ArrayList<String>();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		subCodeStore = new SubCodesStore(this);
		setContentView(R.layout.activity_main);

		X_START -= 1;
		Y_START -= 1;
		X_END -= 1;
		Y_END -= 1;
		deleteDatabase();

		dbHelper = new DbHelper(this);
		dbHelper.getWritableDatabase();

		// Log.d(TAG," " + getExternalStorageDirectory()
		// .getAbsolutePath());
		readCell();

		dbHelper.close();

	}

	private void writeSubCodeToText() {
	      
		   File logFile = new File("sdcard/"+ OUTPUT_TEXT_NAME);
		   if (!logFile.exists())
		   {
		      try
		      {
		         logFile.createNewFile();
		      } 
		      catch (IOException e)
		      {
		         // TODO Auto-generated catch block
		         e.printStackTrace();
		      }
		   }
		   try
		   {
		      //BufferedWriter for performance, true to set append to file flag
		      BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 
		     
		      String text = "";
		      for (String subCode : subCodeList) {
		    	 text+=subCode + "-";
		     }
		      text+="$" + OUTPUT_TEXT_NAME;
		      buf.append(text);
		      buf.close();
		   }
		   catch (IOException e)
		   {
		      // TODO Auto-generated catch block
		      e.printStackTrace();
		   }
		
	}

	private void readCell() {

		new Thread() {
			public void run() {
				try {
					Sheet sheet = initWorkbook();

					for (int j = Y_START; j <= Y_END; ++j) {
						for (int i = X_START; i <= X_END; ++i) {

							String tmp = sheet.getCell(i, j).getContents();
							tmp = tmp.toUpperCase().trim();

							if (tmp != "") {

								int d = getDay(sheet, j);
								Log.i(TAG, "cell (" + i + "," + j + ") : "
										+ tmp + " day : " + d);
								String previous = tmp;
								if (!verifyInititalFormat(tmp))
									continue;
								Log.d(TAG, "is tmp changed : " + tmp);
								manage(tmp, i - X_START + 1, d);

								while (pause) {
									setEditboxText(tmp);

									while (pause) {
									}
									previous = tmp;
									tmp = getEditboxText();
									if (!tmp.equals(previous))
										manage(tmp, i - X_START + 1, d);
								}

							}

						}

					}
					writeSubCodeToText();

					Log.d(TAG, "done!!!");

				} catch (Exception e) {
					Log.d(TAG, "error!!!!!!");

					e.printStackTrace();
				}

			}

		}.start();

	}

	private boolean verifyInititalFormat(String str) {
		boolean result = true;
		if (!matchPattern(str)) {
			pause = true;
		}

		while (pause) {
			setEditboxText(str);

			while (pause) {
			}

			String previous = str;
			str = getEditboxText();
			if (!str.equalsIgnoreCase(previous)) {
				if (!matchPattern(str))
					pause = true;
			} else
				result = false;
		}

		return result;
	}

	private boolean matchPattern(String str) {
		// TODO Auto-generated method stub
		if (str.contains("(")) return true;
		else return false;
		/*String tmp = str.replace('(', '@');
		tmp = tmp.replace(')', '#');
		Pattern p1 = Pattern
				"[LTP]-([A-Z0-9])+[
				.compile("(([A-Z0-9]|[,-])+)@(\\w+)#(([A-Z0-9]|[/\\,-])+)");
		Matcher matcher = p1.matcher(tmp);
		if (matcher.find())
			return true;
		else
			return false;*/
	}

	private int getDay(Sheet sheet, int index) {
		int day = Calendar.SUNDAY;
		for (int j = Y_START; j <= Y_END; ++j) {
			if (sheet.getCell(X_START - 1, j).getContents().trim() != "")
				++day;
			if (j == index)
				return day;
		}
		return 0;
	}
String prevCell="";
	private void manage(String str, int columnIndex, int day) {
		char classType;
		String subCode;
		String room;
		String teacherCode;

		String tmp;
		if (str.equals(prevCell)) return;
		else
			prevCell = str;
		try {

			classType = str.charAt(0);
			
			subCode = str.substring(str.indexOf('-') + 1, str.indexOf(" ")).trim();
			saveSubCode(subCode);
			//subCode = subCodeStore.getFullCode(subCode);
			/*if (str.contains(")-"))
				tmp = (str.substring(str.indexOf(")-") + 2));
			else
				tmp = (str.substring(str.indexOf(")") + 1));

			int i = 0;
			for (; i < tmp.length(); ++i) {
				if (tmp.charAt(i) == '/' || tmp.charAt(i) == ',')
					break;
			}*/
			room = str.substring(str.indexOf(")")+1).trim();	

			teacherCode = str.substring(str.indexOf('(') + 1, str.indexOf(')')).trim();

			tmp = str.substring(str.indexOf(' '), str.indexOf('('));
			tmp = tmp.trim();
			tmp = tmp.replaceAll("\\s", "");
			List<String> batch = new ArrayList<String>();
			extractBatch(tmp, batch);
			manageTable(batch, classType, subCode, room, teacherCode,
					columnIndex + 8, day);

		} catch (Exception e) {
			Log.e(TAG, "error string");
			e.printStackTrace();
		}

	}

	private void saveSubCode(String subCode) {
		if (!subCodeList.contains(subCode)) subCodeList.add(subCode);
		
	}

	private void manageTable(List<String> batchlist, char classType,
			String subCode, String room, String teacherCode,
			int databaseColumnIndex, int day) {

		if (verifyFormat(batchlist, classType, subCode, room, teacherCode)) {
			SQLiteDatabase db;
			db = dbHelper.getWritableDatabase();

			for (String batch : batchlist) {

				if (!doesTableExist(batch))
					createTable(batch, db);
				// inserting data into table
				String oldData = getOldData(String.valueOf(day), "c"+databaseColumnIndex, batch ,db);
				ContentValues values = new ContentValues();
				String data="";
				if (oldData != null) data = oldData+ "#";
				data = data + classType + "-" + subCode + "-" + room + "-"
						+ teacherCode;
				// if (!verifyFormat(classType, subCode, room, teacherCode))
				// Log.e(TAG, "Invalid format");
				values.put("c" + databaseColumnIndex, data);
				Log.d(TAG,
						"Batch " + batch + " " + data + " day : "
								+ String.valueOf(day));
				if (classType == 'P') 
				{
					values.put("c" + (databaseColumnIndex + 1), "same");
					
				}

				// if (classType == 'T' && subCode.equals("PD111"))
				// values.put("c" +
				// (databaseColumnIndex+1), "same");

				int a = db.update(batch, values,
						C_DAY + "='" + String.valueOf(day) + "'", null);
				if (a != 1)
					Log.e(TAG, "error a = " + a + " day " + String.valueOf(day));

			}
			db.close();
		} else {
			Log.e(TAG, "Invalid format");
			pause = true;
		}

	}

	private String getOldData(String row, String column,String table, SQLiteDatabase db) {
		String[] columns ={column};
		String result;
		
		Cursor cursor = db.query(table, columns, C_DAY + "='" + row + "'", null, null, null, null);

		if (cursor == null)
			result = null;
		else {
			if (cursor.getCount() == 0) {
				result = null;
				// Log.e(TAG, "Subject Changed");
			} else {
				cursor.moveToFirst();
				result = cursor.getString(cursor
						.getColumnIndex(column));
				
			}
		}
		cursor.close();
		
		return result;
	}

	private boolean verifyFormat(List<String> batchlist, char classType,
			String subCode, String room, String teacherCode) {
		boolean result = true;

		if (classType == 'L' || classType == 'T' || classType == 'P') {
		} else
			result = false;

		Pattern p1 = Pattern.compile("\\W");
		Matcher matcher;
		for (String batch : batchlist) {
			matcher = p1.matcher(batch);
			if (matcher.find())
				result = false;
		}

		matcher = p1.matcher(subCode);
		if (matcher.find())
			result = false;

		matcher = p1.matcher(room);
		if (matcher.find())
			result = false;

		return result;

	}

	private void createTable(String batch, SQLiteDatabase db) {
		String sql = String
				.format("create table %s"
						+ "(%s int primary key, %s text, %s text, %s text, %s text, %s text, %s text, %s text, %s text, %s text)",
						batch, C_DAY, "c9", "c10", "c11", "c12", "c13", "c14",
						"c15", "c16", "c17");
		// Log.d(TAG, "createTable  : " + sql);
		try {
			db.execSQL(sql);
			Log.e(TAG, batch + " created");
			overallBatchList.add(batch);
			initRow(batch, db);

		} catch (SQLException e) {
			Log.e(TAG, batch + " Table creation or insertion error");
			e.printStackTrace();
		}

	}

	private void initRow(String batch, SQLiteDatabase db) {

		for (int day = Calendar.MONDAY; day <= Calendar.SATURDAY; ++day) {
			ContentValues values = new ContentValues();
			values.put(C_DAY, day);

			String data = null;

			for (int i = 9; i <= 17; ++i) {
				// if (i == LUNCH_COLUMN_NAME)
				// data = "lunch";
				values.put("c" + i, data);
			}

			db.insert(batch, null, values);
			// db.insertWithOnConflict(batch, null, values,
			// SQLiteDatabase.CONFLICT_IGNORE);

		}
	}

	private boolean doesTableExist(String checkBatch) {
		for (String batch : overallBatchList) {
			if (batch.equals(checkBatch))
				return true;
		}
		return false;
	}

	private void extractBatch(final String str, List<String> batch) {
		Pattern p1 = Pattern.compile("[A-Z]([0-9]+)");

		Matcher matcher = p1.matcher(str);
		while (matcher.find()) {
			batch.add(str.substring(matcher.start(), matcher.end()));
			// Log.d("excel", str.substring(matcher.start(), matcher.end()));
		}

		p1 = Pattern.compile("(\\w{1,3})-(\\w{1,3})");
		matcher = p1.matcher(str);
		while (matcher.find()) {
			int start, end;
			String tmp = str.substring(matcher.start(), matcher.end());

			Pattern p2 = Pattern.compile("\\d+");

			Matcher matcher1 = p2.matcher(tmp);
			matcher1.find();
			start = Integer.parseInt(tmp.substring(matcher1.start(),
					matcher1.end()));
			matcher1.find();
			end = Integer.parseInt(tmp.substring(matcher1.start(),
					matcher1.end()));

			for (++start; start <= end; ++start) {
				if (!isAdded(batch,"" + tmp.charAt(0) + start))
				batch.add("" + tmp.charAt(0) + start);
				// Log.d(TAG, " " + tmp.charAt(0) + start);
			}

		}
		//p1 = Pattern.compile("([^-])[A-Z]([^0-9]|[^-])");
		String tmp = str;
		p1 = Pattern.compile("[A-Z]([0-9]|[-])");
		while(true) {
			matcher = p1.matcher(tmp);
			if ( !matcher.find()) break;
			String t = tmp.substring(matcher.start(), matcher.end());
			tmp = tmp.replace(t, "##");
		}
		Log.d(TAG, "TMP : " + tmp);

		p1 = Pattern.compile("[A-Z]");
		matcher = p1.matcher(tmp);

		while (matcher.find()) {
			char a = tmp.charAt(matcher.start());
			//tmp = tmp.substring(0, matcher.start() + 1) + "#" + tmp.substring(matcher.start() + 2);
			Log.d(TAG, "found : " + a);
			for (int i = 1; i < 15; ++i) {
				if (!isAdded(batch, "" + a + i)) {
					batch.add("" + a + i);
					Log.d(TAG, "added : " + "" + a + i);
					
				}
			}

		}

		/*
		 * for(int i=0 ; i<tmp.length() ; ++i) { if (tmp.charAt(i) == ',') {
		 * batch.add(tmp.substring(0, i)); tmp = tmp.substring(i+1); i=0; } }
		 */
	}

	private boolean isAdded(List<String> batch, String string) {
		for (int i = 0; i < batch.size(); ++i) {
			if (batch.get(i).contains(string))
				return true;
		}
		return false;
	}

	class DbHelper extends SQLiteOpenHelper {
		public DbHelper(Context context) {
			super(context, OUTPUT_DB_NAME, null, DB_VERSION);
			Log.d(TAG, "DbHelper");
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			String sql = String.format("create table %s"
					+ "(%s text primary key, %s text, %s int)", "subjectLink",
					"code", "link", "LTP");
			Log.d(TAG, "onCreate with SQL : " + sql);
			db.execSQL(sql);

		}

	}

	public void buttonPress(View v) {
		Log.d(TAG, "buttonclick");
		pause = false;
	}

	private Sheet initWorkbook() throws BiffException, IOException {
		Workbook workbook;
		workbook = Workbook.getWorkbook(getAssets().open(INPUT_FILENAME));
		Sheet sheet = workbook.getSheet(SHEET);
		return sheet;
	}

	private void setEditboxText(final String str) {
		this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				((EditText) findViewById(R.id.editText)).setText(str);

			}
		});
	}

	String editBoxText;

	private String getEditboxText() {
		String tmp;
		this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				editBoxText = ((EditText) findViewById(R.id.editText))
						.getEditableText().toString().trim();
				Log.d(TAG, "editbox text : " + editBoxText);

			}
		});
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return editBoxText;
	}

	private void deleteDatabase() {
		if (deleteDatabase(OUTPUT_DB_NAME))
			Log.d(TAG, "Attendence cleared");
		else
			Log.d(TAG, "unable to clear attendence");

	}

}
