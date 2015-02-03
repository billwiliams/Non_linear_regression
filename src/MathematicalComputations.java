
import org.apache.commons.math3.linear.*;

import org.apache.commons.math3.util.FastMath;


/**
 * Created by Bill on 1/23/2015.
 * Handles computations of values
 */
public class MathematicalComputations {

    public static void main(String[] args) {

    }



    public static double LeastSquares(double[] Bi, double[] Bo) {
        double[] LeastSquares = new double[Bi.length];
        RealMatrix SquaredErrors;
        for (int i = 0; i < Bi.length; i++) {
            LeastSquares[i] = FastMath.pow((Bi[i] - Bo[i]), 2);
        }
        SquaredErrors = (MatrixUtils.createColumnRealMatrix(LeastSquares));
        //returns the double sum of the values in the matrix. This is the sum of the squared errors
        return SquaredErrors.getNorm();
    }




}
