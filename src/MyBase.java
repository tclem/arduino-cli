import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import processing.app.Base;
import processing.app.Preferences;
import processing.app.Sketch;
import processing.app.debug.Compiler;
import processing.app.debug.Target;
import processing.core.PApplet;

public class MyBase extends Base {

	public MyBase(String[] args) {
		super(args);
	}

	static Map<String, File> imports;

	static public String init(String[] args) {

		if (args.length < 1) {
			throw new IllegalArgumentException(
					"A *.pde file must be specified.");
		}
		if (args.length < 2) {
			throw new IllegalArgumentException(
					"A serial port must be specified.");
		}
		String pdeFile = args[0];
		String serialPort = args[1];
		String board = args.length >= 3 ? args[2] : "uno";

		System.out.printf("Compiling file:    %s\n", pdeFile);
		System.out.printf("Using board:       %s\n", board);
		System.out.printf("Using serial port: %s\n", serialPort);

		// Mock out the Arduino IDE minimal setup
		imports = new HashMap<String, File>();
		targetsTable = new HashMap<String, Target>();

		System.setProperty("javaroot",
				"/Applications/Arduino.app/Contents/Resources/Java/");

		Preferences.set("sketchbook.path", "/Users/tclem/Documents/Arduino/");

		loadHardware2(getHardwareFolder());
		loadHardware2(getSketchbookHardwareFolder());

		Preferences.setInteger("editor.tabs.size", 2);

		Preferences.set("target", "arduino");
		Preferences.set("upload.using", "bootloader");

		Preferences.set("board", board);
		Preferences.set("serial.port", serialPort);

		Preferences.setInteger("serial.databits", 8);
		Preferences.setInteger("serial.stopbits", 1);
		Preferences.set("serial.parity", "N");
		Preferences.setInteger("serial.debug_rate", 9600);

		addLibraries();

		return pdeFile;
	}

	private static void addLibraries() {
		try {
			addLibraries(getContentFile("libraries"));
			addLibraries(getSketchbookLibrariesFolder());

			System.out.println(imports);

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
