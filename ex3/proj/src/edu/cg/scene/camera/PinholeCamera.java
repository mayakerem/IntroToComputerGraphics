package edu.cg.scene.camera;

import edu.cg.UnimplementedMethodException;
import edu.cg.algebra.Point;
import edu.cg.algebra.Ray;
import edu.cg.algebra.Vec;

public class PinholeCamera {

	Point cameraPosition;
    Point centerPoint;
    Vec towardsVec;
    Vec upVec;
    Vec rightVec;
    double angleView;
    double distanceToPlain;
    double resolutionX;
    double resolutionY;
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
		this.cameraPosition = cameraPosition;
        this.towardsVec = towardsVec.normalize();
        this.rightVec = this.towardsVec.cross(upVec).normalize();
        this.upVec = this.rightVec.cross(this.towardsVec).normalize();
        this.distanceToPlain = distanceToPlain;
        this.centerPoint = new Ray(cameraPosition, towardsVec).add(distanceToPlain);
        this.resolutionX = 200.0;
        this.resolutionY = 200.0;
        this.angleView = 2.0;
        
	}

	/**
	 * Initializes the resolution and width of the image.
	 * 
	 * @param height    - the number of pixels in the y direction.
	 * @param width     - the number of pixels in the x direction.
	 * @param viewAngle - the view Angle.
	 */
	public void initResolution(int height, int width, double viewAngle) {
		this.angleView = viewAngle;
        this.resolutionX = width;
        this.resolutionY = height;
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
		final double viewPlainWidth = 2.0 * Math.tan(Math.toRadians(this.angleView / 2.0)) * this.distanceToPlain;
        final double pixelHeight;
        final double pixelWidth = pixelHeight = viewPlainWidth / this.resolutionX;
        final double upDistance = -1 * (y - (int)(this.resolutionY / 2.0)) * pixelHeight;
        final double rightDistance = (x - (int)(this.resolutionX / 2.0)) * pixelWidth;
        final Vec upMovement = this.upVec.mult(upDistance);
        final Vec rightMovement = this.rightVec.mult(rightDistance);
        final Point midPoint = this.centerPoint.add(upMovement).add(rightMovement);
        return midPoint;
        }

	/**
	 * Returns the camera position
	 * 
	 * @return a new point representing the camera position.
	 */
	public Point getCameraPosition() {
		return new Point(this.cameraPosition.x, this.cameraPosition.y, this.cameraPosition.z);
		}
}
