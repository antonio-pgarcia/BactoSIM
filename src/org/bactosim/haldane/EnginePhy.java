package org.bactosim.haldane;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.vecmath.Point2i;

import org.apache.commons.math3.geometry.euclidean.threed.Line;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.sun.security.auth.UnixNumericUserPrincipal;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.collections.IndexedIterable;
import repast.simphony.visualization.visualization2D.Random2DLayout;

@SuppressWarnings({"rawtypes", "unchecked"})
public class EnginePhy {
	private double x;
	private double y;
	private int xmax;
	private int ymax;
	
	private Grid grid;
	private ContinuousSpace space;
	private Class clazz = null;
	
	public EnginePhy(double xx, double yy, int xxmax, int yymax) {
		x = xx;
		y = yy;
		xmax = xxmax;
		ymax = yymax;
		clazz = (new VEColi()).getClass(); 
	}
	
	//@ScheduledMethod(start = 1, interval = 2, shuffle=true)
	public void step1() {
		Context context = (Context) ContextUtils.getContext(this);
		IndexedIterable<VEColi> agents = context.getObjects(clazz);
		
		space = (ContinuousSpace) context.getProjection("continuous-space");
		grid = (Grid) context.getProjection("grid-space");
		
		GridPoint p;
		NdPoint point;
				
		//for (Object o: grid.getObjectsAt(p.getX(),p.getY())){
		for (VEColi agent : agents) {
			if(agent.getElongation() > 0.6) {
				p = grid.getLocation(agent);
				point = space.getLocation(agent);
				System.out.println("[" + p.getX() + "," + p.getY() + "]" + "[" + point.getX() + "," + point.getY() + "]" + " >heading= " + agent.getHeading() + " length= " + agent.getElongation() + " Neighborhood= " + count(p.getX(), p.getY()));
				distance(p.getX(), p.getY());
			}
		}
	}
	
	@ScheduledMethod(start = 1, interval = 10, shuffle=true)
	public void step() {
		RingVisitor();
	}
	
	public void RingVisitor() {
		int counter= 0;
		double rings = Math.min(Math.min(xmax - x, x), Math.min(ymax - y, y));
		ColonyRing cr= new ColonyRing((int) x, (int)y);
		
		// Ring loop
		for(int ring = 0; ring < rings; ring++) {
			// Ring components loop
			
			counter= ColonyEdge.agentCount(this, cr.Items(ring));
			if(ring > 1 && counter == 0) break;
			
			ColonyExpansion.Relaxation(this, cr.Items(ring));
		}
		
		
	}
	
	public int count(int x, int y) {
		int v= 0;
		for (@SuppressWarnings("unused") Object o: grid.getObjectsAt(x, y)) {
			v++;
		}
		return v;
	}
	
	public double distance(int x, int y) {
		double v= 0;
		if(count(x,y) > 1) {
			for (Object a1: grid.getObjectsAt(x, y)) {
				for (Object a2: grid.getObjectsAt(x, y)) {
					if(!a1.equals(a2)) {
						v= Agent.distance((VEColi) a1, (VEColi) a2);
						System.out.println(" /Distance a1-a2: " + v);
					}
				}
			}
			
		}
		return v;
	}

}


/**
 * This class calculates the distance between two 
 * agents
 * TODO: Improve and make it a public class     
 */
@SuppressWarnings({"rawtypes"})
class Agent {
	
	public static double heading(Vector3D v) {
		return Angle.convheading(Math.toDegrees(Math.atan2((v.getY()),(v.getX()))));
	}
	
	public static double heading0(VEColi a1) {
		Vector3D v1 = vector0(a1);
		Vector3D v2 = vector1(a1); 
		return heading(v2.subtract(v1).normalize());
	}

	public static Vector3D vector(VEColi a1) {
		Vector3D point0 = vector0(a1);
		Vector3D point1 = vector1(a1); 
		return point1.subtract(point0);
	}
	
	public static double heading11(VEColi a1) {
		Line l = line(a1);
		
		double alpha = Math.atan2( l.getDirection().getY() , l.getDirection().getX());
		System.out.println("> " + Math.toDegrees(l.getDirection().getAlpha()));
		double alphadeg = Math.toDegrees(alpha);
		double h = Angle.convheading(alphadeg);
		return h;
	}
	
	public static Line line(VEColi a1) {
		Vector3D v1 = vector0(a1);
		Vector3D v2 = vector1(a1);
		return (new Line(v1, v2, 1.0e-10));
	}
	

	public static Vector3D vector0(VEColi a1) {
		NdPoint p;
		Context context = (Context) ContextUtils.getContext(a1);
		ContinuousSpace space = (ContinuousSpace) context.getProjection("continuous-space");
		p= space.getLocation(a1);
		Vector3D v1 = new Vector3D(p.getX(), p.getY(), 0);
		return v1;
	}
	
	public static Vector3D vector1(VEColi a1) {
		Vector3D p = vector0(a1);
		double alpha = Math.toRadians(Angle.convheading(a1.getHeading()));
		Vector3D v2 = new Vector3D(p.getX() + a1.getLength() *  Math.cos(alpha), p.getY() + a1.getLength() * Math.sin(alpha), 0);
		return v2;
	}
	
	public static boolean collided(VEColi a1, VEColi a2, double overlap) {
		double closest = a1.getWidth() * (1 - overlap);
		return (distance(a1, a2) < closest);
	}
	
	public static double distance(VEColi a1, VEColi a2) {
		Vector3D uS = vector0(a1);
		Vector3D uE = vector1(a1);
		Vector3D vS = vector0(a2);
		Vector3D vE = vector1(a2);
		Vector3D w1 = vector0(a1);
		Vector3D w2 = vector0(a2);
		
		Vector3D u = uE.subtract(uS);
	    Vector3D v = vE.subtract(vS);
	    Vector3D w = w1.subtract(w2);
	    
	    double a = u.dotProduct(u);
	    double b = u.dotProduct(v);
	    double c = v.dotProduct(v);
	    double d = u.dotProduct(w);
	    double e = v.dotProduct(w);
	    double D = a * c - b * b;
	    double sc, sN, sD = D;
	    double tc, tN, tD = D;

	    if (D < 0.01) {
	    	sN = 0;
	        sD = 1;
	        tN = e;
	        tD = c;
	    } else {
	    	sN = (b * e - c * d);
	        tN = (a * e - b * d);
	        if (sN < 0) {
	        	sN = 0;
	            tN = e;
	            tD = c;
	        } else if (sN > sD) {
	        	sN = sD;
	            tN = e + b;
	            tD = c;
	        }
	    }
	        
	    if (tN < 0) {
	    	tN = 0;
	        if (-d < 0) {
	        	sN = 0;
	        } else if (-d > a) {
	        	sN = sD;
	        } else {
	        	sN = -d;
	            sD = a;
	        }
	    } else if (tN > tD) {
	    	tN = tD;
	    	if ((-d + b) < 0) {
	    		sN = 0;
	        } else if ((-d + b) > a) {
	        	sN = sD;
	        }
	        else {
	        	sN = (-d + b);
	            sD = a;
	        }
	    }
	    
	    if (Math.abs(sN) < 0.01) {
	    	sc = 0;
	    } else {
	    	sc = sN / sD;
	    }
	        
	    if (Math.abs(tN) < 0.01) {
	    	tc = 0;
	    } else {
	    	tc = tN / tD;
	    }
	    
	    Vector3D dP = w.add(u.scalarMultiply(sc).subtract((v.scalarMultiply(tc))));
	    return Math.sqrt(dP.dotProduct(dP));
	}
	
		       
}


class Angle {
	
	public static double convheading(double alpha) {
		return (450.0 - alpha) % 360.0;
	}
}
/**
 * This class provides Shoving on ring  
 * TODO: Improve and make it a public class     
 */
class ColonyExpansion {
	
	@SuppressWarnings("rawtypes")
	public static void Relaxation(Object o, Iterable<Point2i> ring) {
		Context context = (Context) ContextUtils.getContext(o);
		Grid grid = (Grid) context.getProjection("grid-space");
		ContinuousSpace space = (ContinuousSpace) context.getProjection("continuous-space");
		
		// Loop on ring boxes
		for(Point2i p: ring) {
			
			for (Object a1: grid.getObjectsAt(p.getX(), p.getY())) {
				for (Object a2: grid.getObjectsAt(p.getX(), p.getY())) {
					if(!(a1 instanceof VEColi) || !(a2 instanceof VEColi) || a1.equals(a2)) continue;
					if( Agent.collided((VEColi) a1, (VEColi) a2, 0.1) ) {
						Vector3D resultant = Agent.vector((VEColi) a1).add(Agent.vector((VEColi) a2));
						
						if(Agent.heading(resultant.normalize()) >= ((VEColi) a1).getHeading() ) {
							((VEColi) a1).setHeading( ((VEColi) a1).getHeading() + (15 * Math.random()) );
						} else {
							((VEColi) a1).setHeading( ((VEColi) a1).getHeading() - (15 * Math.random()) );
						} 
						
						
						space.moveTo(((VEColi) a1), space.getLocation(((VEColi) a1)).getX() + 0.01 * Math.random(), space.getLocation(((VEColi) a1)).getY() + 0.01 * Math.random() ,0); 
						//System.out.println(" /heading= " + h + " /dist= " + (Agent.vector1((VEColi) a1)).distanceSq(Agent.vector1((VEColi) a2)));
						//System.out.println("** dist= (" + Agent.distance((VEColi) a1, (VEColi) a2) + ") >[field] heading a1, a2= " + ((VEColi) a1).getHeading() + ", " + ((VEColi) a2).getHeading() + 	" >resultant= " + 
						//Agent.heading(resultant.normalize()) + " > magnitude= (" + resultant.getNorm() + ", " + resultant.getNormSq() + ")");
					}
				}
			}
		}
		
	}
}

/**
 * This class provides search colony edge  
 * TODO: Improve and make it a public class     
 */
class ColonyEdge {
	
	public static boolean isEdge(Object o, Iterable<Point2i> ring) {
		return (agentCount(o, ring) == 0);
	}
	
	public static int agentCount(Object o, Iterable<Point2i> ring) {
		//Context context = (Context) ContextUtils.getContext(o);
		//Grid grid = (Grid) context.getProjection("grid-space");
		int count= 0;
		
		for(Point2i p: ring) {
			count+= agentCount(o, p.getX(), p.getY());
		}
		return count;
	}
	
	@SuppressWarnings("rawtypes")
	public static int agentCount(Object o, int x, int y) {
		Context context = (Context) ContextUtils.getContext(o);
		Grid grid = (Grid) context.getProjection("grid-space");
		int count= 0;
		for (@SuppressWarnings("unused") Object a: grid.getObjectsAt(x, y)) {
			count++;
		}
		return count;
	}
}

/**
 * This class provides the iterable collection of 
 * colony ring coordinates
 * TODO: Improve and make it a public class     
 */
class ColonyRing {
	private int x= 0;
	private int y= 0;
	
	public ColonyRing() {
	}
	
	public ColonyRing(int xx, int yy) {
		x= xx;
		y= yy;
	}
	
	public Iterable<Point2i> Items(int r) {
		List<Point2i> list= new ArrayList<Point2i>();
		
		int rx= -r;
		int ry= 0;
		for(int i= 0; i < (r > 0 ? (r * 4) : 1); i++) {
			list.add(new Point2i(new int[] {x+rx,y+ry}));

			rx= (i % 2 == 0 ? rx + 1 : rx);
			ry= (i % 2 == 0 ? (r - Math.abs(rx)) : -(r - Math.abs(rx)));
		}
		Collections.shuffle(list);
		return list;
	}
}
