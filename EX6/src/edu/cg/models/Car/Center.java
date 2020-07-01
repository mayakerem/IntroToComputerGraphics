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

public class Center implements IRenderable, IIntersectable {

	private SkewedBox bodyBase = new SkewedBox(Specification.C_BASE_LENGTH, Specification.C_BASE_HEIGHT,
			Specification.C_BASE_HEIGHT, Specification.C_DEPTH, Specification.C_DEPTH);
	private SkewedBox backBox = new SkewedBox(Specification.C_BACK_LENGTH, Specification.C_BACK_HEIGHT_1,
			Specification.C_BACK_HEIGHT_2, Specification.C_BACK_DEPTH, Specification.C_BACK_DEPTH);
	private SkewedBox frontBox = new SkewedBox(Specification.C_FRONT_LENGTH, Specification.C_FRONT_HEIGHT_1,
			Specification.C_FRONT_HEIGHT_2, Specification.C_FRONT_DEPTH_1, Specification.C_FRONT_DEPTH_2);
	private SkewedBox sideBox = new SkewedBox(Specification.C_SIDE_LENGTH, Specification.C_SIDE_HEIGHT_1,
			Specification.C_SIDE_HEIGHT_2, Specification.C_SIDE_DEPTH_1, Specification.C_SIDE_DEPTH_2);

	@Override
	public void render(GL2 gl) {
		gl.glPushMatrix();
		Materials.SetBlueMetalMaterial(gl);
		bodyBase.render(gl);
		Materials.SetOceanGreenMetalMaterial(gl);
		gl.glTranslated(Specification.C_BASE_LENGTH / 2.0 - Specification.C_FRONT_LENGTH / 2.0,
				Specification.C_BASE_HEIGHT, 0.0);
		frontBox.render(gl);
		gl.glPopMatrix();
		gl.glPushMatrix();
		gl.glTranslated(-Specification.C_BASE_LENGTH / 2.0 + Specification.C_FRONT_LENGTH / 2.0,
				Specification.C_BASE_HEIGHT, 0.0);
		gl.glRotated(180, 0.0, 1.0, 0.0);
		frontBox.render(gl);
		gl.glPopMatrix();
		gl.glPushMatrix();
		gl.glTranslated(0.0, Specification.C_BASE_HEIGHT,
				Specification.C_SIDE_LENGTH / 2 + Specification.C_FRONT_DEPTH_1 / 2.0);
		gl.glRotated(90, 0.0, 1.0, 0.0);
		sideBox.render(gl);
		gl.glPopMatrix();
		gl.glPushMatrix();
		gl.glTranslated(0.0, Specification.C_BASE_HEIGHT,
				-Specification.C_SIDE_LENGTH / 2 - Specification.C_FRONT_DEPTH_1 / 2.0);
		gl.glRotated(-90, 0.0, 1.0, 0.0);
		sideBox.render(gl);
		gl.glPopMatrix();
		Materials.SetBlueMetalMaterial(gl);
		gl.glPushMatrix();
		gl.glTranslated(
				-Specification.C_BASE_LENGTH / 2.0 + Specification.C_FRONT_LENGTH + Specification.C_BACK_LENGTH / 2.0,
				Specification.C_BASE_HEIGHT, 0.0);
		backBox.render(gl);
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
				Specification.C_LENGTH / 2, 
				Specification.C_HIEGHT / 2, 
				Specification.C_DEPTH / 2).norm();
		
		BoundingSphere boundingSphere = new BoundingSphere(sphereRadius, centerPoint);
		
		boundingSphere.setSphereColore3d(1.0, 0, 1.0);
		res.add(boundingSphere);
		System.out.println("Printed Center Bounding Box" + res);

	    return res;
		
	}

	@Override
	public void destroy(GL2 gl) {
		// TODO Auto-generated method stub
		
	}
}
