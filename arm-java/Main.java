 
/* Code for Assignment ?? 
 * Name:
 * Usercode:
 * ID:
 */

import ecs100.*;
import java.util.*;
import java.io.*;
import java.awt.*;

/** <description of class Main>
 */
public class Main{

    private Arm arm;
    private Drawing drawing;
    private ToolPath tool_path;
    // state of the GUI
    private int state; // 0 - nothing
    // 1 - inverse point kinematics - point
    // 2 - enter path. Each click adds point  
    // 3 - enter path pause. Click does not add the point to the path

    public ArrayList <String> toSendText = new ArrayList<String>();

    /**      */
    public Main(){
        UI.initialise();
        UI.setWindowSize(1200, 800);
        UI.addButton("xy to angles", this::inverse);
        UI.addButton("Enter path XY", this::enter_path_xy);
        UI.addButton("Save path XY", this::save_xy);
        UI.addButton("Load path XY", this::load_xy);
        UI.addButton("Save path Ang", this::save_ang);
        UI.addButton("Load path Ang:Play", this::load_ang);
        UI.addButton("SEND file", this::doSendFile);

        // UI.addButton("Quit", UI::quit);
        UI.setMouseMotionListener(this::doMouse);
        UI.setKeyListener(this::doKeys);

        //ServerSocket serverSocket = new ServerSocket(22); 
        this.arm = new Arm();
        this.drawing = new Drawing();
        this.run();
        arm.draw();
    }

    public void doSendFile(){
        try{
            File f = new File (UIFileChooser.save());
            PrintStream p = new PrintStream(f);
            for (String s : toSendText){
                p.println(s);
            }

            Runtime.getRuntime().exec("scp PWMToSend pi@10.140.141.22:/home/pi");//sends the text file "PWMToSend" to the pi. I think can only be executed on lab computers MIGHT need to add passwords
        }
        catch(IOException e){
            UI.println("Something went wrong "+e);
        }
    }

    public void doKeys(String action){
        UI.printf("Key :%s \n", action);
        if (action.equals("b")) {
            // break - stop entering the lines
            state = 3;
            //

        }

    }

    public void doMouse(String action, double x, double y) {
        //UI.printf("Mouse Click:%s, state:%d  x:%3.1f  y:%3.1f\n",
        //   action,state,x,y);
        UI.clearGraphics();
        String out_str=String.format("%3.1f %3.1f",x,y);
        UI.drawString(out_str, x+10,y+10);
        // 
        if ((state == 1)&&(action.equals("clicked"))){
            // draw as 

            arm.inverseKinematic(x,y);
            arm.draw();
            return;
        }

        if ( ((state == 2)||(state == 3))&&action.equals("moved") ){
            // draw arm and path
            arm.inverseKinematic(x,y);
            //toSendText.add(String.valueOf(arm.get_theta1())+ ","+String.valueOf(arm.get_theta2())+",0");//0 is up/down pen
            arm.draw();

            // draw segment from last entered point to current mouse position
            if ((state == 2)&&(drawing.get_path_size()>0)){
                PointXY lp = new PointXY();
                lp = drawing.get_path_last_point();
                //if (lp.get_pen()){
                UI.setColor(Color.GRAY);
                UI.drawLine(lp.get_x(),lp.get_y(),x,y);
                // }
            }
            drawing.draw();
        }

        // add point
        if (   (state == 2) &&(action.equals("clicked"))){
            // add point(pen down) and draw
            UI.printf("Adding point x=%f y=%f\n",x,y);
            drawing.add_point_to_path(x,y,true); // add point with pen down

            arm.inverseKinematic(x,y);
            toSendText.add(this.convertToPWM(arm.get_theta1(),true)+ ","+this.convertToPWM(arm.get_theta2(),false)+",2000");//2000 is up/down pen
            arm.draw();
            drawing.draw();
            drawing.print_path();
        }

        if (   (state == 3) &&(action.equals("clicked"))){
            // add point and draw
            //UI.printf("Adding point x=%f y=%f\n",x,y);
            drawing.add_point_to_path(x,y,false); // add point wit pen up

            arm.inverseKinematic(x,y);
            toSendText.add(this.convertToPWM(arm.get_theta1(),true)+ ","+this.convertToPWM(arm.get_theta2(),false)+",2000");//2000 is up/down pen
            arm.draw();
            drawing.draw();
            drawing.print_path();
            state = 2;
        }

    }

    public String convertToPWM (double angle, boolean motor1){
        // angle is between 0 - -3.14 radians
        // angle should be converted into PWM String value 0 for 0 radian angle, each degree extra corresponds to about 10PWM, increase
        double PWMvalue = 0;
        angle = -Math.toDegrees(angle);
        double variable = motor1 ? 170 : 770; //x = motor 1, y = motor 2

        double finalPWM = angle*10 + variable;
        if(finalPWM<1000)
        finalPWM =1000;
        if(finalPWM>2000)
        finalPWM =2000;
        return String.valueOf((int)finalPWM);
    }

    public void save_xy(){
        state = 0;
        String fname = UIFileChooser.save();
        if(drawing!=null)
            drawing.save_path(fname);
    }

    public void enter_path_xy(){
        state = 2;
    }

    public void inverse(){
        state = 1;
        arm.draw();
    }

    public void load_xy(){
        state = 0;
        String fname = UIFileChooser.open();
        drawing.load_path(fname);
        drawing.draw();

        arm.draw();
    }

    // save angles into the file
    public void save_ang(){
        String fname = UIFileChooser.open();
        tool_path.convert_drawing_to_angles(drawing,arm,fname);
    }

    public void load_ang(){
    }

    public void run() {
        while(true) {
            arm.draw();
            UI.sleep(20);
        }
    }

    public static void main(String[] args){
        Main obj = new Main();
    }    

}

