package mbrobot;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import robocode.AdvancedRobot;
import robocode.BattleEndedEvent;
import robocode.Bullet;
import robocode.BulletHitBulletEvent;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.RobocodeFileOutputStream;
import robocode.RobocodeFileWriter;
import robocode.Robot;
import robocode.RoundEndedEvent;
import robocode.ScannedRobotEvent;
import robocode.control.RobotSetup;
import algorithms.QLearningSelector;

public class MBRobot extends AdvancedRobot {

	private IRobotMovement movementControl;

	private int missed;
	private int hits;

	private QLearningSelector selector;

	// private State beforeLastFiredBulletState;
	// private Bullet lastFiredBullet;
	// private double lastFiredBulletReward;

	private static final double MISSED_REWARD = PropertiesReader.getInstance()
			.getMissedReward();
	private static final double HIT_REWARD = PropertiesReader.getInstance()
			.getHitReward();

	public void run() {
		movementControl = new RobotMovement(this);
		missed = 0;
		hits = 0;

		try {
			loadPiqleSelector();
		} catch (IOException | ClassNotFoundException _) {
			makePiqleSelector();
		}

		movementControl.move();
	}

	private void makePiqleSelector() {
		System.out.println("NEW SELECTOR");
		selector = new QLearningSelector();
		// the probability of choosing an action at random in the e-greedy case
		selector.setEpsilon(PropertiesReader.getInstance().getEpsilon());
		selector.setAlpha(1);
		// Factor by which we multiply alpha at each learning step
		if (PropertiesReader.getInstance().getAlphaDecay() == "geometric") {
			selector.setGeometricAlphaDecay();
		} else {
			selector.setExponentialAlphaDecay();
		}
	}

	private void loadPiqleSelector() throws FileNotFoundException, IOException,
			ClassNotFoundException {
		FileInputStream fileIn = new FileInputStream(selectorFileLocation());
		ObjectInputStream in = new ObjectInputStream(fileIn);
		selector = (QLearningSelector) in.readObject();
		in.close();
		fileIn.close();
	}

	private String selectorFileLocation() {
		return getDataFile("qselector.sel").toString();
	}

	private String shootingStatsFileLocation() {
		return getDataFile("shooting.csv").toString();
	}

	private void log(String s) {
		try {
			PrintWriter writer = new PrintWriter(new RobocodeFileWriter(
					getDataFile("log.log").toString(), true));
			writer.write(s + "\n");
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onRoundEnded(RoundEndedEvent event) {
		super.onRoundEnded(event);

		try {
			savePiqleSelector();
			saveShootingStats();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		System.out.println("Missed: " + missed + " Hit: " + hits);
	}

	private void saveShootingStats() throws IOException {
		PrintWriter writer = new PrintWriter(new RobocodeFileWriter(
				shootingStatsFileLocation(), true));
		double ratio = ((double) hits) / (missed + hits);
		writer.println("" + (hits + missed) + ", " + hits + ", " + missed
				+ ", " + ratio);
		writer.close();
	}

	private void savePiqleSelector() throws IOException {
		RobocodeFileOutputStream fileOut = new RobocodeFileOutputStream(
				selectorFileLocation());
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		out.writeObject(selector);
		out.close();
		fileOut.close();
	}

	@Override
	public void onBattleEnded(BattleEndedEvent event) {
		try {
			PrintWriter writer = new PrintWriter(new RobocodeFileWriter(
					shootingStatsFileLocation(), true));
			writer.println();
			writer.close();
		} catch (IOException i) {
			i.printStackTrace();
		}
	}

	@Override
	public void onBulletHit(BulletHitEvent event) {
		double reward = HIT_REWARD;
		++hits;
		State originalState = bulletToStateMap.get(event.getBullet());
		State newState = bulletToNextStateMap.get(event.getBullet());
		selector.learn(originalState, newState,
				bulletToActionMap.get(event.getBullet()), reward);
	}

	@Override
	public void onBulletHitBullet(BulletHitBulletEvent event) {
		double reward = HIT_REWARD;
		++hits;
		State originalState = bulletToStateMap.get(event.getBullet());
		State newState = bulletToNextStateMap.get(event.getBullet());
		selector.learn(originalState, newState,
				bulletToActionMap.get(event.getBullet()), reward);
	}

	@Override
	public void onBulletMissed(BulletMissedEvent event) {
		double reward = MISSED_REWARD;
		missed++;
		State originalState = bulletToStateMap.get(event.getBullet());
		State newState = bulletToNextStateMap.get(event.getBullet());
		selector.learn(originalState, newState,
				bulletToActionMap.get(event.getBullet()), reward);
	}

	private Map<Bullet, State> bulletToStateMap = new HashMap<Bullet, State>();
	private Map<Bullet, State> bulletToNextStateMap = new HashMap<Bullet, State>();
	private Map<Bullet, FireWithPowerAction> bulletToActionMap = new HashMap<Bullet, FireWithPowerAction>();

	@Override
	public void onScannedRobot(ScannedRobotEvent e) {
		movementControl.onScannedRobot(e);
		//setTurnGunRight(0);
		
//		while (getTurnRemaining() != 0 ) {
//			execute();
//		}
		
		
		State state = createStateOnScannedRobot(e);
		State newState = createStateOnScannedRobot(e);
		FireWithPowerAction action = (FireWithPowerAction) selector
				.bestAction(state);
		// beforeLastFiredBulletState = state;
		double rotate = (action.rotateRight - 5) * 2;
		log(rotate + "");
		turnGunRight(rotate);
		while (getTurnRemaining() != 0 ) {
			execute();
		}
		
		Bullet b = fireBullet(3);
		execute();
		bulletToStateMap.put(b, state);
		newState.setGunAngle(newState.gunAngle - rotate);
		// log("scanned");
		bulletToNextStateMap.put(b, newState);
		bulletToActionMap.put(b, action);
	}

	private State createStateOnScannedRobot(ScannedRobotEvent e) {
		// double gunAngle = movementControl.getGunAngle();
		return new State(this.getGunHeading(), e.getVelocity(), e.getHeading(),
				e.getDistance());
	}
}
