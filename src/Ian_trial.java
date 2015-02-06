/**
 * Created by Bill on 1/23/2015.
 *A project to determine the Linear regression for maritime analytic using java
 * Modules such as apache commons maths libraries and Jfreechart are used for analysis and visualization
 */
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.OptimizationData;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.MultiDirectionalSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.apache.commons.math3.optim.univariate.BrentOptimizer;
import org.apache.commons.math3.optim.univariate.SearchInterval;
import org.apache.commons.math3.optim.univariate.UnivariateObjectiveFunction;
import org.apache.commons.math3.optim.univariate.UnivariateOptimizer;
import org.apache.commons.math3.optim.univariate.UnivariatePointValuePair;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import au.com.bytecode.opencsv.CSVReader;

@SuppressWarnings("deprecation")
public class Ian_trial {

	@SuppressWarnings("rawtypes")
	public static void main(String[] args) throws Exception {
		/* Reading a csv file to obtain data for analysis */

		// Filename containing the data
		String csvFilename = "data/ArcTan_Data.csv";
		// Reading the csv file but ignoring the first column since it contains
		// headings the result is stored in a list
		CSVReader csvReader = new CSVReader(new FileReader(csvFilename), ',',
				'\'', 1);
		final List content = csvReader.readAll();
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
		double SquaredErrors;

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
		for (int i = 0; i < content.size(); i++) {

			date[i] = formatter.parseDateTime(DateTime[i]);
			Seconds diff = Seconds.secondsBetween(date[0], date[i]);
			elapsedTimes[i] = Double.valueOf(diff.getSeconds()) / 10000;
		}

		// ok, declare the function that will experiment with different leg slices
		UnivariateFunction g = new LegSplitter(elapsedTimes , bearings); 
        		
        UnivariateOptimizer optimizerMult = new BrentOptimizer(1e-3, 1e-6); 
        UnivariatePointValuePair solutionMult = optimizerMult.optimize(         		
        		GoalType.MINIMIZE,
        		new UnivariateObjectiveFunction(g),
        		new SearchInterval(0, elapsedTimes.length)); 

        System.out.println(solutionMult.getValue()); 
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
	
	/** function to generate sum of squares of errors for a permutation of slice time
	 * 
	 * @author ian
	 *
	 */
	private static class LegSplitter implements UnivariateFunction
	{
		final private Double[] _times;
		final private Double[] _bearings;


		public LegSplitter(Double[] times, Double[] bearings)
		{
			_times = times;
			_bearings = bearings;
		}
		

		@Override
		public double value(double x) {
			
			// ok, treat x as the integer to experiments with
			int index = (int)x;
			
			// check it's valid
			if((index <= 0) || (index >= _times.length))
			{
				// ok, invalid - move along
				return Double.MAX_VALUE;
			}
			
			// use this to make the two slices
			// first the times
			List<Double> safeTimes = Arrays.asList(_times);
			List<Double> beforeTimes = safeTimes.subList(0, index);
			List<Double> afterTimes = safeTimes.subList(index, _times.length-1);
			
			// now the bearings
			List<Double> safeBearings = Arrays.asList(_bearings);
			List<Double> beforeBearings = safeBearings.subList(0, index);
			List<Double> afterBearings = safeBearings.subList(index, _bearings.length-1);
			
			
	        MultivariateFunction beforeF = new ArcTanSolver(beforeTimes, beforeBearings); 
	        MultivariateFunction afterF = new ArcTanSolver(afterTimes, afterBearings); 
    		    		
	        SimplexOptimizer optimizerMult = new SimplexOptimizer(1e-3, 1e-6); 
	        
	        PointValuePair beforeOptimiser = optimizerMult.optimize( 
	                new MaxEval(200), 
	                new ObjectiveFunction(beforeF), 
	                GoalType.MAXIMIZE, 
	                new InitialGuess(new double[] {beforeBearings.get(0), 0, 0} ), 
	                new MultiDirectionalSimplex(3)); 
	        
	        PointValuePair afterOptimiser = optimizerMult.optimize( 
	                new MaxEval(200), 
	                new ObjectiveFunction(afterF), 
	                GoalType.MAXIMIZE, 
	                new InitialGuess(new double[] {afterBearings.get(0), 0, 0} ), 
	                new MultiDirectionalSimplex(3)); 

	        double sum = beforeOptimiser.getValue() + afterOptimiser.getValue();

	        return sum;
		}
		
	}
	
	
}
