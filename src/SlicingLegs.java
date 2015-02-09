import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bill on 2/9/2015.
 * A class that slices the ownship data into straight legs only
 */
public class SlicingLegs {
    public void main (String[] args){

    }
    /** slice this data into ownship legs, where the course and speed are relatively steady
     *
     * @param course_degs
     * @param speed
     *
     * @return
     */
   public static double[] calculatelegs(double[] course_degs,double[] speed){
      ArrayList legs=new ArrayList();
       int index_counter=0;
       final double COURSE_TOLERANCE = 0.1;   // degs / sec (just a guess!!)
       final double SPEED_TOLERANCE = 2.0;   // knots / sec  (just a guess!!)

       double lastCourse = 0;
       double lastSpeed = 0;
       double lastTime = 0;
       for (int i=1;i<speed.length;i++) {
           lastCourse = course_degs[i] - course_degs[i - 1];
           lastSpeed = speed[i] - speed[i - 1];
           if ((lastCourse>COURSE_TOLERANCE )||(lastSpeed>SPEED_TOLERANCE)){
               legs.add(i);
               i+=2;

           }

       }
       double[] legs_cuts=new double[legs.size()];
       for(int i=0;i<legs.size();i++){
           legs_cuts[i]=Double.parseDouble(legs.get(i).toString());
       }
      return legs_cuts;
   }

}
