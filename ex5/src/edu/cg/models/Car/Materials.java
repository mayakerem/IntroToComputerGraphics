package edu.cg.models.Car;

import com.jogamp.opengl.GL2;

public class Materials {

	private static final float turquoise[] = { 0.12f, 0.75f, 0.5f };
	private static final float green_blue[] = { 0.12f, 0.5f, 0.75f };

	private static final float ocean_green[] = { 0.0f, 0.75f, 0.75f }; 
	private static final float blue[] = { 0.0f, 0.47f, 0.75f };

	public static void SetMetalMaterial(GL2 gl, float[] color) {
		float specular[] = { 0.8f, 0.8f, 0.8f };
		float emissionColor[] = new float[3];
		float diffColor[] = new float[3];
		float minIntensity = 1.0f;
		float maxIntensity = 0.0f;
		for (int i = 0; i < 3; i++) {
			emissionColor[i] =0.5f * color[i];
			minIntensity = Math.min(color[i], minIntensity);
			maxIntensity = Math.min(color[i], maxIntensity);
		}
		for (int i = 0; i < 3; i++) {
			diffColor[i] =color[i] + 0.5f*(maxIntensity - minIntensity);
		}
		
		gl.glColor3fv(color, 0);
		gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, 20);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, diffColor, 0);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, specular, 0);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_EMISSION, emissionColor, 0);
	}

	public static void SetBlueMetalMaterial(GL2 gl) {
		SetMetalMaterial(gl, blue);
	}

	public static void SetSandColorMetalMaterial(GL2 gl) {
		float[] sandColor = {0.87f, 0.82f, 0.75f} ; // sand color
		SetMetalMaterial(gl,sandColor);
	}
	
	public static void SetOceanGreenMetalMaterial(GL2 gl) {
		SetMetalMaterial(gl, ocean_green);
	}

	public static void SetGreenBlueMetalMaterial(GL2 gl) {
		SetMetalMaterial(gl, green_blue);
	}
	
	public static void SetTurquoiseMetalMaterial(GL2 gl) {
		gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, 25);
		SetMetalMaterial(gl, turquoise);
	}

	public static void setMaterialTire(GL2 gl) {
		float col[] = { 1.0f, 1.0f, 1.0f }; // White Color
		gl.glColor3fv(col,0);
		gl.glMaterialf(GL2.GL_FRONT, 0xFFFF, 100);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, col, 0);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, col, 0);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_EMISSION, col, 0);
	}

	public static void setMaterialRims(GL2 gl) {
		float col[] = { 1.0f, 1.0f, 1.0f }; // White Color
		gl.glColor3fv(turquoise,0);
		gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, 20);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, turquoise, 0);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, col, 0);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_EMISSION, turquoise, 0);
	}

}
