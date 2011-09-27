import java.io.IOException;
import java.util.Map;

import processing.app.Base;
import processing.app.Preferences;
import processing.app.SerialException;
import processing.app.debug.RunnerException;

public class Main {

	/**
	 * @param args
	 * @throws IOException
	 * @throws RunnerException
	 * @throws SerialException
	 */
	public static void main(String[] args) throws IOException, RunnerException,
			SerialException {

		MyBase.init(args);

		// These need to be set to the proper values
		Preferences.set("board", "uno");
		Preferences.set("serial.port", "/dev/tty.usbmodemfa141");

		MySketch sketch = new MySketch(
				null,
				"/Applications/Arduino.app/Contents/Resources/Java/examples/1.Basics/Blink/Blink.pde");
		// MySketch sketch = new MySketch(null,
		// "/Users/tclem/Documents/Arduino/DoorMe/Doorme.pde");
		// sketch.compile();
		sketch.compileAndDeploy();
	}

}