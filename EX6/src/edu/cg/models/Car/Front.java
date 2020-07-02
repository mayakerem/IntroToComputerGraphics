package edu.cg.models.Car;

import java.util.LinkedList;
import java.util.List;

import com.jogamp.opengl.GL2;

import edu.cg.algebra.Point;
import edu.cg.algebra.Vec;
import edu.cg.models.BoundingSphere;
import edu.cg.models.IIntersectable;
import edu.cg.models.IRenderable;

public class Front implements IRenderable, IIntersectable {
	private FrontHood hood = new FrontHood();
	private PairOfWheels wheels = new PairOfWheels();
	private FrontBumber frontBumper = new FrontBumber(); // Couldnt overlook this spelling mistake

	@Override
	public void render(GL2 gl) {
		
		gl.glPushMatrix();
		// Render hood - Use Red Material.
		gl.glTranslated(-Specification.F_LENGTH / 2.0 + Specification.F_HOOD_LENGTH / 2.0, 0.0, 0.0);
		hood.render(gl);
		
		// Render the wheels.
		gl.glTranslated(Specification.F_HOOD_LENGTH / 2.0 - 1.25 * Specification.TIRE_RADIUS,
				0.5 * Specification.TIRE_RADIUS, 0.0);
		wheels.render(gl);

		// Rendering Front BUMPER
		gl.glTranslated(1.25 * Specification.TIRE_RADIUS + Specification.F_BUMPER_LENGTH / 2.0, 
				-0.5 * Specification.TIRE_RADIUS, 0.0);
		frontBumper.render(gl);
		
		gl.glPopMatrix();
	}

	@Override
	public void init(GL2 gl) {
	}

	@Override
	public List<BoundingSphere> getBoundingSpheres() {
	    // List to return
	    LinkedList<BoundingSphere> res = new LinkedList<BoundingSphere>();
	    
	    Point centerPoint = new Point(0, (Specification.F_HEIGHT / 2.0), 0);
		double sphereRadius = new Vec(
				Specification.F_LENGTH / 2, 
				Specification.F_HEIGHT / 2, 
				Specification.F_DEPTH / 2).norm();
		
		BoundingSphere boundingSphere = new BoundingSphere(sphereRadius, centerPoint);
		
		boundingSphere.setSphereColore3d(1.0, 1.0, 0);
		res.add(boundingSphere);
//		System.out.println("Printed Front Bounding Box" + res);
	    return res;
		
	}

	@Override
	public String toString() {
		return "CarFront";
	}

	@Override
	public void destroy(GL2 gl) {
		// TODO Auto-generated method stub
		
	}
}
