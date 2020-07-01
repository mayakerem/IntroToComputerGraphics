package edu.cg.models.Car;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;

import edu.cg.models.IRenderable;
import edu.cg.models.SkewedBox;

public class FrontBumber implements IRenderable {
	SkewedBox bumperBox;
	SkewedBox bumperWingsBox1;

	@Override
	public void render(GL2 gl) {

		// Initializing GLU
		final GLU glu = new GLU();
	    final GLUquadric quad = glu.gluNewQuadric();
	    	   
	    gl.glPushMatrix();

	    this.bumperBox = new SkewedBox(
				Specification.F_BUMPER_LENGTH,
				Specification.F_BUMPER_HEIGHT_1,
				Specification.F_BUMPER_HEIGHT_2,
				Specification.F_BUMPER_DEPTH,
				Specification.F_BUMPER_DEPTH);
		this.bumperBox.render(gl);
		//Draw first wing

		gl.glTranslated(0,0, Specification.F_BUMPER_WINGS_DEPTH / 2 + Specification.F_BUMPER_DEPTH / 2);
		gl.glPushMatrix();

		Materials.SetBlueMetalMaterial(gl);

		this.bumperWingsBox1  = new SkewedBox(
				Specification.F_BUMPER_LENGTH,
				Specification.F_BUMPER_WINGS_HEIGHT_1,
				Specification.F_BUMPER_WINGS_HEIGHT_2,
				Specification.F_BUMPER_WINGS_DEPTH,
				Specification.F_BUMPER_WINGS_DEPTH);
		this.bumperWingsBox1.render(gl);

		//Draw sphere
		renderSphere(gl,glu,quad);
		gl.glPopMatrix(); //return to first wing position

		//Draw second wing
		gl.glTranslated(0,0,-2*(Specification.F_BUMPER_WINGS_DEPTH / 2 + Specification.F_BUMPER_DEPTH / 2));
		gl.glPushMatrix();// remember the second wing position
		
		Materials.SetBlueMetalMaterial(gl); // return to wing's color
		this.bumperWingsBox1.render(gl);

		//Draw sphere
		renderSphere(gl, glu, quad);
		gl.glPopMatrix();//return to second wing position

		gl.glPopMatrix(); //return to origin
		glu.gluDeleteQuadric(quad); //Clear from memory
	}
	

	private void renderSphere(GL2 gl, GLU glu, GLUquadric quad) {
		gl.glColor3d(0.87, 0.82, 0.75);// Sand Color
		double sphereRadius = Specification.F_BUMPER_WINGS_DEPTH / 2.5;
		gl.glTranslated(0.0, Specification.F_BUMPER_WINGS_DEPTH / 2.0, 0.0);
		glu.gluSphere(quad, sphereRadius, 10, 10);
	}
	

	@Override
	public void init(GL2 gl) {
	}

	@Override
	public String toString() {
		return "FrontBumper";
	}


	@Override
	public void destroy(GL2 gl) {
		// TODO Auto-generated method stub
		
	}
}
