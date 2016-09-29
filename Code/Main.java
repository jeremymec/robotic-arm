
/* Code for Assignment ?? 
 * Name:
 * Usercode:
 * ID:
 */

import ecs100.*;
import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.image.DataBufferByte;

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
        UI.addButton("SAVE file", this::saveFile);
        UI.addButton("Challange", this::convert);
        UI.addButton("Switch Up/Down", this::switchUpDown);

        // UI.addButton("Quit", UI::quit);
        UI.setMouseMotionListener(this::doMouse);
        UI.setKeyListener(this::doKeys);

        //ServerSocket serverSocket = new ServerSocket(22); 
        this.arm = new Arm();
        this.drawing = new Drawing();
        this.run();
        arm.draw();
    }

    public void doSendFile() {

        File tosend = new File(UIFileChooser.open("Choose File to Send"));

        String usr = "pi";
        String pass = "pi";
        String IP = "10.140.153.83";
        String target = "/home/pi/Arm";

        //command = "scp " + tosend + " " + usr + "@" + IP + ":" + target;
        String[] command = new String[] {"script", "-c", "scp test.txt", "pi@" + "10.140.153.83" + ":" + "/home/pi/Arm"};
        try{
            UI.println("TRY STATEMENT ENTERED");

            Process p = new ProcessBuilder(command).start();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
            InputStream input = p.getInputStream();
            BufferedInputStream buff = new BufferedInputStream(input);
            Scanner s = new Scanner(input);

            String line;

            while (p.isAlive()){
                line = s.next();
                UI.println(line);
                if (line.contains("password")) {
                    writer.write("pi\n");
                }
            }

        } catch (IOException e){
            UI.println("OH NOES AN ERROR");
            e.printStackTrace();
        }
    }

    public void saveFile(){
        tool_path = new ToolPath();
        tool_path.convert_drawing_to_angles(drawing, arm, "does nothing");
        tool_path.convert_angles_to_pwm(arm);
        tool_path.save_pwm_file();
    }

    public void switchUpDown(){
        File f = new File(UIFileChooser.open());
        try{
            Scanner sc = new Scanner (f);
            sc.useDelimiter("//C");
            PrintStream p = new PrintStream(new File(UIFileChooser.save()));
            while (sc.hasNext()){
                p.print(sc.next()+",");
                p.print(sc.next()+",");
                p.print(sc.nextInt()==1000? "2000":"1000");
                p.println();
            }
        }
        catch(IOException e){

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

    public void convert2(){
        int tx = 230, ty = 100;
        File png = new File(UIFileChooser.open("Please choose an File from the list"));
        //Read hte image
        BufferedImage img=null;
        try {
            img = ImageIO.read(png);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Create a new image, but set it to a single byte gray pallet (using a single byte per pixel)
        if(img==null)
            UI.println("img was null");

        BufferedImage newBufferedImage = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        newBufferedImage.createGraphics().drawImage(img, 0, 0, Color.WHITE, null);
        //At this point you no longer care about the color image
        img = newBufferedImage;
        //Convert the image to an array of bytes (We know its DataBufferByte and an array of bytes here as we specified above)
        byte[] pixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
        //Is the pen currently down / was it before?
        boolean cur = false, last = false;
        int dir = 1;

        //Go to the start of the image
        drawing.add_point_to_path(tx, ty, false);
        for (int y = 0; y < img.getHeight(); y++) {
            //Add the current point to the page. Also, factor in the alternating direction
            drawing.add_point_to_path((dir == 1 ? 0 : (img.getWidth()) + tx)*0.5, (y + ty)*0.5, false);
            //Alternate direction so that the pen doesn't cross the page
            for (int x = dir == 1 ? 0 : img.getWidth() - 1; x >= 0 && x < img.getWidth(); x += dir) {
                cur = pixels[(y * img.getWidth()) + x] >= 0;
                //We only care when the pixels are different to the last drawn pixel
                if (!cur) {
                    if (cur) {
                        drawing.add_point_to_path((x + tx)*0.5, (y + ty)*0.5, false);
                    } else {
                        //Remember, if your going from black to white, you want to draw the line to the prev
                        //pixel, not current, so take dir
                        drawing.add_point_to_path((x - dir + tx)*0.5, (y + ty)*0.5, true);
                    }
                }
                last = cur;
            }
            //If your at the end of the page, and the pen was down, we should finish that stroke
            if (cur && dir == 1) {
                drawing.add_point_to_path((img.getWidth() + tx)*0.5, (y + ty)*0.5, true);
            }
            if (cur && dir == -1) {
                drawing.add_point_to_path(tx, y + ty, true);
            }
            dir = -dir;
        }
    }

    public void convert() {
        int tx = 230, ty = 80;
        File png = new File(UIFileChooser.open("Please choose an File from the list"));
        //Read hte image
        BufferedImage img=null;
        try {
            img = ImageIO.read(png);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Create a new image, but set it to a single byte gray pallet (using a single byte per pixel)
        if(img==null)
            UI.println("img was null");

        BufferedImage newBufferedImage = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        newBufferedImage.createGraphics().drawImage(img, 0, 0, Color.WHITE, null);
        //At this point you no longer care about the color image
        img = newBufferedImage;
        //Convert the image to an array of bytes (We know its DataBufferByte and an array of bytes here as we specified above)
        byte[] pixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
        //Is the pen currently down / was it before?
        boolean cur = false, last = false;
        int dir = 1;

        //Go to the start of the image
        drawing.add_point_to_path(tx, ty, false);
        for (int y = 0; y < img.getHeight(); y++) {
            //Add the current point to the page. Also, factor in the alternating direction
            drawing.add_point_to_path((dir == 1 ? 0 : img.getWidth()) + tx, y + ty, false);
            //Alternate direction so that the pen doesn't cross the page
            for (int x = dir == 1 ? 0 : img.getWidth() - 1; x >= 0 && x < img.getWidth(); x += dir) {
                cur = pixels[(y * img.getWidth()) + x] >= 0;
                //We only care when the pixels are different to the last drawn pixel
                if (cur != last) {
                    if (cur) {
                        drawing.add_point_to_path(x + tx, y + ty, true);
                    } else {
                        //Remember, if your going from black to white, you want to draw the line to the prev
                        //pixel, not current, so take dir
                        drawing.add_point_to_path((x - dir) + tx, y + ty, false);
                    }
                }
                last = cur;
            }
            //If your at the end of the page, and the pen was down, we should finish that stroke
            if (cur && dir == 1) {
                drawing.add_point_to_path(img.getWidth() + tx, y + ty, true);
            }
            if (cur && dir == -1) {
                drawing.add_point_to_path(tx, y + ty, true);
            }
            dir = -dir;
        }
        drawing.draw();
    }

    public static void main(String[] args){
        Main obj = new Main();
    }    

}
