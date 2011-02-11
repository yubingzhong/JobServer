package lifei.jobserver.apps;

import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TException;


import lifei.jobserver.entity.Job;
import lifei.jobserver.factory.JobThreadPoolFactory;
import lifei.jobserver.threadpool.JobThread;
import lifei.jobserver.thrift.models.JobService.Iface;

public class JobServiceImpl implements Iface {

	public JobServiceImpl() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public long submitJob(lifei.jobserver.thrift.models.Job j) throws TException {
		JobThread jobthread  = new JobThread(j.user, j.name, j.desc, j.command);
		JobThreadPoolFactory.tpm().addTask(jobthread);
		
		return jobthread.job.id;
	}

	@Override
	public List<Long> submitJobs(List<lifei.jobserver.thrift.models.Job > jobs) throws TException {
		
		List<Long> ret = new ArrayList<Long>();
		for (lifei.jobserver.thrift.models.Job j : (lifei.jobserver.thrift.models.Job[])jobs.toArray()) {
			JobThread jobthread  = new JobThread(j.user, j.name, j.desc, j.command);
			JobThreadPoolFactory.tpm().addTask(jobthread);
			ret.add(jobthread.job.id);
		}
		return ret;
	}
}
