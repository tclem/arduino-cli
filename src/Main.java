import java.io.IOException;

import processing.app.debug.RunnerException;

public class Main {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws RunnerException 
	 */
	public static void main(String[] args) throws IOException, RunnerException {

		System.out.println("invoking arduino compile");
		
		MyBase.init(args);
		
		new MySketch(null, "/Users/tclem/Documents/Arduino/DoorMe/Doorme.pde").compile();
	}

}