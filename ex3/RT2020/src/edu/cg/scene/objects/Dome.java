package edu.cg.scene.objects;

import edu.cg.UnimplementedMethodException;
import edu.cg.algebra.Hit;
import edu.cg.algebra.Point;
import edu.cg.algebra.Ray;
import edu.cg.algebra.Vec;

public class Dome extends Shape {
	private Sphere sphere;
	private Plain plain;

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

	
	
	// everything below here is new
	
	
	
	
	
	@Override
    public Hit intersect(final Ray ray) {
        final Hit hit = this.sphere.intersect(ray);
        if (hit == null) {
            return null;
        }
        return hit.isWithinTheSurface() ? this.hittingFromInside(ray, hit) : this.hittingFromOutside(ray, hit);
    }
    
    private Hit hittingFromOutside(final Ray ray, Hit hit) {
        Point hittingPoint = ray.getHittingPoint(hit);
        if (this.plain.substitute(hittingPoint) > 0.0) {
            return hit;
        }
        hit = this.plain.intersect(ray);
        if (hit == null) {
            return null;
        }
        hittingPoint = ray.getHittingPoint(hit);
        if (this.sphere.substitute(hittingPoint) < 0.0) {
            return hit;
        }
        return null;
    }
    
    private Hit hittingFromInside(final Ray ray, Hit hit) {
        final Point hittingPoint = ray.getHittingPoint(hit);
        if (this.plain.substitute(ray.source()) > 1.0E-5) {
            if (this.plain.substitute(hittingPoint) > 0.0) {
                return hit;
            }
            hit = this.plain.intersect(ray);
            if (hit == null) {
                return null;
            }
            return hit.setWithin();
        }
        else {
            if (this.plain.substitute(hittingPoint) > 0.0) {
                return this.plain.intersect(ray);
            }
            return null;
        }
    }
}