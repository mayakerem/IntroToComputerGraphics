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
	private Vec ambient = new Vec(1, 1, 1); // white
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
		return "Camera: " + camera + endl + "Ambient: " + ambient + endl + "Background Color: " + backgroundColor + endl
				+ "Max recursion level: " + maxRecursionLevel + endl + "Anti aliasing factor: " + antiAliasingFactor
				+ endl + "Light sources:" + endl + lightSources + endl + "Surfaces:" + endl + surfaces;
	}

	private transient ExecutorService executor = null;
	private transient Logger logger = null;

	private void initSomeFields(int imgWidth, int imgHeight, Logger logger) {
		this.logger = logger;
		// TODO: initialize your additional field here.
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
			// TODO: You need to re-implement this method if you want to handle
			// super-sampling. You're also free to change the given implementation if you
			// want.
		
			

			Point centerPoint = camera.transform(x, y);
			Ray ray = new Ray(camera.getCameraPosition(), centerPoint);
			Vec color = calcColor(ray, 0);
			return color.toColor();
		});
	}

	private Vec calcColor(Ray ray, int recusionLevel) {
		// TODO: Implement this method.
		// This is the recursive method in RayTracing.
		
		// If the number of recursive ray is larger than the max possible
		if (recusionLevel >= this.maxRecursionLevel) {
			// return vector
			return new Vec();
		}
		// Initializing intersection ray
		final Hit minHit = this.intersection(ray);
		// If didnt hit the object then no intersection occured.
		// Returning background color like assignment tells us to
	    if (minHit == null) {
	        return this.backgroundColor;
	    }
	    // Defining point and surface
	    final Point hittingPoint = ray.getHittingPoint(minHit);
	    final Surface surface = minHit.getSurface();
	    // Get surface color
	    Vec color = surface.Ka().mult(this.ambient);
	    // Iterate over all light sources
	    for (final Light light : this.lightSources) {
	    	final Ray rayToLight = light.rayToLight(hittingPoint);
	    	
	        if (!this.isOccluded(light, rayToLight)) {
	        	Vec tmpColor = this.diffuse(minHit, rayToLight);
	            tmpColor = tmpColor.add(this.specular(minHit, rayToLight, ray));
	            final Vec Il = light.intensity(hittingPoint, rayToLight);
	            color = color.add(tmpColor.mult(Il));
	        }
	    }
	    
	    // After iterating over all light sources, we now consider reflections
	    if (this.renderReflections) {
	       final Vec reflectionDirection = Ops.reflect(ray.direction(), minHit.getNormalToSurface());
	       final Vec reflectionWeight = new Vec(surface.reflectionIntensity());
	       final Vec reflectionColor = this.calcColor(new Ray(hittingPoint, reflectionDirection), recusionLevel + 1).mult(reflectionWeight);
	       color = color.add(reflectionColor);
	    }
	    if (this.renderRefarctions) {
	    	Vec refractionColor = new Vec();
	        if (surface.isTransparent()) {
	        	final double n1 = surface.n1(minHit);
	        	final double n2 = surface.n2(minHit);
	            final Vec refractionDirection = Ops.refract(ray.direction(), minHit.getNormalToSurface(), n1, n2);
	            final Vec refractionWeight = new Vec(surface.refractionIntensity());
	            refractionColor = this.calcColor(new Ray(hittingPoint, refractionDirection), recusionLevel + 1).mult(refractionWeight);
	                color = color.add(refractionColor);
	            }
	        }
	        return color;
		
		
	}


	private Vec specular(Hit minHit, Ray rayToLight, Ray ray) {
		final Vec L = rayToLight.direction();
        final Vec N = minHit.getNormalToSurface();
        final Vec R = Ops.reflect(L.neg(), N);
        final Vec Ks = minHit.getSurface().Ks();
        final Vec v = ray.direction();
        final int shininess = minHit.getSurface().shininess();
        final double dot = R.dot(v.neg());
        return (dot < 0.0) ? new Vec() : Ks.mult(Math.pow(dot, shininess));
    }

	// The following are Newly added functions
	

	private Vec diffuse(Hit minHit, Ray rayToLight) {
		final Vec L = rayToLight.direction();
        final Vec N = minHit.getNormalToSurface();
        final Vec Kd = minHit.getSurface().Kd();
        return Kd.mult(Math.max(N.dot(L), 0.0));
    }
	
	private boolean isOccluded(Light light, Ray rayToLight) {
		// TODO Auto-generated method stub
		for (final Surface surface : this.surfaces) {
			if (light.isOccludedBy(surface, rayToLight)) {
				return true;
	        }
	    }
	    return false;
    }

	private Hit intersection(Ray ray) {
		// TODO Auto-generated method stub
	     Hit minHit = null;
	        for (final Surface surface : this.surfaces) {
	            final Hit newHit = surface.intersect(ray);
	            if (minHit == null || (newHit != null && newHit.compareTo(minHit) < 0)) {
	                minHit = newHit;
	            }
	        }
	        return minHit;
	   
	}
}
