package mbrobot;

import static robocode.util.Utils.normalRelativeAngleDegrees;

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

	private IMoveBehaviour moveBehaviour;

	private int missed;
	private int hits;

	private QLearningSelector selector;

	private HashMap<Bullet, SavedState> firedBullets;
	private Bullet lastFired;

	public void run() {
		moveBehaviour = new ErraticMoveBehaviour(this);
		missed = 0;
		hits = 0;

		try {
			loadPiqleSelector();
		} catch (IOException | ClassNotFoundException _) {
			makePiqleSelector();
		}

		firedBullets = new HashMap<Bullet, SavedState>();
		
		moveBehaviour.move();
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

		State nextState = null;
		if (firedBullets.get(b).nextState != null) {
			nextState = firedBullets.get(b).nextState;
		} else {
			nextState = firedBullets.get(b).currentState;
		}
		assert (nextState != null);
		selector.learn(firedBullets.get(b).currentState, nextState, new Action(
				true), 24.0);
		firedBullets.remove(event.getBullet());
		++hits;
	}

	@Override
	public void onBulletMissed(BulletMissedEvent event) {
		Bullet b = event.getBullet();
		if (!firedBullets.containsKey(b))
			return;
		State nextState = null;
		if (firedBullets.get(b).nextState != null) {
			nextState = firedBullets.get(b).nextState;
		} else {
			nextState = firedBullets.get(b).currentState;
		}
		assert (nextState != null);
		selector.learn(firedBullets.get(b).currentState,
				nextState, new Action(true), -12.0);
		firedBullets.remove(event.getBullet());
		missed++;
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		
		moveBehaviour.onScannedRobot(e);

		double gunAdjust = normalRelativeAngleDegrees(e.getBearing()
				+ (getHeading() - getRadarHeading()));

		State state = new State(gunAdjust, e.getVelocity(),
				e.getHeading(), e.getDistance());
		// Update last fired
		SavedState lastFiredState = firedBullets.get(lastFired);
		if (lastFiredState != null && lastFiredState.nextState == null)
			lastFiredState.nextState = state;
		Action action = (Action) selector.bestAction(state);
		if (action.shoot) {
			lastFired = fireBullet(1);
			firedBullets.put(lastFired, new SavedState(state, null));
		} else {
			selector.learn(state, state, new Action(false), -1.0);
		}

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
