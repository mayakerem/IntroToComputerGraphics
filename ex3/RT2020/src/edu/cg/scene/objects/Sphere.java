package edu.cg.scene.objects;

import edu.cg.algebra.Hit;
import edu.cg.algebra.Ray;
import edu.cg.algebra.Vec;
import edu.cg.algebra.Point;

public class Sphere extends Shape {
	private Point center;
	private double radius;
	private final double SMALL_NUMBER = 1.0E-5;
	private final double LARGE_NUMBER = 1.0E8;
	
	public Sphere(Point center, double radius) {
		this.center = center;
		this.radius = radius;
	}
	
	public Sphere() {
		this(new Point(0, -0.5, -6), 0.5);
	}
	
	@Override
	public String toString() {
		String endl = System.lineSeparator();
		return "Sphere:" + endl + 
				"Center: " + center + endl +
				"Radius: " + radius + endl;
	}
	
	public Sphere initCenter(Point center) {
		this.center = center;
		return this;
	}
	
	public Sphere initRadius(double radius) {
		this.radius = radius;
		return this;
	}
	// Finding intersection with object
	@Override
    public Hit intersect(Ray ray) {
        final double b = ray.direction().mult(2.0).dot(ray.source().sub(this.center));
        final double c = this.substitute(ray.source());
        // The discriminant is 0 there is 1 intersection, if its less than 0
        // then there are no solutions - else there are two solutions
        final double discriminant = Math.sqrt(Math.pow(b,2) - 4.0 * c);
        
        // No intersecting points
        if (Double.isNaN(discriminant)) {
            return null;
        }
        
        final double x1 = (-b - discriminant) / 2.0;
        final double x2 = (-b + discriminant) / 2.0;
        
        double minIntersectionPoint = x1;
        Vec normalVector = this.normalVector(ray.add(x1));
        boolean included = false;
        
        
        if (x2 < SMALL_NUMBER) {
            return null;
        }
        
        if (x1 < SMALL_NUMBER) {
            minIntersectionPoint = x2;
            normalVector = this.normalVector(ray.add(x2)).neg();
            included = true;
        }
        
        if (minIntersectionPoint > LARGE_NUMBER) {
            return null;
        }
        
        return new Hit(minIntersectionPoint, normalVector).setIsWithin(included);
    }
	
	private Vec normalVector(final Point p) {
        return p.sub(this.center).normalize();
    }
    
    public double substitute(final Point p) {
        return p.distSqr(this.center) - Math.pow(this.radius,2);
    }
}
