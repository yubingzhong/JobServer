package lifei.jobserver.apps;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.thrift.TException;


import lifei.jobserver.config.Config;
import lifei.jobserver.entity.Job;
import lifei.jobserver.factory.JobThreadPoolFactory;
import lifei.jobserver.global.JobCounter;
import lifei.jobserver.global.JobList;
import lifei.jobserver.threadpool.JobThread;
import lifei.jobserver.thrift.models.JobService.Iface;

public class JobServiceImpl implements Iface {

	private static Logger logger = Logger.getLogger("JobServiceImpl");

	public JobServiceImpl() {
	}

	@Override
	public long submitJob(lifei.jobserver.thrift.models.Job j) throws TException {
		
		String key = j.type;
		String max = Config.get("jobs.max." + key);
		
		if(max != null && max.length() > 0) {

			try {
				int maxcnt = Integer.parseInt(max);
				if(JobCounter.get(key) >= maxcnt) {
					return -1;
				}
			} catch(Exception e) {
			}
		}
		
		JobThread jobthread  = new JobThread(j.user, j.name, j.desc, j.type, j.command);
		JobThreadPoolFactory.tpm().addTask(jobthread);
		
		logger .info("新的作业Id:" + String.valueOf(jobthread.job.id));
		
		return jobthread.job.id;
	}

	@Override
	public List<Long> submitJobs(List<lifei.jobserver.thrift.models.Job > jobs) throws TException {
		
		List<Long> ret = new ArrayList<Long>();
		for (lifei.jobserver.thrift.models.Job j : (lifei.jobserver.thrift.models.Job[])jobs.toArray()) {
			
			String key = j.type;
			String max = Config.get("jobs.max." + key);
			
			if(max != null) {
				int maxcnt = Integer.parseInt(Config.get("jobs.max." + key));
				if(JobCounter.get(key) >= maxcnt) {
					ret.add((long) -1);
					continue;
				}
			}
			
			JobThread jobthread  = new JobThread(j.user, j.name, j.desc, j.type, j.command);
			JobThreadPoolFactory.tpm().addTask(jobthread);
			ret.add(jobthread.job.id);
		}
		return ret;
	}

	@Override
	public void killJob(long id) throws TException {
		JobThread job = JobList.get(id);		
		job.interrupt();	
	}
}
