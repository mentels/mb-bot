package mbrobot;

import robocode.ScannedRobotEvent;

public interface IMoveBehaviour {

	public void move();

	public void onScannedRobot(ScannedRobotEvent e);

}