package mbrobot;

import robocode.ScannedRobotEvent;

public interface IRobotMovement {

	public void move();

	public void onScannedRobot(ScannedRobotEvent e);

	public double getGunAngle();
}