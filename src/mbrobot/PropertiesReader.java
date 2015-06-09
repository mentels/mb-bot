package mbrobot;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesReader {
	
	private static PropertiesReader instance;
	
	private Properties prop = new Properties();
	private String propFileName = "config.properties";
	
//	public PropertiesReader() throws IOException {
//		 
//		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
// 
//		if (inputStream != null) {
//			prop.load(inputStream);
//		} else {
//			throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
//		}
//	}
	
	public static PropertiesReader getInstance()  {
        if (instance == null ) {
            synchronized (PropertiesReader.class) {
                if (instance == null) {
                    instance = new PropertiesReader();
                }
            }
        }
 
        return instance;
    }
	
	public double getMissedReward() {
//		return Double.parseDouble(prop.getProperty("missed_reward"));
		return -1.20;
	}
	
	public double getHitReward() {
//		return Double.parseDouble(prop.getProperty("hit_reward"));
		return 24.0;
	}
	
	public int getFirePowerFactor() {
//		return Integer.parseInt(prop.getProperty("power_factor"));
		return 2;
	}

	public double getEpsilon() {
		return 0.3;
	}

	public String getAlphaDecay() {
		return "geometric";
	}
}
