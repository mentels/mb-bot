package mbrobot;

import environment.IAction;

public class RotateGunAction implements IAction {
	private static final long serialVersionUID = 1L;
	public int rotateRight;
	public RotateGunAction(int rotateRight) {
		this.rotateRight = rotateRight;
	}

	@Override
	public Object copy() {
		return new RotateGunAction(rotateRight);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof RotateGunAction))
			return false;
		RotateGunAction a = (RotateGunAction) o;
		return (this.rotateRight == a.rotateRight);
	}

	@Override
	public int hashCode() {
		return (int) this.rotateRight;
	}

	@Override
	public int nnCodingSize() {
		return 1;
	}

	@Override
	public double[] nnCoding() {
		double code[] = new double[1];
		code[0] = (double) rotateRight;
		return code;
	}
}