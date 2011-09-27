import java.io.IOException;

import processing.app.Base;
import processing.app.Editor;
import processing.app.SerialException;
import processing.app.Sketch;
import processing.app.debug.Compiler;
import processing.app.debug.RunnerException;
import processing.app.preproc.PdePreprocessor;

public class MySketch extends Sketch {

	String primaryClassName;
	String tempBuildFolder;

	public MySketch(Editor editor, String path) throws IOException {
		super(editor, path);
	}

	public void compile() throws RunnerException {
		tempBuildFolder = Base.getBuildFolder().getAbsolutePath();
		System.out.println(tempBuildFolder);

		load();

		PdePreprocessor pp = new PdePreprocessor();
		primaryClassName = preprocess(tempBuildFolder, pp);
		System.out.println(pp.getExtraImports());

		Compiler compiler = new Compiler();
		Boolean res = compiler.compile(this, tempBuildFolder, primaryClassName,
				true);
		System.out.printf("Compilation %s.\n", res ? "succeeded" : "failed");
	}

	public void compileAndDeploy() throws RunnerException, SerialException {
		compile();
		this.upload(tempBuildFolder, primaryClassName, true);
	}
}
