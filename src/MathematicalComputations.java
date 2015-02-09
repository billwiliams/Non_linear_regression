

import org.apache.commons.math3.analysis.ParametricUnivariateFunction;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.function.Gaussian;
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
      /*
      * This is our Arctan formula which we will use to fit a curve to our measured data
      * It returns the value of Bi (bearing ) in degrees
      * B,P and Q are our fitting parameters which are unknown and are to be computed using the Levernberg Marquardt optimizer since it's
      * a least squares problem
      * */
       @Override
       public double value(double v, double... doubles) {
           double B=Math.toRadians( doubles[0]);
           double P=doubles[1];
           double Q= doubles[2];
           return Math.toDegrees(Math.atan2(Math.sin((B))+P*v,Math.cos((B))+Q*v));
       }

      /**
       * This Method is used to compute the gradient of the jacobian matrix above, it does so by returning the partial derivatives of the
       * function above. 1.e partial derivative of the arctan formula  with respect to B,P and Q
       * this method returns the fitting parameters for the above method which contains the formula for fitting our curve
       * It takes the unknown variables B,P and Q and uses the partial derivatives of the arctan formula to compute the gradient hence the fitting parameters
       */
       @Override
       public double[] gradient(double v, double... doubles) {
            double B= Math.toRadians(doubles[0]);
           double P= doubles[1];
            double Q=doubles[2];

            return new double[]{
                    //partial derivative of arctan formula with respect to B i.e d/dB
                    ( -(-P*v-Math.sin(B) )*Math.sin(B)/(Math.pow((Math.sin((B))+P*v),2)+Math.pow((Math.cos((B))+Q*v),2)))+((Q*v + Math.cos(B))*Math.cos(B)/(Math.pow((Math.sin((B))+P*v),2)+Math.pow((Math.cos((B))+Q*v),2))),
                    //partial derivative of arctan formula with respect to P i.e d/dP
                    (( v*(Math.cos((B))+Q*v))/(Math.pow((Math.sin((B))+P*v),2)+Math.pow((Math.cos((B))+Q*v),2))),
                    //partial derivative of arctan formula with respect to Q i.e d/dQ
                    (( v*(-P*v-Math.sin((B))))/(Math.pow((Math.sin((B))+P*v),2)+Math.pow((Math.cos((B))+Q*v),2)))
           };
       }
   };





}
