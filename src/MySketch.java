import java.io.IOException;

import processing.app.Base;
import processing.app.Editor;
import processing.app.Sketch;
import processing.app.debug.Compiler;
import processing.app.debug.RunnerException;
import processing.app.preproc.PdePreprocessor;


public class MySketch extends Sketch {
	
	public MySketch(Editor editor, String path) throws IOException {
		super(editor, path);
	}
	
	public int compile() throws RunnerException {
		String tempBuildFolder = Base.getBuildFolder().getAbsolutePath();
		System.out.println(tempBuildFolder);
		
		load();
		
		PdePreprocessor pp = new PdePreprocessor();
		String primaryClassName = preprocess(tempBuildFolder, pp);
		System.out.println(pp.getExtraImports());
		
		Compiler compiler = new Compiler();
		Boolean res = compiler.compile(this, tempBuildFolder, primaryClassName, true);
		System.out.printf("Compilation %s.\n", res ? "succeeded" : "failed");
		
		return res ? 0 : 1;
	}
}
