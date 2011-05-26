package lifei.jobserver.global;

import java.util.concurrent.ConcurrentHashMap;

import lifei.jobserver.threadpool.JobThread;

public class JobList {
	
	private JobList() {}
	
	static ConcurrentHashMap<Long, JobThread> jobs = new ConcurrentHashMap<Long, JobThread>();
	
	public static JobThread get(Long id) {
		return jobs.get(id);
	}
	
	public static void add(Long id, JobThread job) {
		jobs.put(id, job);
	}
	
	public static void remove(Long id) {
		jobs.remove(id);
	}
}
