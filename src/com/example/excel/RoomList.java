package com.example.excel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.util.Log;

public class RoomList {
	Context context;
	List<String> roomList = new ArrayList<String>();

	public RoomList(Context context) {
		this.context = context;
		getRoomListFromStore();
	}
	
	public boolean isRoomPresent(String room) {
		return roomList.contains(room);
	}
	
	public String[] getRoomAndTeacherCode(String str) {
		String[] parts = str.split("\\W");
		for (String x : parts) {
			Log.d("RoomList", "parts : " + x);
			if (roomList.contains(x))  {
				return new String[]{ x, getTeacherCode(x,parts) };
				
			}
		}
		return null;
		
	}
	

	private String getTeacherCode(String room, String[] parts) {
		String str="";
		for (String x : parts) {
			if (x.equals(room)) continue;
			str+="/" + x;
		}
		
		if (str.length() == 0) str = "/NA";
		
		return str.substring(1);
		
	}

	void insert(String room) {
		room = room.trim();
		if (!roomList.contains(room)) {
			roomList.add(room);
			Log.i("RoomList", room);
		}

	}

	void saveChangesToSd() {
		deleteSdCardFile();
		for (String room : roomList) {
			writeToSd(room);
		}

	}

	private void deleteSdCardFile() {
		File logFile = new File("sdcard/log.txt");
		if (logFile.exists()) {
			try {
				logFile.delete();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private  void writeToSd(String text) {

		File logFile = new File("sdcard/log.txt");
		if (!logFile.exists()) {
			try {
				logFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			// BufferedWriter for performance, true to set append to file flag
			BufferedWriter buf = new BufferedWriter(new FileWriter(logFile,
					true));
			buf.append(text);
			// Log.d(TAG, text);
			buf.newLine();
			buf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void getRoomListFromStore() {
		try {
			InputStream inputStream = context.getResources().openRawResource(
					R.raw.roomlist);
			BufferedReader br = new BufferedReader(new InputStreamReader(
					inputStream));

			String line;
			while (true) {
				line = br.readLine();
				if (line == null || line.equals(""))
					break;
				insert(line);
			}
			br.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	

}
