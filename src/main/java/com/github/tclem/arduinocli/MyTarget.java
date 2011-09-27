package com.github.tclem.arduinocli;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import processing.app.debug.Target;

public class MyTarget extends Target {

	private String name;
	private File folder;
	private Map boards;
	private Map programmers;

	public MyTarget(String name, File folder) {
		super(name, new File("blah"));

		this.name = name;
		this.folder = folder;
		this.boards = new LinkedHashMap();
		this.programmers = new LinkedHashMap();

		File boardsFile = new File(folder, "boards.txt");
		try {
			if (boardsFile.exists()) {
				Map boardPreferences = new LinkedHashMap();
				load(new FileInputStream(boardsFile), boardPreferences);
				for (Object k : boardPreferences.keySet()) {
					String key = (String) k;
					String board = key.substring(0, key.indexOf('.'));
					if (!boards.containsKey(board))
						boards.put(board, new HashMap());
					((Map) boards.get(board)).put(
							key.substring(key.indexOf('.') + 1),
							boardPreferences.get(key));
				}
			}
		} catch (Exception e) {
			System.err.println("Error loading boards from " + boardsFile + ": "
					+ e);
		}

		File programmersFile = new File(folder, "programmers.txt");
		try {
			if (programmersFile.exists()) {
				Map programmerPreferences = new LinkedHashMap();
				load(new FileInputStream(programmersFile),
						programmerPreferences);
				for (Object k : programmerPreferences.keySet()) {
					String key = (String) k;
					String programmer = key.substring(0, key.indexOf('.'));
					if (!programmers.containsKey(programmer))
						programmers.put(programmer, new HashMap());
					((Map) programmers.get(programmer)).put(
							key.substring(key.indexOf('.') + 1),
							programmerPreferences.get(key));
				}
			}
		} catch (Exception e) {
			System.err.println("Error loading programmers from "
					+ programmersFile + ": " + e);
		}
	}

	public String getName() {
		return name;
	}

	public File getFolder() {
		return folder;
	}

	public Map<String, Map<String, String>> getBoards() {
		return boards;
	}

	public Map<String, Map<String, String>> getProgrammers() {
		return programmers;
	}

	static public void load(InputStream input, Map table) throws IOException {
		String[] lines = loadStrings(input); // Reads as UTF-8
		for (String line : lines) {
			if ((line.length() == 0) || (line.charAt(0) == '#'))
				continue;

			// this won't properly handle = signs being in the text
			int equals = line.indexOf('=');
			if (equals != -1) {
				String key = line.substring(0, equals).trim();
				String value = line.substring(equals + 1).trim();
				table.put(key, value);
			}
		}
	}

	static public String[] loadStrings(InputStream input) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					input, "UTF-8"));

			String lines[] = new String[100];
			int lineCount = 0;
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (lineCount == lines.length) {
					String temp[] = new String[lineCount << 1];
					System.arraycopy(lines, 0, temp, 0, lineCount);
					lines = temp;
				}
				lines[lineCount++] = line;
			}
			reader.close();

			if (lineCount == lines.length) {
				return lines;
			}

			// resize array to appropriate amount for these lines
			String output[] = new String[lineCount];
			System.arraycopy(lines, 0, output, 0, lineCount);
			return output;

		} catch (IOException e) {
			e.printStackTrace();
			// throw new RuntimeException("Error inside loadStrings()");
		}
		return null;
	}
}
