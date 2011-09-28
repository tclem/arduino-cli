package com.github.tclem.arduinocli;

import java.io.File;
import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import processing.app.Sketch;
import processing.app.SketchCode;
import processing.app.debug.Compiler;
import processing.app.debug.RunnerException;
import processing.core.PApplet;

public class MyCompiler extends Compiler {

	Sketch sketch;
	String buildPath;
	String primaryClassName;
	boolean verbose;
	RunnerException exception;

	public boolean compile(Sketch sketch, String buildPath,
			String primaryClassName, boolean verbose) throws RunnerException {

		this.sketch = sketch;
		this.buildPath = buildPath;
		this.primaryClassName = primaryClassName;
		this.verbose = verbose;

		return super.compile(sketch, buildPath, primaryClassName, verbose);
	}

	private void setField(String fieldName, Object value) {
		try {
			Field field = Compiler.class.getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(this, value);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static public String[] match(String what, String regexp) {
		Pattern p = Pattern.compile(regexp, Pattern.MULTILINE | Pattern.DOTALL);
		Matcher m = p.matcher(what);
		if (m.find()) {
			int count = m.groupCount() + 1;
			String[] groups = new String[count];
			for (int i = 0; i < count; i++) {
				groups[i] = m.group(i);
			}
			return groups;
		}
		return null;
	}

	static public int parseInt(String what, int otherwise) {
		try {
			int offset = what.indexOf('.');
			if (offset == -1) {
				return Integer.parseInt(what);
			} else {
				return Integer.parseInt(what.substring(0, offset));
			}
		} catch (NumberFormatException e) {
		}
		return otherwise;
	}

	public void message(String s) {
		int i;

		// remove the build path so people only see the filename
		// can't use replaceAll() because the path may have characters in it
		// which
		// have meaning in a regular expression.
		if (!verbose) {
			while ((i = s.indexOf(buildPath + File.separator)) != -1) {
				s = s.substring(0, i)
						+ s.substring(i + (buildPath + File.separator).length());
			}
		}

		// look for error line, which contains file name, line number,
		// and at least the first line of the error message
		String errorFormat = "([\\w\\d_]+.\\w+):(\\d+):\\s*error:\\s*(.*)\\s*";
		String[] pieces = match(s, errorFormat);

		// if (pieces != null && exception == null) {
		// exception = sketch.placeException(pieces[3], pieces[1],
		// PApplet.parseInt(pieces[2]) - 1);
		// if (exception != null) exception.hideStackTrace();
		// }

		if (pieces != null) {
			RunnerException e = sketch.placeException(pieces[3], pieces[1],
					parseInt(pieces[2], 0) - 1);

			// replace full file path with the name of the sketch tab (unless
			// we're
			// in verbose mode, in which case don't modify the compiler output)
			if (e != null && !verbose) {
				SketchCode code = sketch.getCode(e.getCodeIndex());
				String fileName = code
						.isExtension(sketch.getDefaultExtension()) ? code
						.getPrettyName() : code.getFileName();
				s = fileName + ":" + e.getCodeLine() + ": error: "
						+ e.getMessage();
			}

			if (pieces[3].trim().equals("SPI.h: No such file or directory")) {
				e = new RunnerException(
						"Please import the SPI library from the Sketch > Import Library menu.");
				s += "\nAs of Arduino 0019, the Ethernet library depends on the SPI library."
						+ "\nYou appear to be using it or another library that depends on the SPI library.";
			}

			if (exception == null && e != null) {
				exception = e;
				exception.hideStackTrace();
				setField("exception", exception);
			}
		}

		System.err.print(s);
	}
}
