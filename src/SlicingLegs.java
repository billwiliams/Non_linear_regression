import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bill on 2/9/2015.
 * A class that slices the ownship data into straight legs only
 * to ensure no single points are  used the counter is incremented by 2.
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
   public static int[] calculatelegs(double[] course_degs,double[] speed){
      ArrayList legs=new ArrayList();

       final double COURSE_TOLERANCE = 0.1;   // degs / sec (just a guess!!)
       final double SPEED_TOLERANCE = 2.0;   // knots / sec  (just a guess!!)

       double lastCourse;
       double lastSpeed ;

       for (int i=1;i<speed.length;i++) {
           lastCourse = course_degs[i] - course_degs[i - 1];
           lastSpeed = speed[i] - speed[i - 1];
           if ((lastCourse>COURSE_TOLERANCE )||(lastSpeed>SPEED_TOLERANCE)){
               legs.add(i);
               i+=2;

           }

       }
       //an array to hold the specific points to slice our straight legs
       int[] legs_cuts=new int[legs.size()];
       for(int i=0;i<legs.size();i++){
           //storing the straight leg slice points in the array
           legs_cuts[i]=Integer.parseInt(legs.get(i).toString());
       }
      return legs_cuts;
   }

}
