package edu.cg.models.Car;

import com.jogamp.opengl.GL2;

import edu.cg.models.IRenderable;
import edu.cg.models.SkewedBox;

public class FrontHood implements IRenderable {
	private SkewedBox hoodBox1 = new SkewedBox(Specification.F_HOOD_LENGTH_1, Specification.F_HOOD_HEIGHT_1,
			Specification.F_HOOD_HEIGHT_2, Specification.F_HOOD_DEPTH_1, Specification.F_HOOD_DEPTH_2);
	private SkewedBox hoodBox2 = new SkewedBox(Specification.F_HOOD_LENGTH_2, Specification.F_HOOD_HEIGHT_2,
			Specification.F_BUMPER_HEIGHT_1, Specification.F_HOOD_DEPTH_2, Specification.F_HOOD_DEPTH_3);
	
	@Override
	public void render(GL2 gl) {
		gl.glPushMatrix();
		double hoodLength = Specification.F_HOOD_LENGTH_1 + Specification.F_HOOD_LENGTH_2;
		// Render hood - Use Red Material.
		Materials.SetRedMetalMaterial(gl);
		gl.glTranslated(-hoodLength / 2.0 + Specification.F_HOOD_LENGTH_1 / 2.0, 0.0, 0.0);
		hoodBox1.render(gl);
		gl.glTranslated(Specification.F_HOOD_LENGTH_1 / 2.0 + Specification.F_HOOD_LENGTH_2 / 2.0, 0.0, 0.0);
		hoodBox2.render(gl);
		gl.glPopMatrix();
	}

	@Override
	public void init(GL2 gl) {
	}
	
	@Override
	public String toString() {
		return "FrontHood";
	}

}
