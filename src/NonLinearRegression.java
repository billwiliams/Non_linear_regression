/**
 * Created by Bill on 1/23/2015.
 *A project to determine the Linear regression for maritime analytic using java
 * Modules such as apache commons maths libraries and Jfreechart are used for analysis and visualization
 */
import au.com.bytecode.opencsv.CSVReader;


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
        List content = csvReader.readAll();
        //variable to hold each row of the List while iterating through it
        String[] row = null;
        //counter used to populate the variables with data from the csv file
        int counter = 0;
        //initializing the variables to hold data in the csv file
        double X_m[] = new double[content.size()];
        double Y_m[] = new double[content.size()];
        String Date[] = new String[content.size()];
        double Speed[] = new double[content.size()];
        double Course_degs[] = new double[content.size()];
        double Bearing_degs[] = new double[content.size()];
        double Xo[] ;
        double Yo[];
        double SquaredErrors;

        for (Object object : content) {
            row = (String[]) object;
            /*parsing data from the list to the variables */
            Date[counter] = row[0].toString();
            X_m[counter] = (Double.parseDouble(row[1].toString()));
            Y_m[counter] = (Double.parseDouble(row[2].toString()));
            Course_degs[counter] = (Double.parseDouble(row[3].toString()));
            Speed[counter] = (Double.parseDouble(row[4].toString()));
            Bearing_degs[counter] = (Double.parseDouble(row[5].toString()));
            counter++;
        }

        csvReader.close();
        //obtaining the values of Yo' and Xo'
        Xo = MathematicalComputations.ConvertCourseX_m(Course_degs, Speed);
        Yo = MathematicalComputations.ConvertCourseY_m(Course_degs, Speed);

        ScatterPlot.ScatterCreate(Xo,Yo);
        System.out.println(MathematicalComputations.EstimateGausianMean(Bearing_degs));
        System.out.println(MathematicalComputations.EstimateGausianVariance(Bearing_degs));



    }
    }

