package ToWebSite;

        // stores cartesian coordinates of the path points
    public class PointXY{
        private double x;
        private double y;
        private boolean pen_down; // position of the pen when arm moves
                                  // toward this point
        public PointXY(){
        }
                                  
        public PointXY(double xi,double yi,boolean pen){
            pen_down = pen;
            x = xi;
            y = yi;
        }
        
        // set, get
        public double get_x(){
            return x;
        }
        public double get_y(){
            return y;
        }
        public boolean get_pen(){
            return pen_down;
        }
        public void set_x(double xi){
            x = xi;
        }
        public void set_y(double yi){
            y=yi;
        }
        public void set_pen(boolean peni){
            pen_down=peni;
        }
        
    }
