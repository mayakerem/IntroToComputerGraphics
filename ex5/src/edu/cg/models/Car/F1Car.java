package edu.cg.models.Car;

import java.util.LinkedList;
import java.util.List;

import com.jogamp.opengl.*;

import edu.cg.algebra.Point;
import edu.cg.models.BoundingSphere;
import edu.cg.models.IIntersectable;
import edu.cg.models.IRenderable;

/**
 * A F1 Racing Car.
 *
 */
public class F1Car implements IRenderable, IIntersectable {
	// TODO : Add new design features to the car.
	// Remember to include a ReadMe file specifying what you implemented.
	Center carCenter = new Center();
	Back carBack = new Back();
	Front carFront = new Front();

	@Override
	public void render(GL2 gl) {
		carCenter.render(gl);
		gl.glPushMatrix();
		gl.glTranslated(-Specification.B_LENGTH / 2.0 - Specification.C_BASE_LENGTH / 2.0, 0.0, 0.0);
		carBack.render(gl);
		gl.glPopMatrix();
		gl.glPushMatrix();
		gl.glTranslated(Specification.F_LENGTH / 2.0 + Specification.C_BASE_LENGTH / 2.0, 0.0, 0.0);
		carFront.render(gl);
		gl.glPopMatrix();

	}

	@Override
	public String toString() {
		return "F1Car";
	}

	@Override
	public void init(GL2 gl) {

	}

	@Override
	public List<BoundingSphere> getBoundingSpheres() {

		// Initializing resulting list
		LinkedList<BoundingSphere> res = new LinkedList<BoundingSphere>();

		// Constructing s1
		// changed B_Height to S_base height
		Point s1Center = new Point(0, Specification.S_BASE_HEIGHT / 2.0, 0);

		double s1CenterFrontDistance = s1Center
				.dist(new Point(Specification.F_LENGTH + Specification.F_BUMPER_LENGTH, 0.0, 0.0));
		double s1CenterBackDistance = s1Center
				.dist(new Point(Specification.B_LENGTH + Specification.S_LENGTH, 0.0, 0.0));
		double s1Radius = Math.max(s1CenterFrontDistance, s1CenterBackDistance);

		BoundingSphere boundingSphere = new BoundingSphere(s1Radius, s1Center);
		boundingSphere.setSphereColore3d(1.0, 0.0, 0.0);
		System.out.println("Printed Entire Car bounding box" + res);
		res.add(boundingSphere);

		// Constructing s2
		List<BoundingSphere> frontBS = carFront.getBoundingSpheres();
		double frontTranslation = Specification.F_LENGTH / 2.0 + Specification.C_BASE_LENGTH / 2.0;

		for (BoundingSphere frontBoundingSphere : frontBS) {
			// Using global coordinate system
			frontBoundingSphere.translateCenter(frontTranslation, 0.0, 0.0);
			res.add(frontBoundingSphere);
		}
		System.out.println("Printed Front Car bounding box");

		// Constructing s3
		List<BoundingSphere> centerBS = carCenter.getBoundingSpheres();
		for (BoundingSphere centerBoundingSphere : centerBS) {
			// Move to global coordinate system = Null
			res.add(centerBoundingSphere);
		}

		// Constructing s4
		List<BoundingSphere> backBS = carBack.getBoundingSpheres();
		double backTranslation = -1 * (Specification.B_LENGTH) / 2.0 - Specification.C_BASE_LENGTH / 2.0;
		// translate relative to the car model coordinate system
		for (BoundingSphere backBoundingSphere : backBS) {
			backBoundingSphere.translateCenter(backTranslation, 0.0, 0.0);
			res.add(backBoundingSphere);
		}

		return res;
	}
}
