package com.github.tclem.arduinocli;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.zip.GZIPInputStream;

import processing.app.SketchCode;

public class MySketchCode extends SketchCode {

	public MySketchCode(File file, String extension) {
		super(file, extension);
	}
	
	private void setField(String fieldName, Object value){
		try {
			Field field = SketchCode.class.getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(this, value);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void load() throws IOException {
		File file = getFile();
		String program = getProgram();
		
		program = loadFile(file);
		setField("program",  program);

		if (program.indexOf('\uFFFD') != -1) {
			System.err.println(file.getName()
					+ " contains unrecognized characters.");
			System.err
					.println("If this code was created with an older version of Processing,");
			System.err
					.println("you may need to use Tools -> Fix Encoding & Reload to update");
			System.err
					.println("the sketch to use UTF-8 encoding. If not, you may need to");
			System.err
					.println("delete the bad characters to get rid of this warning.");
			System.err.println();
		}

		setModified(false);
	}

	static public String loadFile(File file) throws IOException {
		String[] contents = loadStrings(file);
		if (contents == null)
			return null;
		return join(contents, "\n");
	}
	
	static public String join(String str[], String separator) {
	    StringBuffer buffer = new StringBuffer();
	    for (int i = 0; i < str.length; i++) {
	      if (i != 0) buffer.append(separator);
	      buffer.append(str[i]);
	    }
	    return buffer.toString();
	  }

	static public String[] loadStrings(File file) {
		InputStream is = createInput(file);
		if (is != null)
			return loadStrings(is);
		return null;
	}

	static public InputStream createInput(File file) {
		if (file == null) {
			throw new IllegalArgumentException(
					"File passed to createInput() was null");
		}
		try {
			InputStream input = new FileInputStream(file);
			if (file.getName().toLowerCase().endsWith(".gz")) {
				return new GZIPInputStream(input);
			}
			return input;

		} catch (IOException e) {
			System.err.println("Could not createInput() for " + file);
			e.printStackTrace();
			return null;
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
