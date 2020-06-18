package edu.cg.scene.objects;

import edu.cg.UnimplementedMethodException;
import edu.cg.algebra.Hit;
import edu.cg.algebra.Point;
import edu.cg.algebra.Ray;
import edu.cg.algebra.Vec;

public class Dome extends Shape {
	private Sphere sphere;
	private Plain plain;
	private final double SMALL_NUMBER = 1.0E-5;
	
	public Dome() {
		sphere = new Sphere().initCenter(new Point(0, -0.5, -6));
		plain = new Plain(new Vec(-1, 0, -1), new Point(0, -0.5, -6));
	}

	public Dome(Point center, double radious, Vec plainDirection) {
		sphere = new Sphere(center, radious);
		plain = new Plain(plainDirection, center);
	}

	@Override
	public String toString() {
		String endl = System.lineSeparator();
		return "Dome:" + endl + sphere + plain + endl;
	}

	@Override
    public Hit intersect(final Ray ray) {
        final Hit intersection = this.sphere.intersect(ray);
        // If no intersection then return null
        if (intersection == null) {
            return null;
        }
        //if hit is within the surface, then hit from inside, outside otherwise
        if (intersection.isWithinTheSurface()) {
        	return  (this.insideIntersection(ray, intersection));
        } else {
        	return (this.outsideIntersection(ray, intersection));
        }
    }
    
    private Hit outsideIntersection(final Ray ray, Hit intersection) {
        Point intersectionPoint = ray.getHittingPoint(intersection);
        if (this.plain.substitute(intersectionPoint) > 0) {
            return intersection;
        }
        intersection = this.plain.intersect(ray);
        if (intersection == null) {
            return null;
        }
        intersectionPoint = ray.getHittingPoint(intersection);
        if (this.sphere.substitute(intersectionPoint) < 0) {
            return intersection;
        }
        return null;
    }
    
    private Hit insideIntersection(final Ray ray, Hit intersection) {
        final Point intersectionPoint = ray.getHittingPoint(intersection);
        if (this.plain.substitute(ray.source()) > SMALL_NUMBER) {
            if (this.plain.substitute(intersectionPoint) > 0) {
                return intersection;
            }
            intersection = this.plain.intersect(ray);
            if (intersection == null) {
                return null;
            }
            return intersection.setWithin();
        }
        else {
            if (this.plain.substitute(intersectionPoint) > 0) {
                return this.plain.intersect(ray);
            }
            return null;
        }
    }
}