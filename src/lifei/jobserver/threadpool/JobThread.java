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
			logger.fatal("致命错误@run：" + e.getMessage());
		} finally {
			try {
				this.runAfter();
			} catch(Exception e) {
				logger.fatal("致命错误@runAfter：" + e.getMessage());
			}
		}

	}

	private void runAfter() {
		this.session.close();
		
		if(!Config.get("jobserver.email").equals("true")) {
			return;
		}
		
		// 发送邮件
		
		StringBuffer sb = new StringBuffer(1000);

		sb.append("<style>th td {padding-left:20px;padding-right:20px;}</style>");
		sb.append("<table border=\"1\" width=\"80%\">");
		sb.append("<tr><th style=\"height:24px;font-size:14px;padding-left:20px;padding-right:20px;line-height:24px\" colspan=\"2\">${job.id}号作业 “${job.name}” 运行情况报告表</th></tr>");
		sb.append("<tr><td style=\"height:24px;font-size:14px;padding-left:20px;padding-right:20px;line-height:24px\" width=\"33%\">作业ID:</td><td style=\"height:24px;font-size:14px;padding-left:5px;line-height:24px\">${job.id}</td></tr>");
		sb.append("<tr><td style=\"height:24px;font-size:14px;padding-left:20px;padding-right:20px;line-height:24px\">作业名称:</td><td style=\"height:24px;font-size:14px;padding-left:5px;line-height:24px\">${job.name}</td></tr>");
		sb.append("<tr><td style=\"height:24px;font-size:14px;padding-left:20px;padding-right:20px;line-height:24px\">作业描述:</td><td style=\"height:24px;font-size:14px;padding-left:5px;line-height:24px\">${job.desc}</td></tr>");
		sb.append("<tr><td style=\"height:24px;font-size:14px;padding-left:20px;padding-right:20px;line-height:24px\">用户:</td><td style=\"height:24px;font-size:14px;padding-left:5px;line-height:24px\">${job.user}</td></tr>");
		sb.append("<tr><td style=\"height:24px;font-size:14px;padding-left:20px;padding-right:20px;line-height:24px\">环境目录:</td><td style=\"height:24px;font-size:14px;padding-left:5px;line-height:24px\">${job.workdir}</td></tr>");
		sb.append("<tr><td style=\"height:24px;font-size:14px;padding-left:20px;padding-right:20px;line-height:24px\">运行命令:</td><td style=\"height:24px;font-size:14px;padding-left:5px;line-height:24px\">${job.command}</td></tr>");
		sb.append("<tr><td style=\"height:24px;font-size:14px;padding-left:20px;padding-right:20px;line-height:24px\">提交时间:</td><td style=\"height:24px;font-size:14px;padding-left:5px;line-height:24px\">" + DateFormatUtils.format(job.createtime,"yyyy-MM-dd HH:mm:ss") + "</td></tr>");
		sb.append("<tr><td style=\"height:24px;font-size:14px;padding-left:20px;padding-right:20px;line-height:24px\">开始时间:</td><td style=\"height:24px;font-size:14px;padding-left:5px;line-height:24px\">" + DateFormatUtils.format(job.executetime,"yyyy-MM-dd HH:mm:ss") + "</td></tr>");
		sb.append("<tr><td style=\"height:24px;font-size:14px;padding-left:20px;padding-right:20px;line-height:24px\">结束时间:</td><td style=\"height:24px;font-size:14px;padding-left:5px;line-height:24px\">" + DateFormatUtils.format(job.finishtime,"yyyy-MM-dd HH:mm:ss") + "</td></tr>");
		sb.append("<tr><td style=\"height:24px;font-size:14px;padding-left:20px;padding-right:20px;line-height:24px\">运行耗时:</td><td style=\"height:24px;font-size:14px;padding-left:5px;line-height:24px\">" + ((job.finishtime - job.executetime) / 1000)  + "秒</td></tr>");
		sb.append("<tr><td style=\"height:24px;font-size:14px;padding-left:20px;padding-right:20px;line-height:24px\">运行结果:</td><td style=\"height:24px;font-size:14px;padding-left:5px;line-height:24px\">${job.status>-1?'成功':'失败'}</td></tr>");
		sb.append("<tr><td style=\"height:24px;font-size:14px;padding-left:20px;padding-right:20px;line-height:24px\">状态:</td><td style=\"height:24px;font-size:14px;padding-left:5px;line-height:24px\">${job.status}</td></tr>");
		sb.append("<tr><td style=\"height:24px;font-size:14px;padding-left:20px;padding-right:20px;line-height:24px\">错误信息:</td><td style=\"height:24px;font-size:14px;padding-left:5px;line-height:24px\">${job.error}</td></tr>");
		sb.append("<tr><td style=\"height:24px;font-size:14px;padding-left:20px;padding-right:20px;line-height:24px\" colspan=\"2\">运行输出:</td></tr>");
		sb.append("<tr><td style=\"height:24px;font-size:14px;padding-left:20px;padding-right:20px;line-height:24px\" colspan=\"2\"><pre>${job.stdout}</pre></td></tr>");
		sb.append("<tr><td style=\"height:24px;font-size:14px;padding-left:20px;padding-right:20px;line-height:24px\" colspan=\"2\">错误输出:</td></tr>");
		sb.append("<tr><td style=\"height:24px;font-size:14px;padding-left:20px;padding-right:20px;line-height:24px\" colspan=\"2\"><pre>${job.stderr}</pre></td></tr>");
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
			logger.error("整理邮件内容发送错误@runAfter：" + e.getMessage());
		} catch (ClassNotFoundException e) {
			logger.error("整理邮件内容发送错误@runAfter：" + e.getMessage());
		} catch (IOException e) {
			logger.error("整理邮件内容发送错误@runAfter：" + e.getMessage());
		}
	}

	private void proccess() throws Exception {
		
		this.job.executetime = System.currentTimeMillis();
		this.job.status = 1;
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
			this.job.status  = 2;
		} catch(Exception e) {
			this.job.status = -1;
			logger.fatal("致命错误@process：" + e.getMessage());
			this.job.error = e.getMessage();
		} finally {
			this.job.finishtime = System.currentTimeMillis();
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

	}

}
