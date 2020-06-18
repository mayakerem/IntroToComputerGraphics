package edu.cg.scene;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import edu.cg.Logger;
import edu.cg.UnimplementedMethodException;
import edu.cg.algebra.Hit;
import edu.cg.algebra.Ops;
import edu.cg.algebra.Point;
import edu.cg.algebra.Ray;
import edu.cg.algebra.Vec;
import edu.cg.scene.camera.PinholeCamera;
import edu.cg.scene.lightSources.Light;
import edu.cg.scene.objects.Surface;

public class Scene {
	private String name = "scene";
	private int maxRecursionLevel = 1;
	private int antiAliasingFactor = 1; // gets the values of 1, 2 and 3
	private boolean renderRefarctions = false;
	private boolean renderReflections = false;

	private PinholeCamera camera;
	private Vec ambient = new Vec(1, 1, 1); //white
	private Vec backgroundColor = new Vec(0, 0.5, 1); // blue sky
	private List<Light> lightSources = new LinkedList<>();
	private List<Surface> surfaces = new LinkedList<>();

	// MARK: initializers
	public Scene initCamera(Point eyePoistion, Vec towardsVec, Vec upVec, double distanceToPlain) {
		this.camera = new PinholeCamera(eyePoistion, towardsVec, upVec, distanceToPlain);
		return this;
	}

	public Scene initAmbient(Vec ambient) {
		this.ambient = ambient;
		return this;
	}

	public Scene initBackgroundColor(Vec backgroundColor) {
		this.backgroundColor = backgroundColor;
		return this;
	}

	public Scene addLightSource(Light lightSource) {
		lightSources.add(lightSource);
		return this;
	}

	public Scene addSurface(Surface surface) {
		surfaces.add(surface);
		return this;
	}

	public Scene initMaxRecursionLevel(int maxRecursionLevel) {
		this.maxRecursionLevel = maxRecursionLevel;
		return this;
	}

	public Scene initAntiAliasingFactor(int antiAliasingFactor) {
		this.antiAliasingFactor = antiAliasingFactor;
		return this;
	}

	public Scene initName(String name) {
		this.name = name;
		return this;
	}

	public Scene initRenderRefarctions(boolean renderRefarctions) {
		this.renderRefarctions = renderRefarctions;
		return this;
	}

	public Scene initRenderReflections(boolean renderReflections) {
		this.renderReflections = renderReflections;
		return this;
	}

	// MARK: getters
	public String getName() {
		return name;
	}

	public int getFactor() {
		return antiAliasingFactor;
	}

	public int getMaxRecursionLevel() {
		return maxRecursionLevel;
	}

	public boolean getRenderRefarctions() {
		return renderRefarctions;
	}

	public boolean getRenderReflections() {
		return renderReflections;
	}

	@Override
	public String toString() {
		String endl = System.lineSeparator();
		return "Camera: " + camera + endl + 
				"Ambient: " + ambient + endl + 
				"Background Color: " + backgroundColor + endl + 
				"Max recursion level: " + maxRecursionLevel + endl + 
				"Anti aliasing factor: " + antiAliasingFactor+ endl + 
				"Light sources:" + endl + lightSources + endl + 
				"Surfaces:" + endl + surfaces;
	}

	private transient ExecutorService executor = null;
	private transient Logger logger = null;

	private void initSomeFields(int imgWidth, int imgHeight, Logger logger) {
		this.logger = logger;
	}

	public BufferedImage render(int imgWidth, int imgHeight, double viewAngle, Logger logger)
			throws InterruptedException, ExecutionException, IllegalArgumentException {

		initSomeFields(imgWidth, imgHeight, logger);

		BufferedImage img = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
		camera.initResolution(imgHeight, imgWidth, viewAngle);
		int nThreads = Runtime.getRuntime().availableProcessors();
		nThreads = nThreads < 2 ? 2 : nThreads;
		this.logger.log("Intitialize executor. Using " + nThreads + " threads to render " + name);
		executor = Executors.newFixedThreadPool(nThreads);

		@SuppressWarnings("unchecked")
		Future<Color>[][] futures = (Future<Color>[][]) (new Future[imgHeight][imgWidth]);

		this.logger.log("Starting to shoot " + (imgHeight * imgWidth * antiAliasingFactor * antiAliasingFactor)
				+ " rays over " + name);

		for (int y = 0; y < imgHeight; ++y)
			for (int x = 0; x < imgWidth; ++x)
				futures[y][x] = calcColor(x, y);

		this.logger.log("Done shooting rays.");
		this.logger.log("Wating for results...");

		for (int y = 0; y < imgHeight; ++y)
			for (int x = 0; x < imgWidth; ++x) {
				Color color = futures[y][x].get();
				img.setRGB(x, y, color.getRGB());
			}

		executor.shutdown();

		this.logger.log("Ray tracing of " + name + " has been completed.");

		executor = null;
		this.logger = null;

		return img;
	}

	private Future<Color> calcColor(int x, int y) {
		return executor.submit(() -> {
			Point centerPoint = camera.transform(x, y);
			Ray ray = new Ray(camera.getCameraPosition(), centerPoint);
			Vec color = calcColor(ray, 0);
			return color.toColor();
		});
	}
	// This method calculates the color per ray and will be called recursively
	private Vec calcColor(Ray ray, int recusionLevel) {
		// If the number of recursive ray is larger than the max possible
		if (recusionLevel >= this.maxRecursionLevel) {
			// return vector
			return new Vec();
		}
		
		// Initializing intersection ray
		final Hit minimalIntersection = this.intersection(ray);
		// If didn't hit the object then no intersection occurred.
	    if (minimalIntersection == null) {
			// Returning background color like assignment tells us to
	    	return this.backgroundColor;
	    }
	    
	    // Defining point and surface
	    final Point intersectionPoint = ray.getHittingPoint(minimalIntersection);
	    final Surface intersectionSurface = minimalIntersection.getSurface();
	    
	    // Get surface color
	    Vec finalColor = intersectionSurface.Ka().mult(this.ambient);
	    // Iterate over all light sources
	    for (final Light lightSource : this.lightSources) {
	   
	    	final Ray rayToLight = lightSource.rayToLight(intersectionPoint);
	    	
	        if (!this.isOccluded(lightSource, rayToLight)) {
	        	Vec colorTemp = this.diffuse(minimalIntersection, rayToLight);
	        	
	            colorTemp = colorTemp.add(this.mirrorReflection(minimalIntersection, rayToLight, ray));
	            
	            final Vec intensityColor = lightSource.intensity(intersectionPoint, rayToLight);
	            // Adjusting final color
	            finalColor = finalColor.add(colorTemp.mult(intensityColor));
	        }
	    }
	    
	    // After iterating over all light sources, we now consider reflections
	    if (this.renderReflections) {
	       final Vec reflectionDirection = Ops.reflect(ray.direction(), minimalIntersection.getNormalToSurface());
	       final Vec reflectionWeight = new Vec(intersectionSurface.reflectionIntensity());
	       final Vec reflectionColor = this.calcColor(new Ray(intersectionPoint, reflectionDirection), recusionLevel + 1).mult(reflectionWeight);
           // Adjusting final color
	       finalColor = finalColor.add(reflectionColor);
	    }
	    // Now we consider refractions
	    if (this.renderRefarctions) {
	    	Vec refractionColor = new Vec();
	        if (intersectionSurface.isTransparent()) {
	        	final double n1 = intersectionSurface.n1(minimalIntersection);
	        	final double n2 = intersectionSurface.n2(minimalIntersection);
	            final Vec refractionDirection = Ops.refract(ray.direction(), minimalIntersection.getNormalToSurface(), n1, n2);
	            final Vec refractionWeight = new Vec(intersectionSurface.refractionIntensity());
	            refractionColor = this.calcColor(new Ray(intersectionPoint, refractionDirection), recusionLevel + 1).mult(refractionWeight);
	            // Adjusting final color
	            finalColor = finalColor.add(refractionColor);
	            }
	        }
	    
	    return finalColor;
	}


	private Vec mirrorReflection(Hit minHit, Ray rayToLight, Ray ray) {
		final Vec direction = rayToLight.direction();
        final Vec normalVector = minHit.getNormalToSurface();
        final Vec reflection = Ops.reflect(direction.neg(), normalVector);
        final Vec Ks = minHit.getSurface().Ks();
        final Vec directionVector = ray.direction();
        final int shininess = minHit.getSurface().shininess();
        final double dotProduct = reflection.dot(directionVector.neg());
        if (dotProduct < 0.0) {
        	return new Vec();
        } else {
        	return Ks.mult(Math.pow(dotProduct, shininess));
        }
    }


	private Vec diffuse(Hit minHit, Ray rayToLight) {
		final Vec direction = rayToLight.direction();
        final Vec normalVector = minHit.getNormalToSurface();
        final Vec Kd = minHit.getSurface().Kd();
        return Kd.mult(Math.max(normalVector.dot(direction), 0.0));
    }
	
	// Occluded -> Hidden by another object
	private boolean isOccluded(Light light, Ray rayToLight) {
		for (final Surface surface : this.surfaces) {
			if (light.isOccludedBy(surface, rayToLight)) {
				return true;
	        }
	    }
	    return false;
    }

	private Hit intersection(Ray ray) {
		// TODO Auto-generated method stub
	     Hit minimalIntersection = null;
	        for (final Surface surface : this.surfaces) {
	            final Hit newHitSurface = surface.intersect(ray);
	            if (minimalIntersection == null || (newHitSurface != null && newHitSurface.compareTo(minimalIntersection) < 0)) {
	                minimalIntersection = newHitSurface;
	            }
	        }
	        return minimalIntersection;
	}

}
