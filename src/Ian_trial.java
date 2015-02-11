/**
 * Created by Bill on 1/23/2015.
 *A project to determine the Linear regression for maritime analytic using java
 * Modules such as apache commons maths libraries and Jfreechart are used for analysis and visualization
 */
import java.io.FileReader;
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
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import au.com.bytecode.opencsv.CSVReader;

public class Ian_trial {

	public static void main(String[] args) throws Exception {
		/* Reading a csv file to obtain data for analysis */

		// Filename containing the data
		String csvFilename = "data/ArcTan_Data.csv";
		// Reading the csv file but ignoring the first column since it contains
		// headings the result is stored in a list
		CSVReader csvReader = new CSVReader(new FileReader(csvFilename), ',',
				'\'', 1);
		final List<String[]> content = csvReader.readAll();
		// variable to hold each row of the List while iterating through it
		String[] row = null;
		// counter used to populate the variables with data from the csv file
		int counter = 0;
		// initializing the variables to hold data in the csv file
		double X_m[] = new double[content.size()];
		double Y_m[] = new double[content.size()];
		String DateTime[] = new String[content.size()];
		double Speed[] = new double[content.size()];
		double Course_degs[] = new double[content.size()];
		final Double bearings[] = new Double[content.size()];
		org.joda.time.DateTime date[] = new DateTime[content.size()];
		final Double elapsedTimes[] = new Double[content.size()];

		for (Object object : content) {
			row = (String[]) object;
			/* parsing data from the list to the variables */
			DateTime[counter] = row[0].toString();
			X_m[counter] = (Double.parseDouble(row[1].toString()));
			Y_m[counter] = (Double.parseDouble(row[2].toString()));
			Course_degs[counter] = (Double.parseDouble(row[3].toString()));
			Speed[counter] = (Double.parseDouble(row[4].toString()));
			bearings[counter] = (Double.parseDouble(row[5].toString()));
			counter++;
		}

		csvReader.close();
		/*
		 * Method to obtain the number of seconds elapsed in every bearing
		 * reading
		 */
		DateTimeFormatter formatter = DateTimeFormat
				.forPattern("dd/MM/yyyy HH:mm:ss");
		long firstDate = 0;
		for (int i = 0; i < content.size(); i++) {
			// take a copy of the data - it will be useful later on
			date[i] = formatter.parseDateTime(DateTime[i]);
			
			// is this the first row?
			if(i==0)
			{
				// ok, first time - store it;
				firstDate = date[i].getMillis();
			}			
			elapsedTimes[i] = (date[i].getMillis() - firstDate) / 10000d;
		}

		long startTime = System.currentTimeMillis();
		System.out.println("data loaded - computing");
		

		// Now, we have to slice the data into ownship legs
		List<LegOfData> ownshipLegs = calculateLegs(Course_degs, Speed, bearings, elapsedTimes);
		
		// ok, work through the legs.  In the absence of a Discrete Optimisation algorithm we're taking a brue force approach.
		// Hopefully Craig can find an optimised alternative to this.
		for (Iterator<LegOfData> iterator = ownshipLegs.iterator(); iterator.hasNext();) {
			
			LegOfData thisLeg = (LegOfData) iterator.next();
			
			System.out.println(" handling leg:" + thisLeg);
			
			double bestScore = Double.MAX_VALUE;
			int bestIndex = -1;

			// make the two slices				
			final int BUFFER_REGION = 4; // the number of measurements to ignore whilst the target is turning 

			for(int index=1 + BUFFER_REGION / 2;index<thisLeg.size() - BUFFER_REGION / 2;index++)
			{
				List<Double> theseTimes = thisLeg.times();
				List<Double> theseBearings = thisLeg.bearings();				
				
				// first the times
				List<Double> beforeTimes = theseTimes.subList(0, index - BUFFER_REGION / 2);
				List<Double> afterTimes = theseTimes.subList(index + BUFFER_REGION / 2, theseTimes.size()-1);
				
				// now the bearings
				List<Double> beforeBearings = theseBearings.subList(0, index);
				List<Double> afterBearings = theseBearings.subList(index, theseBearings.size()-1);
				
				
		        MultivariateFunction beforeF = new ArcTanSolver(beforeTimes, beforeBearings); 
		        MultivariateFunction afterF = new ArcTanSolver(afterTimes, afterBearings); 
	    		    		
		        SimplexOptimizer optimizerMult = new SimplexOptimizer(1e-3, 1e-6); 
		        
		        int MAX_ITERATIONS = Integer.MAX_VALUE;
		        
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
			
	        System.out.println(" slicing leg:" + thisLeg.getName() + " at index " + bestIndex);
	        
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
	private static List<LegOfData> calculateLegs(double[] course_degs,
			double[] speed, Double[] bearings, Double[] elapsedTimes) {
		
		final double COURSE_TOLERANCE = 0.1;   // degs / sec (just a guess!!)
		final double SPEED_TOLERANCE = 2;   // knots / sec  (just a guess!!)
		
		double lastCourse = 0;
		double lastSpeed = 0;
		double lastTime = 0;
		
		List<LegOfData> legs = new ArrayList<LegOfData>();
		legs.add(new LegOfData("Ownship Leg 0"));
		
		for (int i = 0; i < elapsedTimes.length; i++) {
			Double thisTime = elapsedTimes[i];
			
			double thisSpeed = speed[i];
			double thisCourse = course_degs[i];
			
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
					legs.get(legs.size()-1).add(thisTime, bearings[i]);
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
		final List<Double> _times = new ArrayList<Double>();
		final List<Double> _bearings = new ArrayList<Double>();
		
		final private String _myName;
		
		public LegOfData(final String name)
		{
			_myName = name;
		}
		public String getName() {
			return _myName;
		}
		public List<Double> bearings() {
			return _bearings;
		}
		public List<Double> times() {
			return _times;
		}
		public void add(double time, double bearing)
		{
			_times.add(time);
			_bearings.add(bearing);		
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
		final private List<Double> _times;
		final private List<Double> _bearings;

		public ArcTanSolver(List<Double> beforeTimes, List<Double> beforeBearings)
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
        		Double elapsedSecs = _times.get(i);
				double thisForecast = calcForecast(B, P, Q, elapsedSecs); 
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
