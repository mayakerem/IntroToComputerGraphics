package edu.cg.models.Car;

import java.util.LinkedList;
import java.util.List;

import com.jogamp.opengl.GL2;

import edu.cg.algebra.Point;
import edu.cg.algebra.Vec;
import edu.cg.models.BoundingSphere;
import edu.cg.models.IIntersectable;
import edu.cg.models.IRenderable;
import edu.cg.models.SkewedBox;

public class Back implements IRenderable, IIntersectable {
	private SkewedBox baseBox = new SkewedBox(Specification.B_BASE_LENGTH, Specification.B_BASE_HEIGHT,
			Specification.B_BASE_HEIGHT, Specification.B_BASE_DEPTH + 0.6, Specification.B_BASE_DEPTH); // Adapted the base so it acts as wings as well
	private SkewedBox backBox = new SkewedBox(Specification.B_LENGTH, Specification.B_HEIGHT_1,
			Specification.B_HEIGHT_2, Specification.B_DEPTH_1, Specification.B_DEPTH_2);
	private PairOfWheels wheels = new PairOfWheels();
	private Spolier spoiler = new Spolier();
	private BackPipes backpipe = new BackPipes();
	@Override
	public void render(GL2 gl) {
		
		gl.glPushMatrix();
		Materials.SetBlueMetalMaterial(gl);
		gl.glTranslated(Specification.B_LENGTH / 2.0 - Specification.B_BASE_LENGTH / 2.0, 0.0, 0.0);
		
		backpipe.render(gl);
		gl.glTranslated(Specification.B_LENGTH / 2.0 - Specification.B_BASE_LENGTH / 2.0, 0.0, 0.0);
		
		baseBox.render(gl);
		Materials.SetOceanGreenMetalMaterial(gl);
		
		gl.glTranslated(
				-1.0 * (Specification.B_LENGTH / 2.0 - Specification.B_BASE_LENGTH / 2.0),
				Specification.B_BASE_HEIGHT, 
				0.0);
		backBox.render(gl);
		
		gl.glPopMatrix();
		
		gl.glPushMatrix();
		gl.glTranslated(
				-Specification.B_LENGTH / 2.0 + Specification.TIRE_RADIUS, 
				0.5 * Specification.TIRE_RADIUS,
				0.0);
		wheels.render(gl);
		gl.glPopMatrix();
		
		gl.glPushMatrix();
		gl.glTranslated(
				-Specification.B_LENGTH / 2.0 + 0.5 * Specification.S_LENGTH,
				0.5 * (Specification.B_HEIGHT_1 + Specification.B_HEIGHT_2), 
				0.0);
		spoiler.render(gl);
		gl.glPopMatrix();
		
		
	}

	@Override
	public void init(GL2 gl) {

	}

	@Override
	public List<BoundingSphere> getBoundingSpheres() {
		
	    // List to return
	    LinkedList<BoundingSphere> res = new LinkedList<BoundingSphere>();
	    
	    Point centerPoint = new Point(0, (Specification.B_HEIGHT / 2.0), 0);
		double sphereRadius = new Vec(
				Specification.B_LENGTH / 2, 
				Specification.B_HEIGHT / 2, 
				Specification.B_DEPTH / 2).norm();
		
		BoundingSphere boundingSphere = new BoundingSphere(sphereRadius, centerPoint);
		
		boundingSphere.setSphereColore3d(0, 1.0, 1.0);
		res.add(boundingSphere);
		System.out.println("Printed Back Bounding Box" + res);
	    return res;
		}

	@Override
	public void destroy(GL2 gl) {
		// TODO Auto-generated method stub
		
	}

}
