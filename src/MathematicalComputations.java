

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
       public double value(double elapsedSecs, double... doubles) {
          //fitting parameter B is converted into radians since it's in Degrees
           double B= Math.toRadians(doubles[0]);
           double P=doubles[1];
           double Q= doubles[2];
           return Math.toDegrees(Math.atan2(Math.sin((B))+P*elapsedSecs,Math.cos((B))+Q*elapsedSecs));
       }

      /**
       * This Method is used to compute the gradient of the jacobian matrix above, it does so by returning the partial derivatives of the
       * function above. 1.e partial derivative of the arctan formula  with respect to B,P and Q
       * it does so by computing the gradient of the equation at each point with respect to B,P and Q
       * it returns the fitting parameters
       *
       */
       @Override
       public double[] gradient(double v, double... doubles) {
           //fitting parameter B is converted into radians since it's in Degrees
            double B= Math.toRadians(doubles[0]);
           double P= doubles[1];
            double Q=doubles[2];

            return new double[]{
                    //The partial derivative of the arctan equation with respect to B i.e d/dB
                    (( Math.cos((B))*(Math.cos((B))+Q*v))-(Math.sin((B))*((-Math.sin((B)))-P*v)))/(Math.pow((Math.sin((B))+P*v),2)+Math.pow((Math.cos((B))+Q*v),2)),
                    //The partial derivative of the arctan equation with respect to P i.e d/dP
                    (( v*(Math.cos((B))+Q*v))/(Math.pow((Math.sin((B))+P*v),2)+Math.pow((Math.cos((B))+Q*v),2))),
                    //The partial derivative of the arctan equation with respect to Q i.e d/dQ
                    (( v*(-Math.sin((B))-P*v))/(Math.pow((Math.sin((B))+P*v),2)+Math.pow((Math.cos((B))+Q*v),2)))
           };
       }
   };





}
