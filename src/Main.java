import java.io.IOException;
import java.util.Map;

import processing.app.Base;
import processing.app.SerialException;
import processing.app.debug.RunnerException;

public class Main {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws RunnerException 
	 * @throws SerialException
	 */
	public static void main(String[] args) throws IOException, RunnerException, SerialException {

		System.out.println("invoking arduino compile");
		
		MyBase.init(args);
		
		MySketch sketch =  new MySketch(null, "/Users/tclem/Documents/Arduino/DoorMe/Doorme.pde");
		//sketch.compile();
		sketch.compileAndDeploy();
	}

}