package lifei.jobserver.helper;

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

public class EnvHelper {
	
	private static Logger logger = Logger.getLogger("EnvHelper");
	
	private static String[] arr = null;

	private EnvHelper() {
		// TODO Auto-generated constructor stub
	}
	
	public static String[] getEnv() {
		
		if(arr != null)
			return arr;
		
		
		Map<String,String> env = System.getenv();
		Iterator<String> iter = env.keySet().iterator();
		

		arr = new String[0];
		
		while(iter.hasNext()) {
			String key = iter.next();
			
			arr = (String[]) ArrayUtils.add(arr, key + "=" + env.get(key));
			logger.debug(key + ":" + env.get(key));
		}
		
		return arr;
	}


}
