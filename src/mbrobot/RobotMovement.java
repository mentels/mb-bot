package mbrobot;

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

	private double oldEnemyHeading;

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
//		double bulletPower = Math.min(3.0, robot.getEnergy());
//		double myX = robot.getX();
//		double myY = robot.getY();
//		double absoluteBearing = robot.getHeadingRadians()
//				+ e.getBearingRadians();
//		double enemyX = robot.getX() + e.getDistance()
//				* Math.sin(absoluteBearing);
//		double enemyY = robot.getY() + e.getDistance()
//				* Math.cos(absoluteBearing);
//		double enemyHeading = e.getHeadingRadians();
//		double enemyHeadingChange = enemyHeading - oldEnemyHeading;
//		double enemyVelocity = e.getVelocity();
//		oldEnemyHeading = enemyHeading;
//
//		double deltaTime = 0;
//		double battleFieldHeight = robot.getBattleFieldHeight(), battleFieldWidth = robot
//				.getBattleFieldWidth();
//		double predictedX = enemyX, predictedY = enemyY;
//		while ((++deltaTime) * (20.0 - 3.0 * bulletPower) < Point2D.Double
//				.distance(myX, myY, predictedX, predictedY)) {
//			predictedX += Math.sin(enemyHeading) * enemyVelocity;
//			predictedY += Math.cos(enemyHeading) * enemyVelocity;
//			enemyHeading += enemyHeadingChange;
//			if (predictedX < 18.0 || predictedY < 18.0
//					|| predictedX > battleFieldWidth - 18.0
//					|| predictedY > battleFieldHeight - 18.0) {
//
//				predictedX = Math.min(Math.max(18.0, predictedX),
//						battleFieldWidth - 18.0);
//				predictedY = Math.min(Math.max(18.0, predictedY),
//						battleFieldHeight - 18.0);
//				break;
//			}
//		}
//		double theta = Utils.normalAbsoluteAngle(Math.atan2(
//				predictedX - robot.getX(), predictedY - robot.getY()));
//
//		robot.setTurnRadarRightRadians(Utils
//				.normalRelativeAngle(absoluteBearing
//						- robot.getRadarHeadingRadians()));
//		robot.setTurnGunRightRadians(Utils.normalRelativeAngle(theta
//				- robot.getGunHeadingRadians()));
		//robot.execute();
		// gunAngle = normalRelativeAngleDegrees(e.getBearing()
		// + (robot.getHeading() - robot.getRadarHeading()));
		// gunAdjustRequired = true;
		
		this.lastTime = robot.getTime();
	}

	public double getGunAngle() {
		return gunAngle;
	}

	long lastTime;

	private void adjustGunAndSetOut() {
		// if (gunAdjustRequired) {
		// robot.setTurnGunRight(gunAngle);
		// robot.setTurnRadarRight(gunAngle);
		// } else {
//
		if (robot.getGunTurnRemaining() == 0) {
			if (robot.getTime() > lastTime + 10) {
				robot.setTurnGunRight(10);
			}
		}
//		if (robot.getRadarTurnRemaining() == 0)
//			robot.setTurnRadarRight(360);
		// }
//		if (direction.equals(Direction.FORWARD))
//			robot.setAhead(500);
//		else
//			robot.setBack(500);
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
		return x < 20 + middleRobotWidth
				|| x > fileldwidth - middleRobotWidth - 20
				|| y < 20 + middleRobotHeight
				|| y > fieldHeight - middleRobotHeight - 20;
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
	FORWARD, BACKWARD;
	private static Direction[] vals = values();

	public Direction next() {
		return vals[(this.ordinal() + 1) % vals.length];
	}
}