package mbrobot;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;

import robocode.AdvancedRobot;
import robocode.BattleEndedEvent;
import robocode.Bullet;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.RobocodeFileOutputStream;
import robocode.RobocodeFileWriter;
import robocode.RoundEndedEvent;
import robocode.ScannedRobotEvent;
import algorithms.QLearningSelector;

public class MBRobot extends AdvancedRobot {

	private IRobotMovement movementControl;

	private int missed;
	private int hits;

	private QLearningSelector selector;

	private State beforeLastFiredBulletState;
	private Bullet lastFiredBullet;
	private double lastFiredBulletReward;
	
	
	private static final double MISSED_REWARD = PropertiesReader.getInstance().getMissedReward();
	private static final double HIT_REWARD = PropertiesReader.getInstance().getHitReward();

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
		selector.setEpsilon(.3);
		// Factor by which we multiply alpha at each learning step
		selector.setGeometricAlphaDecay();
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
		return getDataFile("shooting.log").toString();
	}

	@Override
	public void onRoundEnded(RoundEndedEvent event) {
		super.onRoundEnded(event);

		try {
			savePiqleSelector();
			saveShootingStats();
		} catch (IOException i) {
			i.printStackTrace();
		}

		System.out.println("Missed: " + missed + " Hit: " + hits);
	}

	private void saveShootingStats() throws IOException {
		PrintWriter writer = new PrintWriter(new RobocodeFileWriter(shootingStatsFileLocation(),
				true));
		double ratio = ((double) hits) / missed;
		writer.println(ratio);
		writer.close();
	}

	private void savePiqleSelector() throws IOException {
		RobocodeFileOutputStream fileOut = new RobocodeFileOutputStream(selectorFileLocation());
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		out.writeObject(selector);
		out.close();
		fileOut.close();
	}

	@Override
	public void onBattleEnded(BattleEndedEvent event) {
		try {
			PrintWriter writer = new PrintWriter(new RobocodeFileWriter(shootingStatsFileLocation(),
					true));
			writer.println();
			writer.close();
		} catch (IOException i) {
			i.printStackTrace();
		}
	}

	@Override
	public void onBulletHit(BulletHitEvent event) {
		lastFiredBulletReward = HIT_REWARD;
		++hits;
	}

	@Override
	public void onBulletMissed(BulletMissedEvent event) {
		lastFiredBulletReward = MISSED_REWARD;
		missed++;
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e) {
		movementControl.onScannedRobot(e);
		State state = createStateOnScannedRobot(e);
		learnBasedOnLastFiredBullet(state);
		fireOnScannedRobotAndUpdateLastFiredBullet(state);
	}
	
	private void learnBasedOnLastFiredBullet(State currentState) {
		if(lastFiredBullet != null) {
			FireWithPowerAction lastAction = 
					new FireWithPowerAction(lastFiredBullet.getPower());
			selector.learn(beforeLastFiredBulletState, currentState, lastAction, 
					lastFiredBulletReward);
		}
	}

	private void fireOnScannedRobotAndUpdateLastFiredBullet(State state) {
		FireWithPowerAction action = (FireWithPowerAction) selector.bestAction(state);
		beforeLastFiredBulletState = state;
		lastFiredBullet = fireBullet(action.power);
	}
	
	private State createStateOnScannedRobot(ScannedRobotEvent e) {
		double gunAngle = movementControl.getGunAngle();
		return new State(gunAngle, e.getVelocity(), e.getHeading(), e.getDistance());
	}

}
