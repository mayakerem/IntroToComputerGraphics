package edu.cg.scene.camera;

import edu.cg.UnimplementedMethodException;
import edu.cg.algebra.Point;
import edu.cg.algebra.Ray;
import edu.cg.algebra.Vec;

public class PinholeCamera {
	// TODO Add your fields
	Point cameraPosition;
    Point centerPoint;
    Vec towardsVec;
    Vec upVec;
    Vec rightVec;
    double viewAngle;
    double distanceToPlain;
    double resX;
    double resY;
    double pixelRatio;       
	/**
	 * Initializes a pinhole camera model with default resolution 200X200 (RxXRy)
	 * and View Angle 90.
	 * 
	 * @param cameraPosition  - The position of the camera.
	 * @param towardsVec      - The towards vector of the camera (not necessarily
	 *                        normalized).
	 * @param upVec           - The up vector of the camera.
	 * @param distanceToPlain - The distance of the camera (position) to the center
	 *                        point of the image-plain.
	 * 
	 */
	public PinholeCamera(Point cameraPosition, Vec towardsVec, Vec upVec, double distanceToPlain) {
		// TODO: Initialize your fields
		this.cameraPosition = cameraPosition;
        this.towardsVec = towardsVec.normalize();
        this.rightVec = this.towardsVec.cross(upVec).normalize();
        this.upVec = this.rightVec.cross(this.towardsVec).normalize();
        this.distanceToPlain = distanceToPlain;
        this.centerPoint = new Ray(cameraPosition, towardsVec).add(distanceToPlain);
        this.resX = 200.0;
        this.resY = 200.0;
        this.viewAngle = 2.0;
        
	}

	/**
	 * Initializes the resolution and width of the image.
	 * 
	 * @param height    - the number of pixels in the y direction.
	 * @param width     - the number of pixels in the x direction.
	 * @param viewAngle - the view Angle.
	 */
	public void initResolution(int height, int width, double viewAngle) {
		// TODO: init your fields
		this.viewAngle = viewAngle;
        this.resX = width;
        this.resY = height;
	}

	/**
	 * Transforms from pixel coordinates to the center point of the corresponding
	 * pixel in model coordinates.
	 * 
	 * @param x - the pixel index in the x direction.
	 * @param y - the pixel index in the y direction.
	 * @return the middle point of the pixel (x,y) in the model coordinates.
	 */
	public Point transform(int x, int y) {
		// TODO: implement this method.
		final double viewPlainWidth = 2.0 * Math.tan(Math.toRadians(this.viewAngle / 2.0)) * this.distanceToPlain;
        final double pixelHeight;
        final double pixelWidth = pixelHeight = viewPlainWidth / this.resX;
        final double upDistance = (y - (int)(this.resY / 2.0)) * pixelHeight * -1.0;
        final double rightDistance = (x - (int)(this.resX / 2.0)) * pixelWidth;
        final Vec upMovement = this.upVec.mult(upDistance);
        final Vec rightMovement = this.rightVec.mult(rightDistance);
        final Point fovPoint = this.centerPoint.add(upMovement).add(rightMovement);
        return fovPoint;
        }

	/**
	 * Returns the camera position
	 * 
	 * @return a new point representing the camera position.
	 */
	public Point getCameraPosition() {
		// TODO: implement this method.
		return new Point(this.cameraPosition.x, this.cameraPosition.y, this.cameraPosition.z);
		}
}
