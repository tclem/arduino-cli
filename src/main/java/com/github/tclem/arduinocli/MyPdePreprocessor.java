package com.github.tclem.arduinocli;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import processing.app.Preferences;
import processing.app.Sketch;
import processing.app.preproc.PdePreprocessor;

public class MyPdePreprocessor extends PdePreprocessor {

	private void setField(String fieldName, Object value) {
		try {
			Field field = PdePreprocessor.class.getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(this, value);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int writePrefix(String program, String buildPath, String sketchName,
			String codeFolderPackages[]) throws FileNotFoundException {

		setField("buildPath", buildPath);
		// this.buildPath = buildPath;
		// this.name = sketchName;
		setField("name", sketchName);

		// if the program ends with no CR or LF an OutOfMemoryError will happen.
		// not gonna track down the bug now, so here's a hack for it:
		// http://dev.processing.org/bugs/show_bug.cgi?id=5
		program += "\n";

		// if the program ends with an unterminated multi-line comment,
		// an OutOfMemoryError or NullPointerException will happen.
		// again, not gonna bother tracking this down, but here's a hack.
		// http://dev.processing.org/bugs/show_bug.cgi?id=16
		Sketch.scrubComments(program);
		// If there are errors, an exception is thrown and this fxn exits.

		if (Preferences.getBoolean("preproc.substitute_unicode")) {
			program = substituteUnicode(program);
		}

		// String importRegexp = "(?:^|\\s|;)(import\\s+)(\\S+)(\\s*;)";
		String importRegexp = "^\\s*#include\\s+[<\"](\\S+)[\">]";
		ArrayList<String> programImports = new ArrayList<String>();
		setField("programImports", programImports);

		String[][] pieces = matchAll(program, importRegexp);

		if (pieces != null)
			for (int i = 0; i < pieces.length; i++)
				programImports.add(pieces[i][1]); // the package name

		ArrayList<String> codeFolderImports = new ArrayList<String>();
		setField("codeFolderImports", codeFolderImports);

		ArrayList<String> prototypes = prototypes(program);
		setField("prototypes", prototypes);

		// store # of prototypes so that line number reporting can be adjusted
		prototypeCount = prototypes.size();

		// do this after the program gets re-combobulated
		// this.program = program;
		setField("program", program);

		// output the code
		File streamFile = new File(buildPath, sketchName + ".cpp");
		PrintStream stream = new PrintStream(new FileOutputStream(streamFile));
		setField("stream", stream);

		return headerCount + prototypeCount;
	}

	static public String[][] matchAll(String what, String regexp) {
		Pattern p = Pattern.compile(regexp, Pattern.MULTILINE | Pattern.DOTALL);
		Matcher m = p.matcher(what);
		ArrayList<String[]> results = new ArrayList<String[]>();
		int count = m.groupCount() + 1;
		while (m.find()) {
			String[] groups = new String[count];
			for (int i = 0; i < count; i++) {
				groups[i] = m.group(i);
			}
			results.add(groups);
		}
		if (results.isEmpty()) {
			return null;
		}
		String[][] matches = new String[results.size()][count];
		for (int i = 0; i < matches.length; i++) {
			matches[i] = (String[]) results.get(i);
		}
		return matches;
	}

	static String substituteUnicode(String program) {
		// check for non-ascii chars (these will be/must be in unicode format)
		char p[] = program.toCharArray();
		int unicodeCount = 0;
		for (int i = 0; i < p.length; i++) {
			if (p[i] > 127)
				unicodeCount++;
		}
		// if non-ascii chars are in there, convert to unicode escapes
		if (unicodeCount != 0) {
			// add unicodeCount * 5.. replacing each unicode char
			// with six digit uXXXX sequence (xxxx is in hex)
			// (except for nbsp chars which will be a replaced with a space)
			int index = 0;
			char p2[] = new char[p.length + unicodeCount * 5];
			for (int i = 0; i < p.length; i++) {
				if (p[i] < 128) {
					p2[index++] = p[i];

				} else if (p[i] == 160) { // unicode for non-breaking space
					p2[index++] = ' ';

				} else {
					int c = p[i];
					p2[index++] = '\\';
					p2[index++] = 'u';
					char str[] = Integer.toHexString(c).toCharArray();
					// add leading zeros, so that the length is 4
					// for (int i = 0; i < 4 - str.length; i++) p2[index++] =
					// '0';
					for (int m = 0; m < 4 - str.length; m++)
						p2[index++] = '0';
					System.arraycopy(str, 0, p2, index, str.length);
					index += str.length;
				}
			}
			program = new String(p2, 0, index);
		}
		return program;
	}
}
