package mbrobot;

import environment.IAction;

public class FireWithPowerAction implements IAction {
	
	private static final int factor = PropertiesReader.getInstance().getFirePowerFactor();
	
	private static final long serialVersionUID = 1L;

	public double power;
	
	public static final int VALUE_1 = 1;
	
	public static final int VALUE_2 = VALUE_1 * factor;
	
	public static final int VALUE_3 = VALUE_2 * factor;

	public FireWithPowerAction(double power) {
		this.power = power;
	}

	@Override
	public Object copy() {
		return new FireWithPowerAction(power);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof FireWithPowerAction))
			return false;
		FireWithPowerAction a = (FireWithPowerAction) o;
		return (this.power == a.power);
	}

	@Override
	public int hashCode() {
		return (int) this.power;
	}

	@Override
	public int nnCodingSize() {
		return 1;
	}

	@Override
	public double[] nnCoding() {
		double code[] = new double[1];
		code[0] = (double) power;
		return code;
	}

}