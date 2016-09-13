 


/**
 * ToolPath stores motor contol signals (pwm)
 * and motor angles
 * for given drawing and arm configuration.
 * Arm hardware takes sequence of pwm values 
 * to drive the motors
 * @Arthur Roberts 
 * @1000000.0
 */
import ecs100.UI;
import java.util.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;


public class ToolPath
{
     int n_steps; //straight line segmentt will be broken
                      // into that many sections
                      
     // storage for angles and 
     // moto control signals
     ArrayList<Double> theta1_vector;
     ArrayList<Double> theta2_vector;
     ArrayList<Integer> pen_vector;
     ArrayList<Integer> pwm1_vector;
     ArrayList<Integer> pwm2_vector;
     ArrayList<Integer> pwm3_vector;

    /**
     * Constructor for objects of class ToolPath
     */
    public ToolPath()
    {
        // initialise instance variables
      n_steps = 50;
      theta1_vector = new ArrayList<Double>();
      theta2_vector = new ArrayList<Double>();
      pen_vector = new ArrayList<Integer>();
      pwm1_vector = new ArrayList<Integer>();
      pwm2_vector = new ArrayList<Integer>();
      pwm3_vector = new ArrayList<Integer>();

    }

    /**********CONVERT (X,Y) PATH into angles******************/
    public void convert_drawing_to_angles(Drawing drawing,Arm arm,String fname){

        // for all points of the drawing...        
        for (int i = 0;i < drawing.get_drawing_size()-1;i++){ 
            // take two points
            PointXY p0 = drawing.get_drawing_point(i);
            PointXY p1 = drawing.get_drawing_point(i+1);
            // break line between points into segments: n_steps of them
            for ( int j = 0 ; j< n_steps;j++) { // break segment into n_steps str. lines
                double x = p0.get_x() + j*(p1.get_x()-p0.get_x())/n_steps;
                double y = p0.get_y() + j*(p1.get_y()-p0.get_y())/n_steps;
                arm.inverseKinematic(x, y);
                theta1_vector.add(arm.get_theta1()*180/Math.PI);
                theta2_vector.add(arm.get_theta2()*180/Math.PI);
                if (p0.get_pen()){ 
                  pen_vector.add(1);
                } else {
                  pen_vector.add(0);
                }
            }
        }
    }
    
    public void save_angles(String fname){
        for ( int i = 0 ; i < theta1_vector.size(); i++){
         UI.printf(" t1=%3.1f t2=%3.1f pen=%d\n",
            theta1_vector.get(i),theta2_vector.get(i),pen_vector.get(i));
        }
        
         try {
            //Whatever the file path is.
            File statText = new File(fname);
            FileOutputStream is = new FileOutputStream(statText);
            OutputStreamWriter osw = new OutputStreamWriter(is);    
            Writer w = new BufferedWriter(osw);
            String str_out;
            for (int i = 1; i < theta1_vector.size() ; i++){
                str_out = String.format("%3.1f,%3.1f,%d\n",
                  theta1_vector.get(i),theta2_vector.get(i),pen_vector.get(i));
                w.write(str_out);
            }
            w.close();
        } catch (IOException e) {
            UI.println("Problem writing to the file statsTest.txt");
        }
        
    }
    
    // takes sequence of angles and converts it 
    // into sequence of motor signals
    public void convert_angles_to_pwm(Arm arm){
        // for each angle
        for (int i=0 ; i < theta1_vector.size();i++){
            arm.set_angles(theta1_vector.get(i),theta2_vector.get(i));
            pwm1_vector.add(arm.get_pwm1());
            pwm2_vector.add(arm.get_pwm2());
        }
    }
    
    // save file with motor control values
    public void save_pwm_file(){
        ...
    }

}
