package edu.cg;

import java.awt.Component;
//import java.util.List;

import javax.swing.JOptionPane;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
//import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;

import edu.cg.algebra.Vec;
//import edu.cg.models.BoundingSphere;
import edu.cg.models.Track;
import edu.cg.models.TrackSegment;
import edu.cg.models.Car.F1Car;
//import edu.cg.models.Car.Specification;

/**
 * An OpenGL 3D Game.
 *
 */
public class NeedForSpeed implements GLEventListener {
	private GameState gameState = null; // Tracks the car movement and orientation
	private F1Car car = null; // The F1 car we want to render
	private Vec carCameraTranslation = null; // The accumulated translation that should be applied on the car, camera
												// and light sources
	private Track gameTrack = null; // The game track we want to render
	private FPSAnimator ani; // This object is responsible to redraw the model with a constant FPS
	private Component glPanel; // The canvas we draw on.
	private boolean isModelInitialized = false; // Whether model.init() was called.
	private boolean isDayMode = true; // Indicates whether the lighting mode is day/night.
	private boolean isBirdseyeView = false; // Indicates whether the camera is looking from above on the scene or
											// looking
	// towards the car direction.
	// TODO: add fields as you want. For example:
	// - Car initial position (should be fixed).
	// - Camera initial position (should be fixed)
	// - Different camera settings
	// - Light colors
	// Or in short anything reusable - this make it easier for your to keep track of your implementation.
	
	public NeedForSpeed(Component glPanel) {
		this.glPanel = glPanel;
		gameState = new GameState();
		gameTrack = new Track();
		carCameraTranslation = new Vec(0.0);
		car = new F1Car();
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		if (!isModelInitialized) {
			initModel(gl);
		}
		if (isDayMode) {
			// TODO: Setup background when day mode is on
			// use gl.glClearColor() function.
		} else {
			// TODO: Setup background when night mode is on
		}
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
		// TODO: This is the flow in which we render the scene.
		// Step (1) Update the accumulated translation that needs to be
		// applied on the car, camera and light sources.
		updateCarCameraTranslation(gl);
		// Step (2) Position the camera and setup its orientation
		setupCamera(gl);
		// Step (3) setup the lights.
		setupLights(gl);
		// Step (4) render the car.
		renderCar(gl);
		// Step (5) render the track.
		renderTrack(gl);
		// Step (6) check collision. Note this has nothing to do with OpenGL.
		if (checkCollision()) {
			JOptionPane.showMessageDialog(this.glPanel, "Game is Over");
			this.gameState.resetGameState();
			this.carCameraTranslation = new Vec(0.0);
		}

	}

	/**
	 * @return Checks if the car intersects the one of the boxes on the track.
	 */
	private boolean checkCollision() {
		// TODO: Implement this function to check if the car collides into one of the boxes.
		// You can get the bounding spheres of the track by invoking: 
		// List<BoundingSphere> trackBoundingSpheres = gameTrack.getBoundingSpheres();
		return false;
	}

	private void updateCarCameraTranslation(GL2 gl) {
		// Update the car and camera translation values (not the ModelView-Matrix).
		// - Always keep track of the car offset relative to the starting
		// point.
		// - Change the track segments here.
		Vec ret = gameState.getNextTranslation();
		carCameraTranslation = carCameraTranslation.add(ret);
		double dx = Math.max(carCameraTranslation.x, -TrackSegment.ASPHALT_TEXTURE_DEPTH / 2.0 - 2);
		carCameraTranslation.x = (float) Math.min(dx, TrackSegment.ASPHALT_TEXTURE_DEPTH / 2.0 + 2);
		if (Math.abs(carCameraTranslation.z) >= TrackSegment.TRACK_LENGTH + 10.0) {
			carCameraTranslation.z = -(float) (Math.abs(carCameraTranslation.z) % TrackSegment.TRACK_LENGTH);
			gameTrack.changeTrack(gl);
		}
	}

	private void setupCamera(GL2 gl) {
		// TODO: You are advised to use :
		//       GLU glu = new GLU();
		//       glu.gluLookAt();
		if (isBirdseyeView) {
			// TODO Setup camera for Birds-eye view
		} else {
			// TODO Setup camera for Third-person view
		}

	}

	private void setupLights(GL2 gl) {
		if (isDayMode) {
			// TODO Setup day lighting.
			// * Remember: switch-off any light sources that were used in night mode and are not use in day mode.
		} else {
			// TODO Setup night lighting.
			// * Remember: switch-off any light sources that are used in day mode
			// * Remember: spotlight sources also move with the camera.
			// * You may simulate moon-light using ambient light.
		}

	}

	private void renderTrack(GL2 gl) {
		// * Note: the track is not translated. It should be fixed.
		gl.glPushMatrix();
		gameTrack.render(gl);
		gl.glPopMatrix();
	}

	private void renderCar(GL2 gl) {
		// TODO: Render the car.
		// * Remember: the car position should be the initial position + the accumulated translation.
		//             This will simulate the car movement.
		// * Remember: the car was modeled locally, you may need to rotate/scale and translate the car appropriately.
		// * Recommendation: it is recommended to define fields (such as car initial position) that can be used during rendering.
	}

	public GameState getGameState() {
		return gameState;
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();

		// Initialize display callback timer
		ani = new FPSAnimator(30, true);
		ani.add(drawable);
		glPanel.repaint();

		initModel(gl);
		ani.start();
	}

	public void initModel(GL2 gl) {
		gl.glCullFace(GL2.GL_BACK);
		gl.glEnable(GL2.GL_CULL_FACE);

		gl.glEnable(GL2.GL_NORMALIZE);
		gl.glEnable(GL2.GL_DEPTH_TEST);
		gl.glEnable(GL2.GL_LIGHTING);
		gl.glEnable(GL2.GL_SMOOTH);

		car.init(gl);
		gameTrack.init(gl);
		isModelInitialized = true;
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		// TODO Setup the projection matrix here.
	}

	/**
	 * Start redrawing the scene with 30 FPS
	 */
	public void startAnimation() {
		if (!ani.isAnimating())
			ani.start();
	}

	/**
	 * Stop redrawing the scene with 30 FPS
	 */
	public void stopAnimation() {
		if (ani.isAnimating())
			ani.stop();
	}

	public void toggleNightMode() {
		isDayMode = !isDayMode;
	}

	public void changeViewMode() {
		isBirdseyeView = !isBirdseyeView;
	}

}
