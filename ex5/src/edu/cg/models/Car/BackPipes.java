package edu.cg.models.Car;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;

import edu.cg.models.IRenderable;

public class BackPipes implements IRenderable {
	
	@Override
	public void render(GL2 gl) {
		
		GLU glu = new GLU();
		GLUquadric quad = glu.gluNewQuadric();
		
		gl.glPushMatrix();
		gl.glTranslated(-0.33, 0.07, -0.03); //distance on coordinate system
		gl.glRotated(90, 0.0, 1.0, 0.0);	
		Materials.SetSandColorMetalMaterial(gl);
		glu.gluCylinder(quad, 0.02,0.02,0.2,20,1);
		gl.glPopMatrix();
		
		gl.glPushMatrix();
		gl.glTranslated(-0.33, 0.07, 0.03); //distance on coordinate system
		gl.glRotated(90, 0.0, 1.0, 0.0);	// rotation
		Materials.SetSandColorMetalMaterial(gl);
		glu.gluCylinder(quad, 0.02,0.02,0.2,20,1); // Cylinder size
		gl.glPopMatrix();
		
		glu.gluDeleteQuadric(quad);
	}

	@Override
	public void init(GL2 gl) {
	}
	
	@Override
	public String toString() {
		return "BackPipes";
	}

}