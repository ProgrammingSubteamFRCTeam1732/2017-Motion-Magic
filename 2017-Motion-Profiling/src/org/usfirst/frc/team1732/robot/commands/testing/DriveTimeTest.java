package org.usfirst.frc.team1732.robot.commands.testing;

import org.usfirst.frc.team1732.robot.Robot;

import edu.wpi.first.wpilibj.command.Command;

public class DriveTimeTest extends Command {

    private Robot robot = Robot.getInstance();
    private static boolean forward = true;
    private double left;
    private double right;

    public DriveTimeTest(double sec, double speed) {
	this(sec, speed, speed);
    }

    public DriveTimeTest(double sec, double leftSpeed, double rightSpeed) {
	// Use requires() here to declare subsystem dependencies
	// eg. requires(chassis);
	requires(robot.drivetrain);
	setTimeout(sec);
	int sign = forward ? 1 : -1;
	left = leftSpeed * sign;
	right = rightSpeed * sign;
	forward = !forward;
    }

    // Called just before this Command runs the first time
    @Override
    protected void initialize() {
	robot.drivetrain.changeToPercentVBus();
	robot.drivetrain.driveWithJoysticks(left, right);
    }

    private double leftMaxVel = 0;
    private double rightMaxVel = 0;

    // store the 4 most recent velocities
    private double[] leftVels = new double[4];
    private double[] rightVels = new double[4];
    // store 4 most recent time stamps
    private long[] times = new long[4];
    // make sure to get enough measurements before calculating stuff
    private int timesDone = 0;
    private int index = 0;
    /*
     * 3 slots because it's storing 3 max accelerations
     * 
     * spot 0 holds a length 1 secant line calculation (uses index and index - 1
     * for acceleration calculation)
     * 
     * spot 1 holds a length 2 calculation (uses index and index - 2)
     * 
     * spot 2 holds a length 3 calculation (uses index and index - 3)
     * 
     */
    private double[] rightMaxAccel = new double[3];
    private double[] leftMaxAccel = new double[3];

    // Called repeatedly when this Command is scheduled to run
    @Override
    protected void execute() {
	double leftVelocity = Math.abs(robot.drivetrain.motionMagic.left.getVelocity());
	leftMaxVel = Math.max(leftVelocity, leftMaxVel);

	double rightVelocity = Math.abs(robot.drivetrain.motionMagic.right.getVelocity());
	rightMaxVel = Math.max(rightVelocity, rightMaxVel);

	// keep track of max accel
	leftVels[index] = leftVelocity;
	rightVels[index] = rightVelocity;
	times[index] = System.currentTimeMillis();

	for (int i = 0; i < 3 && i < timesDone; i++) {
	    int prev = (index + 3 - i) % 4;
	    double accel = (rightVels[index] - rightVels[prev]) / (times[index] - times[prev]);
	    rightMaxAccel[i] = Math.max(accel, rightMaxAccel[i]);
	}
	for (int i = 0; i < 3 && i < timesDone; i++) {
	    int prev = (index + 3 - i) % 4;
	    double accel = (leftVels[index] - leftVels[prev]) / (times[index] - times[prev]);
	    leftMaxAccel[i] = Math.max(accel, leftMaxAccel[i]);
	}
	index = (index + 1) % 4;
	if (timesDone < 3)
	    timesDone++;
    }

    // Make this return true when this Command no longer needs to run execute()
    @Override
    protected boolean isFinished() {
	return isTimedOut();
    }

    // Called once after isFinished returns true
    @Override
    protected void end() {
	System.out.println("Drive Time Max Velocities: ");
	System.out.println("Left Max: " + leftMaxVel);
	System.out.println("Right Max: " + rightMaxVel);
	System.out.println("Drive Time Max Accelerations");
	System.out.printf("Left: %.3f, %.3f, %.3f%n");
	System.out.printf("Right: %.3f, %.3f, %.3f%n");
	robot.drivetrain.driveWithJoysticks(0, 0);
    }

}
