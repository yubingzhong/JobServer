package lifei.jobserver.threadpool;

import groovy.text.GStringTemplateEngine;
import groovy.text.Template;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import lifei.crawler.apps.XCrawler;
import lifei.jobserver.config.Config;
import lifei.jobserver.entity.Job;
import lifei.jobserver.factory.HibernateSessionFactory;
import lifei.jobserver.factory.MailThreadPoolFactory;
import lifei.jobserver.helper.EnvHelper;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.codehaus.groovy.control.CompilationFailedException;
import org.hibernate.Session;

public class JobThread implements Runnable {
	
	public Job job = null;
	private Session session = null;	
	private Process process = null;
	private File workdir = null;

	private HashMap<String, Object > map = new HashMap<String, Object >();	
	
	private static Logger logger = Logger.getLogger("JobThread");

	public JobThread(String user, String name, String desc, String command) {		
		this.job = new Job(user, command, name, desc);		
		this.session = HibernateSessionFactory.getSession();
		job.save(session);
	}

	@Override
	public void run() {
		try {
			this.runBefore();
			this.proccess();
		} catch (Exception e) {
			logger.fatal("致命错误：" + e.getMessage());
		} finally {
			this.runAfter();
		}

	}

	private void runAfter() {
		this.session.close();
		
		if(!Config.get("jobserver.email").equals("true")) {
			return;
		}
		
		// 发送邮件
		
		StringBuffer sb = new StringBuffer(1000);

		
		sb.append("<table border=\"1\" width=\"100%\">");
		sb.append("<tr><th colspan=\"2\">${job.id}号作业 “${job.name}” 运行情况报告表</th></tr>");
		sb.append("<tr><td width=\"33%\">作业ID:</td><td>${job.id}</td></tr>");
		sb.append("<tr><td>作业名称:</td><td>${job.name}</td></tr>");
		sb.append("<tr><td>作业描述:</td><td>${job.desc}</td></tr>");
		sb.append("<tr><td>用户:</td><td>${job.user}</td></tr>");
		sb.append("<tr><td>环境目录:</td><td>${job.workdir}</td></tr>");
		sb.append("<tr><td>运行命令:</td><td>${job.command}</td></tr>");
		sb.append("<tr><td>提交时间:</td><td>${job.createtime}</td></tr>");
		sb.append("<tr><td>运行时间:</td><td>${job.executetime}</td></tr>");
		sb.append("<tr><td>结束时间:</td><td>${job.finishtime}</td></tr>");
		sb.append("<tr><td>运行结果:</td><td>${job.exitvalue==0?'成功':'失败'}</td></tr>");
		sb.append("<tr><td>状态:</td><td>${job.status}</td></tr>");
		sb.append("<tr><td colspan=\"2\">运行输出:</td></tr>");
		sb.append("<tr><td colspan=\"2\"><pre>${job.stdout}</pre></td></tr>");
		sb.append("<tr><td colspan=\"2\">错误输出:</td></tr>");
		sb.append("<tr><td colspan=\"2\"><pre>${job.stderr}</pre></td></tr>");
		sb.append("</table>");
		

		try {
			Template template;
			String html;
			template = new GStringTemplateEngine()
			.createTemplate(sb.toString().replaceAll("\\\\", "\\\\\\\\"));

			html = template.make(map).toString();
			
			String subject = String.format("%d号作业运行情况报告", this.job.id);
			
			Mail mail = new Mail(subject, html);
			
			mail.addTo(this.job.user);
			
			String cc = Config.getMap().get("jobserver.email.cc");
			
			if (cc != null) {
				String[] lists = cc.split("\\s+");

				for (String ccc : lists) {
					mail.addCc(ccc);
				}
			}
			
			MailThreadPoolFactory.tpm().addTask(mail);
			
			
		} catch (CompilationFailedException e) {
			logger.error("整理邮件内容发送错误：" + e.getMessage());
		} catch (ClassNotFoundException e) {
			logger.error("整理邮件内容发送错误：" + e.getMessage());
		} catch (IOException e) {
			logger.error("整理邮件内容发送错误：" + e.getMessage());
		}
	}

	private void proccess() throws Exception {
		
		this.job.executetime = (System.currentTimeMillis() / 1000);
		this.job.status ++;
		this.job.save(this.session);
		
		logger.info(String.format("作业:\"%s\"于%s开始运行。", this.job.name, DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss")));
		logger.info("环境目录：" + this.job.workdir);
		logger.info("运行命令为：" + this.job.command);
		
		try {
			this.process = Runtime.getRuntime().exec(this.job.command, EnvHelper.getEnv(), this.workdir);		
			this.process.waitFor();
			
			this.job.stdout = IOUtils.toString(this.process.getInputStream());
			this.job.stderr = IOUtils.toString(this.process.getErrorStream());
			this.job.exitvalue = this.process.exitValue();
		} catch(Exception e) {
			this.job.status = -1;
			logger.fatal("作业运行中发生致命错误：" + e.getMessage());
		} finally {
			this.job.status ++;
			this.job.finishtime = (System.currentTimeMillis() / 1000);
			this.job.save(this.session);
			logger.info("作业：" + this.job.name + "于"
					+ DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss")
					+ "运行完毕。");
		}
		
	}

	private void runBefore() throws CompilationFailedException, ClassNotFoundException, IOException {
		this.job.workdir = String.format("%s%s%d", 
				Config.get("jobserver.path.runtimes.dir"),
				System.getProperty("file.separator"),
				this.job.id);
		
		this.workdir = new File(this.job.workdir);
		this.job.workdir = this.workdir.getAbsolutePath();
		
		if(!this.workdir.exists()) {
			this.workdir.mkdirs();
		}
		
		if(!this.workdir.isDirectory()) {
			throw new IOException("创建目录失败:" + this.job.workdir);
		}
			
		
		map.put("conf", Config.getMap());
		map.put("job", this.job);		

		Template template = new GStringTemplateEngine()
				.createTemplate(this.job.command.replaceAll("\\\\", "\\\\\\\\"));
		this.job.command = template.make(map).toString();
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		HibernateSessionFactory.build(new File("hibernate.cfg.xml"));
		
		Config.put("jobserver.path.baseworkdir.realpath", System.getProperty("user.dir") + System.getProperty("file.separator") + "runtimes");
		Config.put("jobserver.email.smtp", "smtp.gmail.com");
		Config.put("jobserver.email.username", "lifei@kuxun.com");
		Config.put("jobserver.email.password", "lifei@kuxun.cn");
		Config.put("jobserver.email.ssl", "true");
		Config.put("jobserver.email.cc", "lifei.job@gmail.com lifei.kx@qq.com");
		
		XCrawler.initLogger("");
		JobThread job = new JobThread("lifei@kuxun.com", "测试用程序", "", "\"${System.getProperty('user.dir')}\\test.bat\"");
		job.run();

	}

}
