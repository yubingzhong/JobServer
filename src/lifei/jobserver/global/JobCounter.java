package lifei.jobserver.global;

import java.util.HashMap;

public class JobCounter {
	
	static HashMap<String, Integer> counter = new HashMap<String, Integer>();

	private JobCounter() {
	}
	
	public synchronized static void decrease(String key) {		
		if(key != null && key.length() > 0) {
			Integer cnt = counter.get(key);
			if(cnt != null && cnt > 0) {
				counter.put(key,  cnt--);
			}
		}
	}
	
	public synchronized static void increase(String key) {
		if(key != null && key.length() > 0) {
			Integer cnt = counter.get(key);
			if(cnt != null && cnt > 0) {
				counter.put(key,  cnt++);
			}
		}
	}
	
	public synchronized static int get(String key) {
		if(key == null || key.length() == 0) {
			return 0;
		}

		return counter.get(key);
	}
}
