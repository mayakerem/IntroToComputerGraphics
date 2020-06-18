package edu.cg.models.Car;

import com.jogamp.opengl.GL2;

public class Materials {

	private static final float dark_gray[] = { 0.2f, 0.2f, 0.2f };
	private static final float dark_red[] = { 0.25f, 0.01f, 0.01f };
	private static final float red[] = { 0.5f, 0f, 0f };
	private static final float black[] = { 0.05f, 0.05f, 0.05f };

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
		gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, 10);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, diffColor, 0);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, specular, 0);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_EMISSION, emissionColor, 0);
	}

	public static void SetBlackMetalMaterial(GL2 gl) {
		SetMetalMaterial(gl, black);
	}

	public static void SetRedMetalMaterial(GL2 gl) {
		SetMetalMaterial(gl, red);
	}

	public static void SetDarkRedMetalMaterial(GL2 gl) {
		SetMetalMaterial(gl, dark_red);
	}
	
	public static void SetDarkGreyMetalMaterial(GL2 gl) {
		gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, 25);
		SetMetalMaterial(gl, dark_gray);
	}

	public static void setMaterialTire(GL2 gl) {
		float col[] = { .05f, .05f, .05f };
		gl.glColor3fv(col,0);
		gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, 100);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, col, 0);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, col, 0);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_EMISSION, col, 0);
	}

	public static void setMaterialRims(GL2 gl) {
		float col[] = { .8f, .8f, .8f };
		gl.glColor3fv(dark_gray,0);
		gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, 20);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, dark_gray, 0);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, col, 0);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_EMISSION, dark_gray, 0);
	}

}
