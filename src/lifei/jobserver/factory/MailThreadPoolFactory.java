package lifei.jobserver.factory;

import lifei.threadpool.ThreadPoolManager;

public final class MailThreadPoolFactory {
	
	private static ThreadPoolManager _tpm = null;

	private MailThreadPoolFactory() {
	}
	
	public static ThreadPoolManager build(int core_pool_size, int max_pool_size, long keep_alive_time, int work_queue_size) {
		if(_tpm == null) {
			_tpm = new ThreadPoolManager(core_pool_size, max_pool_size, keep_alive_time, work_queue_size);
		}
		
		return _tpm;
	}
	
	public static ThreadPoolManager tpm() {
		if(_tpm == null) {
			_tpm = new ThreadPoolManager();
		}
		
		return _tpm;
	}
	

}
