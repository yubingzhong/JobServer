package lifei.jobserver.threadpool;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import lifei.jobserver.config.Config;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.log4j.Logger;

public final class Mail implements Runnable {
	
	private static Logger logger = Logger.getLogger("Mail");
	
	private Set<String> to = new HashSet<String>();
	
	private Set<String> cc = new HashSet<String>();
	
	private String subject = "";
	
	private String msg = "";

	public Mail(String subject, String msg) {
		this.subject = subject;
		this.msg = msg;
	}
	
	public Mail() {}

	@Override
	public void run() {
		
		try {
			Email email = new HtmlEmail();
			email.setHostName(Config.get("jobserver.email.smtp"));
			String ssl = Config.get("jobserver.email.ssl");
			if(ssl != null && ssl.equals("true")) {
				email.setSmtpPort(465);
				email.setSSL(true);
			} else {
				email.setSmtpPort(25);
				email.setSSL(false);
			}
			
			email.setDebug(false);
			
			email.setAuthentication(Config.get("jobserver.email.username"),
					Config.get("jobserver.email.password"));
			
			email.setFrom(Config.get("jobserver.email.username"));
			email.setSubject(this.subject);
			email.setMsg(this.msg);
			
			Iterator<String> iter = this.to.iterator();
			
			do {
				String to = iter.next();
				email.addTo(to);
			} while(iter.hasNext());
			
			iter = this.cc.iterator();
			
			do {
				String cc = iter.next();
				email.addCc(cc);
			} while(iter.hasNext());
			
			email.setCharset("utf-8");
			email.send();

		} catch (NullPointerException e) {
			logger.error("邮件发送时发生了致命错误：" + e.getMessage());
			e.printStackTrace(System.err);
			return;
		} catch (EmailException e) {
			logger.error("邮件发送时发生了致命错误：" + e.getMessage());
			e.printStackTrace(System.err);
			return;
		}
		
		logger.info("邮件发送成功：" + this.subject);
	}
	
	public void addTo(String to) {
		this.to.add(to);
	}
	
	public void addCc(String cc) {
		this.cc.add(cc);
	}
	
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public void setMsg(String msg) {
		this.msg = msg;
	}
}
