

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
           return Math.toDegrees(Math.atan2(Math.sin(Math.toRadians(B))+P*v,Math.cos(Math.toRadians(B))+Q*v));
       }

      /**
       * This Method is used to compute the gradient of the jacobian matrix above, it does so by returning the partial derivatives of the
       * function above. 1.e partial derivative of the arctan formula  with respect to B,P and Q
       *
       */
       @Override
       public double[] gradient(double v, double... doubles) {
            double B= doubles[0];
           double P= doubles[1];
            double Q=doubles[2];

            return new double[]{
                    (( Math.cos(Math.toRadians(B))*(Math.cos(Math.toRadians(B))+Q*v))-(Math.sin(Math.toRadians(B))*((-Math.sin(Math.toRadians(B)))-P*v)))/(Math.pow((Math.sin(Math.toRadians(B))+P*v),2)+Math.pow((Math.cos(Math.toRadians(B))+Q*v),2)),
                    (( v*(Math.cos(Math.toRadians(B))+Q*v))/(Math.pow((Math.sin(Math.toRadians(B))+P*v),2)+Math.pow((Math.cos(Math.toRadians(B))+Q*v),2))),
                    (( v*(-Math.sin(Math.toRadians(B))-P*v))/(Math.pow((Math.sin(Math.toRadians(B))+P*v),2)+Math.pow((Math.cos(Math.toRadians(B))+Q*v),2)))
           };
       }
   };





}
