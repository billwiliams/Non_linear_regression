import org.apache.commons.math3.util.FastMath;

import java.lang.reflect.Array;

/**
 * Created by Bill on 1/23/2015.
 * Handles computations of values
 */
public class MathematicalComputations {

    public static void main(String[] args) {

    }

    /*This Method is used to returns the ownship course Xo' or target course XT' */
    public static double[] ConvertCourseX_m(double[] Course_degs, double[] Speed) {
        //variable for the ownship course Y cordinates
        double[] Xo = null;
        for (int counter = 0; counter < Array.getLength(Speed); counter++) {

            Xo[counter] = Speed[counter] * FastMath.sin(Course_degs[counter]);
        }
        return Xo;
    }

    /* This method is used to returns the ownship course Yo' or target course YT' */
    public static double[] ConvertCourseY_m(double[] Course_degs, double[] Speed) {
        //variable for the ownship course Y cordinates
        double[] Yo = null;
        for (int counter = 0; counter < Array.getLength(Speed); counter++) {

            Yo[counter] = Speed[counter] * FastMath.cos(Course_degs[counter]);

        }
        return Yo;
    }
    /*This method returns the bearing Bi*/
    public static double[] Bearing(double[] Xo,double[] Yo,double[] Xt,double[] Yt,double[] Bearing,double[] ti){
        /*
        *Explanation of the variables in the Method
        * Xo is the Ownship X copmponent Xo'
        * Yo is the Ownship Y component Y0'
        * Xt is the target Ship X component XT'
        * Yt is the target Ship X component XT'
        * Bearing is the bearing Bo
        * ti is the time ti used in the equation
        *
        * counter is the variable used to iterate while performing computation of Bi
        * Bi is the bearing  being calculated
        * P=XT'-Xo'
        * Q=YT'-Yo'
        */
        int Counter=0;
        double[] Bi,P,Q;
        Bi=null;
        P=null;
        Q=null;


        for(;Counter<Xo.length;Counter++){
            //obtaining the values of P and Q for every value of X and Y
            P[Counter]=Xt[Counter]-Xo[Counter];
            Q[Counter]=Yt[Counter]-Yo[Counter];
            //Formula for Calculating Bi
            Bi[Counter]=FastMath.atan(FastMath.sin(Bearing[Counter])+P[Counter]*ti[Counter]/(FastMath.cos(Bearing[Counter])+Q[Counter]*ti[Counter]));

        }

        return Bi;
    }
}
