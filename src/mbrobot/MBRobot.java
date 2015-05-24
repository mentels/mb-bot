package mbrobot;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;

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

	private HashMap<Bullet, SavedState> firedBullets;
	private Bullet lastFired;
	
	private static final double MISSED_REWARD = -1.20;
	private static final double HIT_REWARD = 24.0;

	public void run() {
		movementControl = new RobotMovement(this);
		missed = 0;
		hits = 0;

		try {
			loadPiqleSelector();
		} catch (IOException | ClassNotFoundException _) {
			makePiqleSelector();
		}

		firedBullets = new HashMap<Bullet, SavedState>();
		
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
		Bullet b = event.getBullet();
		if (!firedBullets.containsKey(b))
			return;
		updateNextStateForBulletEventAndLearn(b, HIT_REWARD);
		firedBullets.remove(b);
		++hits;
	}

	@Override
	public void onBulletMissed(BulletMissedEvent event) {
		Bullet b = event.getBullet();
		if (!firedBullets.containsKey(b))
			return;
		updateNextStateForBulletEventAndLearn(b, MISSED_REWARD);
		firedBullets.remove(b);
		missed++;
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		movementControl.onScannedRobot(e);
		State state = createStateOnScannedRobot(e);
		updateStateOnScannedRobot(state);
		chooseTheBestActionOnScannedRobotAndLearn(state);

	}
	
	private void updateNextStateForBulletEventAndLearn(Bullet b, double reward) {
		State nextState = null;
		if (firedBullets.get(b).nextState != null) {
			nextState = firedBullets.get(b).nextState;
		} else {
			nextState = firedBullets.get(b).currentState;
		}
		assert (nextState != null);
		selector.learn(firedBullets.get(b).currentState,
				nextState, new Action(true), reward);
	}

	private void chooseTheBestActionOnScannedRobotAndLearn(State state) {
		Action action = (Action) selector.bestAction(state);
		if (action.shoot) {
			lastFired = fireBullet(1);
			// the last fired is used in missed or hit events
			firedBullets.put(lastFired, new SavedState(state, null));
		} else {
			selector.learn(state, state, new Action(false), -1.0);
		}
	}

	private void updateStateOnScannedRobot(State state) {
		SavedState lastFiredState = firedBullets.get(lastFired);
		if (lastFiredState != null && lastFiredState.nextState == null)
			lastFiredState.nextState = state;
	}
	
	private State createStateOnScannedRobot(ScannedRobotEvent e) {
		double gunAngle = movementControl.getGunAngle();
		return new State(gunAngle, e.getVelocity(), e.getHeading(), e.getDistance());
	}

	private class SavedState {
		public State currentState;
		public State nextState;

		public SavedState(State currentState, State nextState) {
			this.currentState = currentState;
			this.nextState = nextState;
		}
	}
}
