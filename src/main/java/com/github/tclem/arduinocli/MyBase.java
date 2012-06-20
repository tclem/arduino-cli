package com.github.tclem.arduinocli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import processing.app.Base;
import processing.app.Preferences;
import processing.app.Sketch;
import processing.app.debug.Compiler;
import processing.app.debug.Target;

public class MyBase extends Base {

	public MyBase(String[] args) {
		super(args);
	}

	public static Boolean deploy = false;

	static Map<String, File> imports;

	static public String init(String[] args) {

		// On OS X this should be: /Applications/Arduino.app/Contents/Resources/Java/
		String path = System.getProperty("java.library.path");
		if (!path.endsWith("/")) path = path + "/";
		System.out.printf("javaroot: %s\n", path);
		System.setProperty("user.dir", path); // Used by *nix
		System.setProperty("javaroot", path); // Used by OS X

		initPlatform();
		loadPreferences();

		if (args.length < 1) {
			throw new IllegalArgumentException(
					"You must specify -c (compile) or -d (compile and deploy).");
		}
		if (args.length < 2) {
			throw new IllegalArgumentException(
					"A *.pde file must be specified.");
		}
		if (args.length < 3) {
			throw new IllegalArgumentException(
					"A serial port must be specified.");
		}
		if (args[0].compareToIgnoreCase("-d") == 0) {
			deploy = true;
		}
		String pdeFile = args[1];
		String serialPort = args[2];
		String board = args.length >= 4 ? args[3] : "uno";

		System.out.printf("Compiling%s file: %s\n", deploy ? " and deploying"
				: "", pdeFile);
		System.out.printf("board: %s\n", board);
		System.out.printf("serial port: %s\n", serialPort);

		// Mock out the Arduino IDE minimal setup
		imports = new HashMap<String, File>();
		targetsTable = new HashMap<String, Target>();

		loadHardware2(getHardwareFolder());
		loadHardware2(getSketchbookHardwareFolder());
		addLibraries();

		return pdeFile;
	}

	private static void loadPreferences() {
		try {
			load(getLibStream("preferences.txt"));
			load(new FileInputStream(getSettingsFile("preferences.txt")));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void load(InputStream input) {
		Hashtable table = new Hashtable();

		String[] lines = MySketchCode.loadStrings(input);  // Reads as UTF-8
	    for (String line : lines) {
	      if ((line.length() == 0) ||
	          (line.charAt(0) == '#')) continue;

	      // this won't properly handle = signs being in the text
	      int equals = line.indexOf('=');
	      if (equals != -1) {
	        String key = line.substring(0, equals).trim();
	        String value = line.substring(equals + 1).trim();
	        table.put(key, value);
	      }
	    }

		// check for platform-specific properties in the defaults
	    String platformExt = "." + platformNames[pa_platform];
	    int platformExtLength = platformExt.length();
	    Enumeration e = table.keys();
	    while (e.hasMoreElements()) {
	      String key = (String) e.nextElement();
	      String value = (String) table.get(key);
	      if (key.endsWith(platformExt)) {
	        // this is a key specific to a particular platform
	        key = key.substring(0, key.length() - platformExtLength);
	      }

	      Preferences.set(key, value);
	      //System.out.printf("%s = %s\n", key, value);
	    }
	}

	// platform IDs for PApplet.platform

	static final int OTHER   = 0;
	static final int WINDOWS = 1;
	static final int MACOSX  = 2;
	static final int LINUX   = 3;

	static final String[] platformNames = {
		"other", "windows", "macosx", "linux"
	};
	static public int pa_platform;
	static {
	    String osname = System.getProperty("os.name");

	    if (osname.indexOf("Mac") != -1) {
	      pa_platform = MACOSX;

	    } else if (osname.indexOf("Windows") != -1) {
	      pa_platform = WINDOWS;

	    } else if (osname.equals("Linux")) {  // true for the ibm vm
	      pa_platform = LINUX;

	    } else {
	      pa_platform = OTHER;
	    }
	  }

	private static void addLibraries() {
		try {
			addLibraries(getContentFile("libraries"));
			addLibraries(getSketchbookLibrariesFolder());

			//System.out.println(imports);

			Field field = Base.class.getDeclaredField("importToLibraryTable");
			field.setAccessible(true);
			field.set(null, imports);

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private static void addLibraries(File folder) {
		String list[] = folder.list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				// skip .DS_Store files, .svn folders, etc
				if (name.charAt(0) == '.')
					return false;
				if (name.equals("CVS"))
					return false;
				return (new File(dir, name).isDirectory());
			}
		});
		Arrays.sort(list, String.CASE_INSENSITIVE_ORDER);
		for (String potentialName : list) {
			File subfolder = new File(folder, potentialName);
			String sanityCheck = Sketch.sanitizeName(potentialName);
			if (!sanityCheck.equals(potentialName))
				continue;

			// String libraryName = potentialName;
			String packages[] = Compiler.headerListFromIncludePath(subfolder
					.getAbsolutePath());
			for (String pkg : packages) {
				imports.put(pkg, subfolder);
			}
		}
	}

	private static void loadHardware2(File folder) {
		if (!folder.isDirectory())
			return;

		String list[] = folder.list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				// skip .DS_Store files, .svn folders, etc
				if (name.charAt(0) == '.')
					return false;
				if (name.equals("CVS"))
					return false;
				return (new File(dir, name).isDirectory());
			}
		});
		// if a bad folder or something like that, this might come back null
		if (list == null)
			return;

		// alphabetize list, since it's not always alpha order
		// replaced hella slow bubble sort with this feller for 0093
		Arrays.sort(list, String.CASE_INSENSITIVE_ORDER);

		for (String target : list) {
			File subfolder = new File(folder, target);
			targetsTable.put(target, new MyTarget(target, subfolder));
		}
	}

}
