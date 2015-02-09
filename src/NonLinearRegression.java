/**
 * Created by Bill on 1/23/2015.
 *A project to determine the Linear regression for maritime analytic using java
 * Modules such as apache commons maths libraries and Jfreechart are used for analysis and visualization
 */
import au.com.bytecode.opencsv.CSVReader;



import org.apache.commons.math3.fitting.AbstractCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.linear.DiagonalMatrix;

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
        org.joda.time.DateTime date[] = new DateTime[content.size()];
        final double TimeElapsed[] = new double[content.size()];
        double SquaredErrors;

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
            Bearing_degs[counter] = Bearing_degs[counter] / 10;
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

/*This instance obtains the optimum values of B,P and Q and uses them to fit the arctan curve into the equation
* The  Levenberg Marquardt Optimizer is used to fit the parameters to the observed data since this is a Least squares problem
* AbstractCurveFitter is used to fit the parameter observed according to our function i.e the Arctan curve
* The Results of this solution were confirmed with the ones obtained from the python implementation which doesn't use derivatives
*
 */
        AbstractCurveFitter fitter = new AbstractCurveFitter() {
            @Override
            protected LeastSquaresProblem getProblem(Collection<WeightedObservedPoint> points) {
                final int len = points.size();
                final double[] target = new double[len];
                final double[] weights = new double[len];
                final double[] initialGuess = {1.0, 1.0, 1.0};

                int i = 0;
                for (WeightedObservedPoint point : points) {
                    target[i] = point.getY();
                    weights[i] = point.getWeight();
                    i += 1;
                }
                TheoreticalValuesFunction model = new TheoreticalValuesFunction(MathematicalComputations.function, points);
                /* building a least squares evaluation by using the maximum possible number of evaluations i.e 2147483647 to ensure we obtain a correct guess*/
                return new LeastSquaresBuilder().
                        maxEvaluations(Integer.MAX_VALUE).
                        maxIterations(Integer.MAX_VALUE).
                        start(initialGuess).
                        target(target).
                        weight(new DiagonalMatrix(weights)).
                        model(model.getModelFunction(), model.getModelFunctionJacobian()).
                        build();
            }
            /* Specifying the optimizer to compute our solution
            * In our case we use the levenberg Marquardt Optimizer for a least squares optimization
            * */
            @Override
            protected LeastSquaresOptimizer getOptimizer() {

                return new org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer();
            }
        };
        //declaring an Arraylist to hold our observed points
        ArrayList<WeightedObservedPoint> points = new ArrayList<WeightedObservedPoint>();

        // Adding the  points here,

        for (int i = 0; i < TimeElapsed.length; i++) {
            WeightedObservedPoint point = new WeightedObservedPoint(1.0, TimeElapsed[i], Bearing_degs[i]);
            points.add(point);
        }

        // the optimum values returned after fitting our curve i.e result contains optimum values for B,P and Q
        double[] result = fitter.fit(points);
        // An array to hold our fitting values to be computed by our function
        double[] Y_fit = new double[TimeElapsed.length];
        //computing our bearing Bi using the optimum values for B,P and Q
        for (int i = 0; i < TimeElapsed.length; i++) {
            Y_fit[i] = MathematicalComputations.function.value(TimeElapsed[i], result);
        }


        //Since we had divided by 10 we multiply by 10 to obtain the actual values  of bearing Bi in degrees
        for (int i = 0; i < Y_fit.length; i++) {
            Y_fit[i]=Y_fit[i]*10;
        }
        ScatterPlot.ScatterCreate(TimeElapsed, Y_fit);
        System.out.println(Integer.MAX_VALUE);

    }

}

