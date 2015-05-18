package mbrobot;

import robocode.Robot;
import robocode.ScannedRobotEvent;

public class FirstRobot extends Robot {
	public void run() {
		while (true) {
			ahead(100);
			turnGunRight(360);
			back(100);
			turnGunRight(360);
		}
	}

	public void onScannedRobot(ScannedRobotEvent event) {
		fire(1);
	}
}