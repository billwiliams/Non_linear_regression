/**
 * Created by Bill on 1/23/2015.
 *A project to determine the Linear regression for maritime analytic using java
 * Modules such as apache commons maths libraries and Jfreechart are used for analysis and visualization
 */
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.MultiDirectionalSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.joda.time.DateTime;

public class Ian_trial {
	
	public static void main(String[] args) throws Exception {
		
		Track ownshipTrack = new Track("data/Scen1_Ownship.csv");
		Track targetTrack = new Track("data/Scen1_Target.csv");
		Sensor sensor = new Sensor("data/Scen1_Sensor.csv");
		
		// Now, we have to slice the data into ownship legs
		List<LegOfData> ownshipLegs = calculateLegs(ownshipTrack);
		
		long startTime = System.currentTimeMillis();
		
		// ok, work through the legs.  In the absence of a Discrete Optimisation algorithm we're taking a brue force approach.
		// Hopefully Craig can find an optimised alternative to this.
		for (Iterator<LegOfData> iterator = ownshipLegs.iterator(); iterator.hasNext();) {
			
			LegOfData thisLeg = (LegOfData) iterator.next();
			
			System.out.println(" handling leg:" + thisLeg);
			
			// ok, extract the relevant data
			List<Double> bearings = sensor.extractBearings(thisLeg.getStart(), thisLeg.getEnd());
			List<Long> times = sensor.extractTimes(thisLeg.getStart(), thisLeg.getEnd());
			
			// find the error score for the overall leg
	        MultivariateFunction wholeLeg = new ArcTanSolver(times, bearings); 
	        SimplexOptimizer wholeOptimizer = new SimplexOptimizer(1e-3, 1e-6); 
	        
	        int MAX_ITERATIONS = Integer.MAX_VALUE;
	        
			PointValuePair wholeLegOptimiser = wholeOptimizer.optimize( 
	                new MaxEval(MAX_ITERATIONS),
	                new ObjectiveFunction(wholeLeg), 
	                GoalType.MINIMIZE,
	                new InitialGuess(new double[] {bearings.get(0), 1, 1} ),//beforeBearings.get(0)
	                new MultiDirectionalSimplex(3)); 

			System.out.println(" whole leg score:" + wholeLegOptimiser.getValue().intValue());
			
			
			double bestScore = Double.MAX_VALUE;
			int bestIndex = -1;

			// make the two slices				
			final int BUFFER_REGION = 4; // the number of measurements to ignore whilst the target is turning 

			for(int index=1 + BUFFER_REGION / 2;index<thisLeg.size() - BUFFER_REGION / 2;index++)
			{
				List<Long> theseTimes = times;
				List<Double> theseBearings = bearings;				
				
				// first the times
				List<Long> beforeTimes = theseTimes.subList(0, index - BUFFER_REGION / 2);
				List<Long> afterTimes = theseTimes.subList(index + BUFFER_REGION / 2, theseTimes.size()-1);
				
				// now the bearings
				List<Double> beforeBearings = theseBearings.subList(0, index);
				List<Double> afterBearings = theseBearings.subList(index, theseBearings.size()-1);
				
				
		        MultivariateFunction beforeF = new ArcTanSolver(beforeTimes, beforeBearings); 
		        MultivariateFunction afterF = new ArcTanSolver(afterTimes, afterBearings); 
	    		    		
		        SimplexOptimizer optimizerMult = new SimplexOptimizer(1e-3, 1e-6); 
		        
				PointValuePair beforeOptimiser = optimizerMult.optimize( 
		                new MaxEval(MAX_ITERATIONS),
		                new ObjectiveFunction(beforeF), 
		                GoalType.MINIMIZE,
		                new InitialGuess(new double[] {beforeBearings.get(0), 1, 1} ),//beforeBearings.get(0)
		                new MultiDirectionalSimplex(3)); 
		        
		        PointValuePair afterOptimiser = optimizerMult.optimize( 
		                new MaxEval(MAX_ITERATIONS),
		                new ObjectiveFunction(afterF), 
		                GoalType.MINIMIZE,
		                new InitialGuess(new double[] {afterBearings.get(0), 1, 1} ),//afterBearings.get(0)
		                new MultiDirectionalSimplex(3)); 

		        double sum = beforeOptimiser.getValue() + afterOptimiser.getValue();
		        
		        if(sum < bestScore)
		        {
		        	bestScore = sum;
		        	bestIndex = index;
		        }
			}			
			
	        System.out.println(" split sum:" + (int)bestScore + " at time " + times.get(bestIndex));
	        
		}
		
		long elapsed = System.currentTimeMillis() - startTime;
		System.out.println("Elapsed:" + elapsed / 1000 + " secs");

	}

	/** slice this data into ownship legs, where the course and speed are relatively steady
	 * 
	 * @param course_degs
	 * @param speed
	 * @param bearings
	 * @param elapsedTimes
	 * @return
	 */
	private static List<LegOfData> calculateLegs(Track track) {
		
		final double COURSE_TOLERANCE = 0.1;   // degs / sec (just a guess!!)
		final double SPEED_TOLERANCE = 2;   // knots / sec  (just a guess!!)
		
		double lastCourse = 0;
		double lastSpeed = 0;
		double lastTime = 0;
		
		List<LegOfData> legs = new ArrayList<LegOfData>();
		legs.add(new LegOfData("Ownship Leg 0"));
		
		DateTime[] times = track.getDates();
		double[] speeds = track.getSpeeds();
		double[] courses = track.getCourses();
		
		for (int i = 0; i < times.length; i++) {
			long thisTime = times[i].getMillis();
			
			double thisSpeed = speeds[i];
			double thisCourse = courses[i];
			
			if(i > 0)
			{
				// ok, check out the course change rate
				double timeStep = thisTime - lastTime;
				double courseRate = Math.abs(thisCourse - lastCourse) / timeStep; 
				double speedRate = Math.abs(thisSpeed - lastSpeed) / timeStep;
				
				// are they out of range
				if((courseRate < COURSE_TOLERANCE) && (speedRate < SPEED_TOLERANCE))
				{
					// ok, we're on a new leg - drop the current one
					legs.get(legs.size()-1).add(thisTime);
				}
				else
				{
					// we may be in a turn. create a new leg, if we haven't done so already
					if(legs.get(legs.size()-1).size() != 0)
					{
						legs.add(new LegOfData("Ownship Leg " + legs.size()));
					}
				}
			}
			
			// ok, store the values
			lastTime = thisTime;
			lastCourse = thisCourse;
			lastSpeed = thisSpeed;
			
		}
		
		return legs;
	}

	/** class to store a leg of ownship data
	 * 
	 * @author ian
	 *
	 */
	private static class LegOfData
	{
		final List<Long> _times = new ArrayList<Long>();
		
		final private String _myName;
		
		public LegOfData(final String name)
		{
			_myName = name;
		}
		public Long getEnd() {
			return _times.get(_times.size()-1);
		}
		public Long getStart() {
			return _times.get(0);
		}
		public String getName() {
			return _myName;
		}
		public void add(long time)
		{
			_times.add(time);
		}
		public int size()
		{
			return _times.size();
		}
		@Override
		public String toString()
		{
			return getName() + " " + _times.get(0) + "-" + _times.get(_times.size()-1);
		}
		
	}
	
	/** function to generate sum of squares of errors for a single permutation of B,P,Q
	 * 
	 */
	private static class ArcTanSolver implements MultivariateFunction
	{
		final private List<Long> _times;
		final private List<Double> _bearings;

		public ArcTanSolver(List<Long> beforeTimes, List<Double> beforeBearings)
		{
			_times = beforeTimes;
			_bearings = beforeBearings;
		}

		@Override
		public double value(double[] point) {
        	double B = point[0];
        	double P = point[1];
        	double Q = point[2];
        	
        	double runningSum = 0;
        	
        	// ok, loop through the data
        	for (int i = 0; i < _times.size(); i++) {
        		Long elapsedSecs = _times.get(i);
				double thisForecast = calcForecast(B, P, Q, elapsedSecs / 1000d); 
				Double thisMeasured = _bearings.get(i);
				double thisError = Math.pow(thisForecast - thisMeasured, 2);
				runningSum += thisError;
			}        	
        		
            return runningSum;
		}

		private double calcForecast(double B, double P, double Q,
				Double elapsedSecs) {
			return Math.toDegrees(Math.atan2(Math.sin(Math.toRadians(B))+P*elapsedSecs,Math.cos(Math.toRadians(B))+Q*elapsedSecs));
		}		
	}
}
