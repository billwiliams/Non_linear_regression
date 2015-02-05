

import org.apache.commons.math3.analysis.ParametricUnivariateFunction;

import org.apache.commons.math3.linear.*;

import org.apache.commons.math3.optimization.fitting.CurveFitter;
import org.apache.commons.math3.optimization.general.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.util.FastMath;

import java.util.Collection;


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
  public static ParametricUnivariateFunction function=new ParametricUnivariateFunction() {
       @Override
       public double value(double v, double... doubles) {
           double B= doubles[0];
           double P=doubles[1];
           double Q= doubles[2];
           return  FastMath.toDegrees(FastMath.atan2(FastMath.sin(FastMath.toRadians(B))+P*v,FastMath.sin(FastMath.toRadians(B))+Q*v));
       }

       @Override
       public double[] gradient(double v, double... doubles) {
           double B= doubles[0];
double P=doubles[1];
           double Q= doubles[2];
           return new double[]{
                   ( (FastMath.sin(B)*(P*v +FastMath.sin(B)))/(Q*v +FastMath.pow(FastMath.cos(B),2))+FastMath.cos(B)/(Q*v + FastMath.cos(B)))/((P*v +FastMath.pow(FastMath.sin(B),2))/(Q*v +FastMath.pow(FastMath.cos(B),2))+1),
                   v/((Q*v+FastMath.cos(B))*((P*v +FastMath.pow(FastMath.sin(B),2))/(Q*v +FastMath.pow(FastMath.cos(B),2))+1)),
                   (v*(P*v +FastMath.sin(B)))/((Q*v+FastMath.pow(FastMath.cos(B),2))*((P*v +FastMath.pow(FastMath.sin(B),2))/(Q*v +FastMath.pow(FastMath.cos(B),2))+1))
           };
       }
   };





}
