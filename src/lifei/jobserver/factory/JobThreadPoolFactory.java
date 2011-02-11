package lifei.jobserver.factory;

import lifei.threadpool.ThreadPoolManager;

public final class JobThreadPoolFactory {
	
	private static ThreadPoolManager _instance = null;

	private JobThreadPoolFactory() {
	}
	
	public static ThreadPoolManager build(int core_pool_size, int max_pool_size, long keep_alive_time, int work_queue_size) {
		if(_instance == null) {
			_instance = new ThreadPoolManager(core_pool_size, max_pool_size, keep_alive_time, work_queue_size);
		}
		
		return _instance;
	}
	
	public static ThreadPoolManager tpm() {
		if(_instance == null) {
			_instance = new ThreadPoolManager();
		}
		
		return _instance;
	}
	
	

}
