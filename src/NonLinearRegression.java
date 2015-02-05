/**
 * Created by Bill on 1/23/2015.
 *A project to determine the Linear regression for maritime analytic using java
 * Modules such as apache commons maths libraries and Jfreechart are used for analysis and visualization
 */
import au.com.bytecode.opencsv.CSVReader;



import org.apache.commons.math3.optimization.fitting.CurveFitter;
import org.apache.commons.math3.optimization.fitting.PolynomialFitter;
import org.apache.commons.math3.optimization.general.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.util.FastMath;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


import java.io.FileReader;
import java.util.List;


public class NonLinearRegression {


    @SuppressWarnings("rawtypes")
    public static void main(String[] args) throws Exception {
        /*Reading a csv file to obtain data for analysis*/


        //Filename containing the data
        String csvFilename = "D:\\projects\\Non_linear_regression\\ArcTan\\ArcTan_Data.csv";
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
            counter++;
        }

        csvReader.close();
        /*Method to obtain the number of seconds elapsed in every bearing reading*/
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        for (int i = 0; i < content.size(); i++) {

            date[i] = formatter.parseDateTime(DateTime[i]);
            Seconds diff = Seconds.secondsBetween(date[0], date[i]);
            TimeElapsed[i] = Double.valueOf(diff.getSeconds())/10000;
        }


        LevenbergMarquardtOptimizer optimizer=new LevenbergMarquardtOptimizer();
        CurveFitter fitter= new CurveFitter(optimizer);
        for (int i = 0; i < TimeElapsed.length; i++) {
            fitter.addObservedPoint(TimeElapsed[i],Bearing_degs[i]);
        }
        //double[] result = fitter.fit(MathematicalComputations.function, new double[] { 0.9, 0.8,0.4 });
double[] initial={0,0,0};
double[] result=fitter.fit(MathematicalComputations.function,initial);
System.out.println(result[2]);

    }
}

