package mbrobot;

import environment.ActionList;
import environment.IAction;
import environment.IEnvironment;
import environment.IState;

public class State implements IState {

	public State(double gunAngle, double enemyVelocity, double enemyAngle,
			double enemyDistance) {
		this.gunAngle = (int) Math.round(gunAngle * 100);
		this.enemyAngle = (int) Math.round(enemyAngle);
		this.enemyMoving = enemyVelocity > 0.0;
		this.enemyDistance = calculateDistance(enemyDistance);
	}

	private State(int gunAngle, boolean enemyMoving, int enemyAngle,
			Distance enemyDistance) {
		this.gunAngle = gunAngle;
		this.enemyMoving = enemyMoving;
		this.enemyAngle = enemyAngle;
		this.enemyDistance = enemyDistance;
	}

	private static final long serialVersionUID = 1L;

	public int gunAngle;
	public boolean enemyMoving;
	public int enemyAngle;
	public Distance enemyDistance;

	@Override
	public ActionList getActionList() {
		ActionList a = new ActionList(this);
		a.add(new Action(false));
		a.add(new Action(true));
		return a;
	}

	@Override
	public void setEnvironment(IEnvironment c) {
	}

	@Override
	public IState modify(IAction a) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IEnvironment getEnvironment() {
		return null;
	}

	@Override
	public String toString() {
		return "State: angle:" + gunAngle + " moving:" + enemyMoving + " head:"
				+ enemyAngle + " dist:" + enemyDistance;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof State) {
			State o = (State) obj;
			return this.enemyAngle == o.enemyAngle
					&& this.enemyDistance == o.enemyDistance
					&& this.enemyMoving == o.enemyMoving
					&& this.gunAngle == o.gunAngle;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.enemyAngle * 1000 + (this.enemyMoving ? 1 : 0) * 100
				+ this.enemyDistance.ordinal() * 10 + this.gunAngle;
	}

	@Override
	public double getReward(IState old, IAction a) {
		return 0;
	}

	@Override
	public boolean isFinal() {
		return false;
	}

	@Override
	public IState copy() {
		return new State(gunAngle, enemyMoving, enemyAngle, enemyDistance);
	}

	@Override
	public int nnCodingSize() {
		return 4;
	}

	@Override
	public double[] nnCoding() {
		double code[] = new double[4];
		code[0] = gunAngle;
		code[1] = enemyMoving ? 1 : 0;
		code[2] = enemyAngle;
		code[3] = enemyDistance.ordinal();
		return code;
	}

	private Distance calculateDistance(double distance) {
		if (distance < 40.0)
			return Distance.Near;
		if (distance < 100.0)
			return Distance.Medium;
		return Distance.Far;
	}

	public enum Distance {
		Near, Medium, Far
	}
}
