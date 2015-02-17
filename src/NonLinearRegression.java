/**
 * Created by Bill on 1/23/2015.
 *A project to determine the Linear regression for maritime analytic using java
 * Modules such as apache commons maths libraries and Jfreechart are used for analysis and visualization
 */
import au.com.bytecode.opencsv.CSVReader;


import flanagan.math.Minimisation;
import flanagan.math.MinimisationFunction;
import flanagan.math.MinimizationFunction;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.fitting.AbstractCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.linear.DiagonalMatrix;

import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.MultiDirectionalSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


public class NonLinearRegression {


    @SuppressWarnings("rawtypes")
    public static void main(String[] args) throws Exception {
        /*Reading a csv file to obtain data for analysis*/


        //Filename containing the data
        String csvFilename = "data/ArcTan_Data.csv";
        //Reading the csv file but ignoring the first column since it contains headings the result is stored in a list
        CSVReader csvReader = new CSVReader(new FileReader(csvFilename), ',', '\'', 1);
        final List content = csvReader.readAll();
        //variable to hold each row of the List while iterating through it
        String[] row = null;
        //counter used to populate the variables with data from the csv file
        int counter = 0;
        //initializing the variables to hold data in the csv file
        double X_m[] = new double[content.size()];
        double Y_m[] = new double[content.size()];
        String DateTime[] = new String[content.size()];
        double Speed[] = new double[content.size()];
        double Course_degs[] = new double[content.size()];
        final double Bearing_degs[] = new double[content.size()];
        final double Bearing_degs_dif[] = new double[content.size()];
        org.joda.time.DateTime date[] = new DateTime[content.size()];
        final double TimeElapsed[] = new double[content.size()];
        double SquaredErrors;
        String[] graplabels;

        for (Object object : content) {
            row = (String[]) object;
            /*parsing data from the list to the variables */
            DateTime[counter] = row[0].toString();
            X_m[counter] = (Double.parseDouble(row[1].toString()));
            Y_m[counter] = (Double.parseDouble(row[2].toString()));
            Course_degs[counter] = (Double.parseDouble(row[3].toString()));
            Speed[counter] = (Double.parseDouble(row[4].toString()));
            Bearing_degs[counter] = (Double.parseDouble(row[5].toString()));
            // We divide Bearing Degrees by 10 since the optimization algorithm doesnt work well with large numbers
            Bearing_degs_dif[counter] = Bearing_degs[counter] / 10;
            counter++;
        }

        csvReader.close();
        /*Method to obtain the number of seconds elapsed in every bearing reading*/
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        for (int i = 0; i < content.size(); i++) {

            date[i] = formatter.parseDateTime(DateTime[i]);
            Seconds diff = Seconds.secondsBetween(date[0], date[i]);
            //we divide the Time elapsed by 10000 to ensure it works well with our optimization algorithm
            TimeElapsed[i] = Double.valueOf(diff.getSeconds()) / 10000;
        }
        /*computing the number of ownship straight legs*/
        int[] legs_cuts = SlicingLegs.calculatelegs(Course_degs, Speed);
        graplabels = new String[5];
        graplabels[0] = "A graph of Fitted parameters against Bearing";
        graplabels[1] = "Time";
        graplabels[2] = "Bearing";
        graplabels[3] = "Bi(Measured)";
        graplabels[4] = "Bi(Computed)";
        ArrayList<WeightedObservedPoint> points = new ArrayList<WeightedObservedPoint>();
        for (int i = 2; i < 30; i++) {
            WeightedObservedPoint point = new WeightedObservedPoint(1.0, TimeElapsed[i], Bearing_degs[i]);
            points.add(point);
        }
        double[] results = MathematicalComputations.fitter.fit(points);
        //using the LMA optimizer
        System.out.println("Optimized Parameters for data from index 2 to 30");
        System.out.println("Optimized params B,P,Q for the LMA optimizer are \n"+Arrays.toString(results));
        ArrayList<Double> beforeTimes = new ArrayList<Double>();
        ArrayList<Double> beforeBearings = new ArrayList<Double>();
        for (int i = 2; i < 30; i++) {
            beforeTimes.add(TimeElapsed[i]);
            beforeBearings.add(Bearing_degs[i]);
        }
        MultivariateFunction beforeF = new ArcTanSolver(beforeTimes, beforeBearings);
        SimplexOptimizer optimizerMult = new SimplexOptimizer(1e-6, 1e-6);


        PointValuePair beforeOptimiser = optimizerMult.optimize(
                new MaxEval(Integer.MAX_VALUE),
                new ObjectiveFunction(beforeF),
                GoalType.MINIMIZE,
                new InitialGuess(new double[]{1, 1, 1}),
                new MultiDirectionalSimplex(3));
//Optimized params using the simplex optimizer
        double[] keys = beforeOptimiser.getKey();

        System.out.println("optimized Params B,P,Q for the simplex optimizer are\n " + Arrays.toString(keys));
        double[] Y_fitop = new double[28];
        double[] Y_fit = new double[28];
        for (int i = 0; i < 28; i++) {
            Y_fit[i] = MathematicalComputations.function.value(TimeElapsed[i + 2], results);
            Y_fitop[i] = MathematicalComputations.function.value(TimeElapsed[i + 2], keys);
        }
//Optimized params using the flanagan.jar nelder-mead optimizer
        System.out.println("Optimized params B,P and Q for the flanagan. jar optimizer are \n"+Arrays.toString(optimiseThis(beforeTimes, beforeBearings, 153.0).getParamValues()));
    }

    private static class ArcTanSolver implements MultivariateFunction {
        final private List<Double> _times;
        final private List<Double> _bearings;

        public ArcTanSolver(List<Double> beforeTimes, List<Double> beforeBearings) {
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
            return Math.toDegrees(Math.atan2(Math.sin(Math.toRadians(B)) + P * elapsedSecs, Math.cos(Math.toRadians(B)) + Q * elapsedSecs));
        }

    }

    static Minimisation optimiseThis(List<Double> times, List<Double> bearings,
                                     double initialBearing) {
        // Create instance of Minimisation
        Minimisation min = new Minimisation();

        // Create instace of class holding function to be minimised
        FlanaganArctan funct = new FlanaganArctan(times, bearings);

        // initial estimates
        Double firstBearing = bearings.get(0);
        double[] start = {1.0, 1.0D, 1.0D};

        // initial step sizes
        double[] step =
                {0.03D, 0.03D, 0.03D};

        // convergence tolerance
        double ftol = 1e-6;

        // Nelder and Mead minimisation procedure
        //nelderMead((MinimizationFunction) funct, start, step, ftol);
        min.nelderMead(funct, start, step, ftol);

        return min;
    }

    private static class FlanaganArctan implements MinimisationFunction {
        final private List<Double> _times;
        final private List<Double> _bearings;

        public FlanaganArctan(List<Double> beforeTimes, List<Double> beforeBearings) {
            _times = beforeTimes;
            _bearings = beforeBearings;
        }

        // evaluation function
        public double function(double[] point) {
            double B = point[0];
            double P = point[1];
            double Q = point[2];

            double runningSum = 0;

            // ok, loop through the data
            for (int i = 0; i < _times.size(); i++) {
                double elapsedMillis = _times.get(i) ;
                double elapsedSecs = elapsedMillis ;
                double thisForecast = MathematicalComputations.function.value(elapsedSecs, B, P, Q);
                double thisMeasured = _bearings.get(i);
                double thisError = Math.pow(thisForecast - thisMeasured, 2);
                runningSum += thisError;
            }
            return runningSum / _times.size();
        }
    }
}


