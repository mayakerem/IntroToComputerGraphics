package edu.cg.algebra;

import edu.cg.UnimplementedMethodException;

//import ex3.UnimplementedMethodException;

public class Ops {
	public static final double epsilon = 1e-5;
	public static final double infinity = 1e8;

	public static double dot(Vec u, Vec v) {
		return u.x * v.x + u.y * v.y + u.z * v.z;
	}

	public static Vec cross(Vec u, Vec v) {
		return new Vec((u.y * v.z - u.z * v.y), (u.z * v.x - u.x * v.z), (u.x * v.y - u.y * v.x));
	}

	public static Vec mult(double a, Vec v) {
		return mult(new Vec(a), v);
	}

	public static Vec mult(Vec u, Vec v) {
		return new Vec(u.x * v.x, u.y * v.y, u.z * v.z);
	}

	public static Point mult(double a, Point p) {
		return mult(new Point(a), p);
	}

	public static Point mult(Point p1, Point p2) {
		return new Point(p1.x * p2.x, p1.y * p2.y, p1.z * p2.z);
	}

	public static double normSqr(Vec v) {
		return dot(v, v);
	}

	public static double norm(Vec v) {
		return Math.sqrt(normSqr(v));
	}

	public static double lengthSqr(Vec v) {
		return normSqr(v);
	}

	public static double length(Vec v) {
		return norm(v);
	}

	public static double dist(Point p1, Point p2) {
		return length(sub(p1, p2));
	}

	public static double distSqr(Point p1, Point p2) {
		return lengthSqr(sub(p1, p2));
	}

	public static Vec normalize(Vec v) {
		return mult(1.0 / norm(v), v);
	}

	public static Vec neg(Vec v) {
		return mult(-1, v);
	}

	public static Vec add(Vec u, Vec v) {
		return new Vec(u.x + v.x, u.y + v.y, u.z + v.z);
	}

	public static Point add(Point p, Vec v) {
		return new Point(p.x + v.x, p.y + v.y, p.z + v.z);
	}

	public static Point add(Point p1, Point p2) {
		return new Point(p1.x + p2.x, p1.y + p2.y, p1.z + p2.z);
	}

	public static Point add(Point p, double t, Vec v) {
		// returns p + tv;
		return add(p, mult(t, v));
	}

	public static Vec sub(Point p1, Point p2) {
		return new Vec(p1.x - p2.x, p1.y - p2.y, p1.z - p2.z);
	}

	public static boolean isFinite(Vec v) {
		return Double.isFinite(v.x) & Double.isFinite(v.y) & Double.isFinite(v.z);
	}

	public static boolean isFinite(Point p) {
		return Double.isFinite(p.x) & Double.isFinite(p.y) & Double.isFinite(p.z);
	}

	public static Vec reflect(Vec u, Vec normal) {
		return add(u, mult(-2 * dot(u, normal), normal));
	}

	/**
	 * Returns the refraction of the vector u.
	 * 
	 * @param u      the light vector direction.
	 * @param normal The normal of the surface at the intersection point
	 * @param n1     the refraction index of the first medium
	 * @param n2     the refraction index of the second medium
	 * @return
	 */
	public static Vec refract(Vec u, Vec normal, double n1, double n2) {
		if (n1 == n2) {
            return u;
        }
		
		double dotProduct = Math.pow(Ops.dot(neg(u), normal),2);
        
        if (n1 > n2) {
            double theta1 = Math.pow(n2 / n1,2);
            
            if ((1.0 - dotProduct) >= theta1) {
                return reflect(u, normal);
            }
        }
        Vec p = add(u, mult(dot(neg(u), normal), normal));
        p = normalize(p);
        
        final double sinTheta2 = Math.sqrt(Math.pow(n1, 2) * (1.0 - dotProduct) / Math.pow(n2,2));
        final double cosTheta2 = Math.sqrt( 1.0 - Math.pow(n1, 2) * (1.0 - dotProduct) / Math.pow(n2,2));
        
        // Addition between two vectors
        Vec resultVector = add(mult(-cosTheta2, normal), mult(sinTheta2, p));
        return  resultVector;
    }
	
}
