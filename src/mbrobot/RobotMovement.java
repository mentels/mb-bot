package mbrobot;

import static robocode.util.Utils.normalRelativeAngleDegrees;
import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

public class RobotMovement implements IRobotMovement {

	private AdvancedRobot robot;

	private Direction direction;

	private double fileldwidth;
	private double fieldHeight;

	private double middleRobotHeight;
	private double middleRobotWidth;

	private boolean gunAdjustRequired;
	private double gunAngle;

	public RobotMovement(AdvancedRobot robot) {
		super();
		this.robot = robot;
		direction = Direction.FORWARD;
		gunAdjustRequired = false;
		fileldwidth = robot.getBattleFieldWidth();
		fieldHeight = robot.getBattleFieldHeight();
		middleRobotHeight = robot.getHeight() / 2;
		middleRobotWidth = robot.getWidth() / 2;
	}

	public void move() {
		while (true) {
			moveVertically();
			if (isRobotHeadingWall()) {
				moveOppositeDirection();
			}
			adjustGunAndSetOut();
			robot.execute();
		}
	}
	
	public void onScannedRobot(ScannedRobotEvent e) {
		gunAngle = normalRelativeAngleDegrees(e.getBearing()
				+ (robot.getHeading() - robot.getRadarHeading()));
		gunAdjustRequired = true;
	}
	
	public double getGunAngle() {
		return gunAngle;
	}

	private void adjustGunAndSetOut() {
		if (gunAdjustRequired)
			robot.setTurnGunRight(gunAngle);
		else
			robot.setTurnGunRight(360);
		if (direction.equals(Direction.FORWARD))
			robot.setAhead(500);
		else
			robot.setBack(500);
	}

	private void moveOppositeDirection() {
		direction = direction.next();
		if (direction.equals(Direction.FORWARD))
			robot.ahead(500);
		else
			robot.back(500);
	}

	private boolean isRobotHeadingWall() {
		double x = robot.getX();
		double y = robot.getY();
		return x < 20 + middleRobotWidth || x > fileldwidth - middleRobotWidth - 20
				|| y < 20 + middleRobotHeight || y > fieldHeight - middleRobotHeight - 20;
	}
	
	private void moveVertically() {
		double heading = robot.getHeading();
		if (heading == 0.0 || heading == 180.0)
			return;
		if (heading <= 90)
			robot.setTurnLeft(heading);
		else if (heading <= 180)
			robot.setTurnRight(180.0 - heading);
		else if (heading <= 270)
			robot.setTurnLeft(270.0 - heading);
		else
			robot.setTurnRight(360.0 - heading);
	}

}

enum Direction {
	FORWARD,
	BACKWARD;
	private static Direction[] vals = values();
	public Direction next() {
		return vals[(this.ordinal()+1) % vals.length];
	}
}