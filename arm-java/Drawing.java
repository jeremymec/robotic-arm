package ToWebSite;


/**
 * Class represents the drawing as set of (x,y) points.
 * 
 * @author (your name) 
 * @version (a version number or a date)
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

import java.awt.Color;


public class Drawing
{
    
    // set of points
    private ArrayList<PointXY> path;

    /**
     * Constructor for objects of class Drawing
     */
    public Drawing()
    {
       path = new ArrayList<PointXY>();
    }

    public void add_point_to_path(double x, double y,boolean pen)
    {
        PointXY new_point = new PointXY(x,y,pen);
        path.add(new_point);
        UI.printf("Pioint added.x=%f y=%f pen=%b New path size - %d\n",
              x,y,pen,path.size());
    }
    
    public void print_path(){
        UI.printf("*************************\n");
        for (int i = 0; i < path.size();i++){
            
            double x0 = path.get(i).get_x();
            double y0 = path.get(i).get_y();
            boolean p = path.get(i).get_pen();
            UI.printf("i=%d x=%f y=%f pen=%b\n",i,x0,y0,p);
        }
        UI.printf("*************************\n");
    }
    
    public void draw(){
           //draw path
        for (int i = 1; i < path.size() ; i++){
            PointXY p0 = get_drawing_point(i-1);
            PointXY p1 = get_drawing_point(i); 
            if (path.get(i).get_pen()){
                UI.setColor(Color.BLUE); //pen down part
            } else {
                UI.setColor(Color.LIGHT_GRAY); // pen uo
            }
            UI.drawLine(p0.get_x(), p0.get_y(), p1.get_x(), p1.get_y());
            
        }
    }
    
    public int get_path_size(){
        return path.size();
    }
    
    //pen_down = false for last point
    public void path_raise_pen(){
        path.get(path.size()-1).set_pen(false);
    }
    
    public PointXY get_path_last_point(){
        PointXY lp = path.get(path.size()-1);
        return lp;
    }
    
    public void save_path(String fname){
        
        try {
            //Whatever the file path is.
            File statText = new File(fname);
            FileOutputStream is = new FileOutputStream(statText);
            OutputStreamWriter osw = new OutputStreamWriter(is);    
            Writer w = new BufferedWriter(osw);
            String str_out; 
            for (int i = 1; i < path.size() ; i++){
                if (path.get(i).get_pen()) {
                  str_out = path.get(i).get_x() +" "+ path.get(i).get_y() +" 1\n";
                } else {
                  str_out = path.get(i).get_x() +" "+ path.get(i).get_y() +" 0\n";
                }
                w.write(str_out);
            }
            w.close();
        } catch (IOException e) {
            UI.println("Problem writing to the file statsTest.txt");
        }
    }
    
    
    public void load_path(String fname){
        
      String  in_line = null;
      try{
         // open input stream test.txt for reading purpose.
         BufferedReader in = new BufferedReader(new FileReader(new File(fname)));
         // clear existing path
         path.clear();
         
         while ((in_line = in.readLine()) != null) {
            UI.println(in_line);
            String[] tokens = in_line.split(" ");
            UI.println("Number of tokens in line " + in_line + ": " + tokens.length);
            UI.println("The tokens are:");
            UI.printf("%s %s %s\n",tokens[0],tokens[1],tokens[2]);
            double x = Double.parseDouble(tokens[0]);
            double y = Double.parseDouble(tokens[1]);
            boolean pen = (Integer.parseInt(tokens[2]) == 1) ;
            add_point_to_path(x,y,pen);
         }       
      }catch(Exception e){
          
         e.printStackTrace();
      }
   
    }
    
    public int get_drawing_size() {
        return path.size();
    }
    
    public PointXY get_drawing_point(int i){
        PointXY p = path.get(i);
        return p;
    }
    
}
