package mbrobot;

import environment.IAction;

public class Action implements IAction {

	public boolean shoot;

	private static final long serialVersionUID = 1L;

	public Action(boolean shoot) {
		this.shoot = shoot;
	}

	@Override
	public Object copy() {
		return new Action(shoot);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Action))
			return false;
		Action a = (Action) o;
		return (this.shoot == a.shoot);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}

	@Override
	public int hashCode() {
		return this.shoot ? 1 : 0;
	}

	@Override
	public int nnCodingSize() {
		return 1;
	}

	@Override
	public double[] nnCoding() {
		double code[] = new double[1];
		code[0] = shoot ? 1.0 : 0.0;
		return code;
	}

}