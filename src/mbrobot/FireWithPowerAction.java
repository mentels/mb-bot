package mbrobot;

import environment.IAction;

public class FireWithPowerAction implements IAction {
	
	private static final int factor = PropertiesReader.getInstance().getFirePowerFactor();
	
	private static final long serialVersionUID = 1L;

	public int rotateRight;
	
	public FireWithPowerAction(int rotateRight) {
		this.rotateRight = rotateRight;
	}

	@Override
	public Object copy() {
		return new FireWithPowerAction(rotateRight);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof FireWithPowerAction))
			return false;
		FireWithPowerAction a = (FireWithPowerAction) o;
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