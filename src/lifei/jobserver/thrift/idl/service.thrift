namespace java lifei.jobserver.thrift.models

struct Job {
    1: i64 id,
    2: string user,
    3: string command,
    4: string name,
    5: string desc,
    6: i64 createtime,
    7: i64 executetime,
    8: i64 finishtime,
    9: i64 exitvalue,
    10: string stdout,
    11: string stderr,
    12: string workdir,
    13: i32 status,
    14: string type,
}

service JobService {
    i64 submitJob(1: Job job),
    list<i64> submitJobs(1: list<Job> jobs),
    void killJob(1:i64 id),
}

