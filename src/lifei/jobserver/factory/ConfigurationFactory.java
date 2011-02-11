package lifei.jobserver.factory;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class ConfigurationFactory {
	
	private static Configuration config = null;

	private ConfigurationFactory() {
	}
	
	public static Configuration build(String file) throws ConfigurationException {
		if(config == null) {
			config = new PropertiesConfiguration(file);
		}
		
		return config;
	}
	
	public static Configuration getConfig() {
		return config;
	}
}
