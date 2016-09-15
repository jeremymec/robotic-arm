import ecs100.*;

import java.awt.Color;

public class Arm {

	// fixed arm parameters
	private int xm1; // coordinates of the motor(measured in pixels of the picture)
	private int ym1;
	private int xm2;
	private int ym2;
	private double r; // length of the upper/fore arm
	// parameters of servo motors - linear function pwm(angle)
	// each of two motors has unique function which should be measured
	// linear function cam be described by two points
	// motor 1, point1
	private double pwm1_val_1;
	private double theta1_val_1;
	// motor 1, point 2
	private double pwm1_val_2;
	private double theta1_val_2;

	// motor 2, point 1
	private double pwm2_val_1;
	private double theta2_val_1;
	// motor 2, point 2
	private double pwm2_val_2;
	private double theta2_val_2;

	// current state of the arm
	private double theta1; // angle of the upper arm
	private double theta2;

	private double xj1; // positions of the joints
	private double yj1;
	private double xj2;
	private double yj2;
	private double xt; // position of the tool
	private double yt;
	private boolean valid_state; // is state of the arm physically possible?

	/**
	 * Constructor for objects of class Arm NEED to manually change location of both motors based on where they actually
	 * are in relation to camera and set r to be correct
	 */
	public Arm() {
		xm1 = 290; // set motor coordinates
		ym1 = 372;
		xm2 = 379;
		ym2 = 374;
		r = 156.0;
		theta1 = -90.0 * Math.PI / 180.0; // initial angles of the upper arms might need to be changed
		theta2 = -90.0 * Math.PI / 180.0;
		valid_state = false;
	}

	// draws arm on the canvas
	public void draw() {
		// draw arm
		int height = UI.getCanvasHeight();
		int width = UI.getCanvasWidth();
		// calculate joint positions
		xj1 = xm1 + r * Math.cos(theta1);
		yj1 = ym1 + r * Math.sin(theta1);
		xj2 = xm2 + r * Math.cos(theta2);
		yj2 = ym2 + r * Math.sin(theta2);

		// SINGULARITY CHECK, whether the joint distances are close to 2*r length apart
		// Might need to move this code into inverse kinematics method
		// 1.985 controls how close the arm can get to being in a singularity position. (2 is max)
		if (Math.hypot(xj1 - xj2, yj1 - yj2) >= 1.985 * r) {
			valid_state = false;
			UI.println("Singularity position");
			return;
		}

		// draw motors and write angles
		int mr = 20;
		UI.setLineWidth(5);
		UI.setColor(Color.BLUE);
		UI.drawOval(xm1 - mr / 2, ym1 - mr / 2, mr, mr);
		UI.drawOval(xm2 - mr / 2, ym2 - mr / 2, mr, mr);
		// write parameters of first motor
		String out_str = String.format("t1=%3.1f", theta1 * 180 / Math.PI);
		UI.drawString(out_str, xm1 - 2 * mr, ym1 - mr / 2 + 2 * mr);
		out_str = String.format("xm1=%d", xm1);
		UI.drawString(out_str, xm1 - 2 * mr, ym1 - mr / 2 + 3 * mr);
		out_str = String.format("ym1=%d", ym1);
		UI.drawString(out_str, xm1 - 2 * mr, ym1 - mr / 2 + 4 * mr);
		// ditto for second motor
		out_str = String.format("t2=%3.1f", theta2 * 180 / Math.PI);
		UI.drawString(out_str, xm2 + 2 * mr, ym2 - mr / 2 + 2 * mr);
		out_str = String.format("xm2=%d", xm2);
		UI.drawString(out_str, xm2 + 2 * mr, ym2 - mr / 2 + 3 * mr);
		out_str = String.format("ym2=%d", ym2);
		UI.drawString(out_str, xm2 + 2 * mr, ym2 - mr / 2 + 4 * mr);
		// draw Field Of View
		UI.setColor(Color.GRAY);
		UI.drawRect(0, 0, 640, 480);

		// it can b euncommented later when
		// kinematic equations are derived
		if (valid_state) {
			// draw upper arms
			UI.setColor(Color.GREEN);
			UI.drawLine(xm1, ym1, xj1, yj1);
			UI.drawLine(xm2, ym2, xj2, yj2);
			// draw forearms
			UI.drawLine(xj1, yj1, xt, yt);
			UI.drawLine(xj2, yj2, xt, yt);
			// draw tool
			double rt = 20;
			UI.drawOval(xt - rt / 2, yt - rt / 2, rt, rt);
		}

	}

	/**
	 * Calculates where the pen should be based on what angles the motors are turned in Angles must be known and
	 * location of motors must be known
	 */
	public void directKinematic() {

		// calculates location of joint 1 and 2.
		xj1 = xm1 + r * Math.cos(theta1);
		yj1 = ym1 + r * Math.sin(theta1);// the + here might have to be a minus

		xj2 = xm2 + r * Math.cos(theta2);// might need to change the pi-
		yj2 = ym2 + r * Math.sin(theta2);// the + here might have to be a minus

		// midpoint between joints
		double xa = (xj1 + xj2) / 2;
		double ya = (yj1 + yj2) / 2;
		// distance between joints 1 and 2.
		double d = Math.sqrt(Math.pow((xj1 - xj2), 2) + Math.pow((yj1 - yj2), 2));
		if (d < 2 * r) {// condition might need changing
			valid_state = true;

			// half distance between tool positions
			double h = Math.sqrt(Math.pow(r, 2) - Math.pow((d / 2), 2));
			double alpha = Math.atan((yj1 - yj2) / (xj2 - xj1));

			// tool position
			double xt = xa + h * Math.cos(Math.PI / 2 - alpha);
			double yt = ya + h * Math.sin(Math.PI / 2 - alpha);
			double xt2 = xa - h * Math.cos(alpha - Math.PI / 2);
			double yt2 = ya - h * Math.sin(alpha - Math.PI / 2);// might need to swap formula around
		} else {
			valid_state = false;
		}

	}

	/**
	 * This method is given the new location of the pen, it changes the location of the pen to new coordinates, it then
	 * calculates where joint 1 and joint 2 are and then what angle the motors must be turned at and sets the angle of
	 * those motors to that. Must be given a new location of pen and must know the location of the motors. Directly
	 * modifies the fields
	 * 
	 * @param xt_new
	 *            new x position of pen
	 * @param yt_new
	 *            new y position of pen
	 */
	public void inverseKinematic(double xt_new, double yt_new) {

		valid_state = true;
		xt = xt_new;
		yt = yt_new;
		valid_state = true;

		// Finds the gradient, mid point and length between motor1 and pen/tool
		// change in x coordinates
		double differenceX1 = xt - xm1;
		double differenceY1 = yt - ym1;
		// mid point
		double midX1 = (xm1 + xt) / 2;
		double midY1 = (ym1 + yt) / 2;
		// distance between pen and motor
		double distance1 = Math.hypot((differenceX1), (differenceY1));

		if (distance1 > 2 * r) {// checks distance is in reach
			UI.println("Arm 1 - can not reach");
			valid_state = false;
			return;
		}
		double halfDistance1 = distance1 / 2;// half the distance between pen and motor

		// h is the distance between the midpoint and the potential positions of joint 1
		double h = Math.sqrt(r * r - halfDistance1 * halfDistance1);

		// math
		double alpha = Math.atan(differenceY1 / differenceX1);
		double xj11 = midX1 - h * Math.cos(Math.PI / 2 + alpha);
		double yj11 = midY1 - h * Math.sin(Math.PI / 2 + alpha);
		double xj12 = midX1 + h * Math.cos(Math.PI / 2 + alpha);
		double yj12 = midY1 + h * Math.sin(Math.PI / 2 + alpha);

		// the two possible angles motor 1 could be at
		double possTheta = Math.atan2(yj11 - ym1, xj11 - xm1);
		double possTheta2 = Math.atan2(yj12 - ym1, xj12 - xm1);

		if (possTheta2 > Math.PI / 2)// ensures correct joint position is selected
			possTheta = possTheta2;

		theta1 = Math.min(possTheta, possTheta2);

		if ((theta1 > 0) || (theta1 < -Math.PI)) {
			valid_state = false;
			UI.println("Angel 1 -invalid");
			return;
		}
		/**
		 * --------------------------------------------------------------------------------------------------------
		 * Repeat for motor 2.
		 */
		double dx2 = xt - xm2;// difference between pen and motor1 x points
		double dy2 = yt - ym2;
		double midX2 = (xm2 + xt) / 2;// mid point x coordinate
		double midY2 = (ym2 + yt) / 2;

		// distance between pen and motor
		double distance2 = Math.hypot((dx2), (dy2));
		if (distance2 > 2 * r) {
			UI.println("Arm 2 - can not reach");
			valid_state = false;
			return;
		}

		double halfDistance2 = distance2 / 2;// half the distance between pen and motor
		double h2 = Math.sqrt(r * r - halfDistance2 * halfDistance2);// h, distance between mid point and first possible
																		// joint point

		double alpha2 = Math.atan((yt - ym2) / (xt - xm2));// result of aTan the gradient between pen and motor

		double xj21 = midX2 - h2 * Math.cos(Math.PI / 2 + alpha2);// possible positions of joint 1
		double yj21 = midY2 - h2 * Math.sin(Math.PI / 2 + alpha2);// CHANGE swap 90 with alpha

		double xj22 = midX2 + h2 * Math.cos(Math.PI / 2 + alpha2);
		double yj22 = midY2 + h2 * Math.sin(Math.PI / 2 + alpha2);

		double poss2Theta = Math.atan2(yj21 - ym2, xj21 - xm2);
		double poss2Theta2 = Math.atan2(yj22 - ym2, xj22 - xm2);

		if (poss2Theta2 > Math.PI / 2)// check for extreme joint
			poss2Theta2 = poss2Theta;

		theta2 = Math.max(poss2Theta, poss2Theta2);
		if ((theta2 > 0) || (theta2 < -Math.PI)) {
			valid_state = false;
			UI.println("Angel 2 -invalid");
			return;
		}

	}

	// returns angle of motor 1
	public double get_theta1() {
		return theta1;
	}

	// returns angle of motor 2
	public double get_theta2() {
		return theta2;
	}

	// sets angle of the motors
	public void set_angles(double t1, double t2) {
		theta1 = t1;
		theta2 = t2;
	}

	// returns motor control signal
	// for motor to be in position(angle) theta1
	// linear intepolation
	public int get_pwm1() {
		int pwm = 0;
		return pwm;
	}

	// ditto for motor 2
	public int get_pwm2() {
		int pwm = 0;
		// pwm = (int)(pwm2_90 + (theta2 - 90)*pwm2_slope);
		return pwm;
	}

}
