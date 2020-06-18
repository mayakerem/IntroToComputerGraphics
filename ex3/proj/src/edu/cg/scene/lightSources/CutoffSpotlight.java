package edu.cg.scene.lightSources;

import edu.cg.UnimplementedMethodException;
import edu.cg.algebra.Ops;
import edu.cg.algebra.Point;
import edu.cg.algebra.Ray;
import edu.cg.algebra.Vec;
import edu.cg.scene.objects.Surface;

public class CutoffSpotlight extends PointLight {
	private Vec direction;
	private double cutoffAngle;
	private final double SMALL_NUMBER = 1.0E-5;

	public CutoffSpotlight(Vec dirVec, double cutoffAngle) {
		this.direction = dirVec;
		this.cutoffAngle = cutoffAngle;
	}

	public CutoffSpotlight initDirection(Vec direction) {
		this.direction = direction;
		return this;
	}

	public CutoffSpotlight initCutoffAngle(double cutoffAngle) {
		this.cutoffAngle = cutoffAngle;
		return this;
	}

	@Override
	public String toString() {
		String endl = System.lineSeparator();
		return "Spotlight: " + endl + description() + "Direction: " + direction + endl;
	}

	@Override
	public CutoffSpotlight initPosition(Point position) {
		return (CutoffSpotlight) super.initPosition(position);
	}

	@Override
	public CutoffSpotlight initIntensity(Vec intensity) {
		return (CutoffSpotlight) super.initIntensity(intensity);
	}

	@Override
	public CutoffSpotlight initDecayFactors(double q, double l, double c) {
		return (CutoffSpotlight) super.initDecayFactors(q, l, c);
	}

	@Override
	public boolean isOccludedBy(Surface surface, Ray rayToLight) {
		return rayToLight.direction().neg().dot(this.direction.normalize()) < SMALL_NUMBER || super.isOccludedBy(surface, rayToLight);
 
	}

	@Override
	public Vec intensity(Point hittingPoint, Ray rayToLight) {
		
		final Vec direction = this.direction.normalize().neg();
        final Vec lightRay = rayToLight.direction().normalize();
        final double dotProduct = direction.dot(lightRay);
        final double epsilon = Math.toDegrees(Math.acos(dotProduct));
        
        if (dotProduct < SMALL_NUMBER || epsilon > this.cutoffAngle) {
            return new Vec(0.0);
        }
        return super.intensity(hittingPoint, rayToLight).mult(dotProduct);

}
}
