package mbrobot;

import static robocode.util.Utils.normalRelativeAngleDegrees;
import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

public class ErraticMoveBehaviour implements IMoveBehaviour {

	private AdvancedRobot robot;

	private ErraticMoveMode moveMode;

	private boolean forward;
	private double width;
	private double height;

	private double selfHeight;
	private double selfWidth;

	private boolean adjustGun;
	private double gunAdjust;

	public ErraticMoveBehaviour(AdvancedRobot robot) {
		super();
		this.robot = robot;

		// Initial setting on map
		moveMode = ErraticMoveMode.VERTICAL;
		adjustGun = false;
		forward = true;
		width = robot.getBattleFieldWidth();
		height = robot.getBattleFieldHeight();
		selfHeight = robot.getHeight() / 2;
		selfWidth = robot.getWidth() / 2;
	}

	public void move() {
		while (true) {
			double heading = robot.getHeading();
			switch (moveMode) {
			case VERTICAL:
				if (heading == 0.0 || heading == 180.0)
					break;
				if (heading <= 90)
					robot.setTurnLeft(heading);
				else if (heading <= 180)
					robot.setTurnRight(180.0 - heading);
				else if (heading <= 270)
					robot.setTurnLeft(270.0 - heading);
				else
					robot.setTurnRight(360.0 - heading);
				break;
			case HORIZONTAL:
				if (heading == 90.0 || heading == 270.0)
					break;
				if (heading <= 90)
					robot.setTurnRight(90 - heading);
				else if (heading <= 180)
					robot.setTurnLeft(180 - heading);
				else if (heading <= 270)
					robot.setTurnRight(270.0 - heading);
				else
					robot.setTurnLeft(360.0 - heading);
				break;
			default:
				break;
			}
			// Check if we need to turn back
			double x = robot.getX();
			double y = robot.getY();
			if (x < 20 + selfWidth || x > width - selfWidth - 20
					|| y < 20 + selfHeight || y > height - selfHeight - 20) {
				forward = !forward;
				if (forward)
					robot.ahead(50);
				else
					robot.back(50);
			}
			if (adjustGun)
				robot.setTurnGunRight(gunAdjust);
			else
				robot.setTurnGunRight(360);
			if (forward)
				robot.setAhead(1000);
			else
				robot.setBack(1000);
			robot.execute();
		}
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		gunAdjust = normalRelativeAngleDegrees(e.getBearing()
				+ (robot.getHeading() - robot.getRadarHeading()));
		adjustGun = true;
	}

}

enum ErraticMoveMode {
	VERTICAL,
	HORIZONTAL,
	DIAGONAL,
	RECTANGLE,
	CIRCLE
}