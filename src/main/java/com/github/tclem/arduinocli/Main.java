package com.github.tclem.arduinocli;
import java.io.IOException;

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

		String pdeFile = MyBase.init(args);

		try {
			MySketch sketch = new MySketch(null, pdeFile);
			sketch.compile();
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		// sketch.compileAndDeploy();
	}

}