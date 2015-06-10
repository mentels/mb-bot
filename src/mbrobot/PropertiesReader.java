package mbrobot;

public class PropertiesReader {
	private static PropertiesReader instance;

	public static PropertiesReader getInstance() {
		if (instance == null) {
			synchronized (PropertiesReader.class) {
				if (instance == null) {
					instance = new PropertiesReader();
				}
			}
		}

		return instance;
	}

	public double getMissedReward() {
		return -10;
	}

	public double getHitReward() {
		return 2;
	}

	public int getFirePowerFactor() {
		return 5;
	}

	public double getEpsilon() {
		return 0.3;
	}

	public String getAlphaDecay() {
		return "geometric";
	}
}
