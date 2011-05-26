package lifei.jobserver.apps;

import java.io.File;

import lifei.jobserver.config.Config;
import lifei.jobserver.factory.ConfigurationFactory;
import lifei.jobserver.factory.HibernateSessionFactory;
import lifei.jobserver.factory.JobThreadPoolFactory;
import lifei.jobserver.factory.MailThreadPoolFactory;
import lifei.jobserver.thrift.models.JobService;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TBinaryProtocol.Factory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;

public class JobServer {
	
	private static Logger logger = Logger.getLogger("JobServer");
	
	private void start() {
		try {
			int port = Integer.parseInt(Config.get("jobserver.port"));
			TServerSocket serverTransport = new TServerSocket(port);
			JobService.Processor processor = new JobService.Processor(
					new JobServiceImpl());
			Factory protFactory = new TBinaryProtocol.Factory(true, true);
			TServer server = new TThreadPoolServer(processor, serverTransport,
					protFactory);
			logger.info("启动作业服务器，端口号：" + Config.get("jobserver.port"));
			server.serve();
		} catch (TTransportException e) {
			logger.error("启动作业服务器启动失败：" + e.getMessage());
			e.printStackTrace(System.err);
		} catch (Exception e) {
			logger.error("启动作业服务器启动失败：" + e.getMessage());
			e.printStackTrace(System.err);
		}
	}

	/**
	 * @param args
	 */
	@SuppressWarnings("static-access")
	public static void main(String[] args) {
		Options options = new Options();
		
		Option config = OptionBuilder.withArgName("config file").withLongOpt("config-file").hasArg()
				.withDescription("Server配置文件").create("c");
		
		Option help = OptionBuilder.withArgName("help").withLongOpt("help").hasArg(false)
		.withDescription("显示帮助").create("h");
		
		Option logging = OptionBuilder.withArgName("log file").withLongOpt("logging-file").hasArg()
		.withDescription("日志配置文件").create("l");


		options.addOption( config );
		options.addOption( help );
		options.addOption( logging );
		
		System.out.println("解析命令行");
		
	    CommandLineParser parser = new PosixParser();
	    CommandLine line = null;
	    try {
	        line = parser.parse( options, args );
	    }
	    catch( ParseException exp ) {
	        // oops, something went wrong
	        System.err.println( "无法解析参数:" + exp.getMessage() );
	        System.exit(-1);
	    }
	    

		if(line.hasOption("h")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "jobserver [options] [target [target2 [target3] ...]]", options );
			return;
		}
		
		System.out.println("加载日志配置");
		String file = "conf/log4j.xml";

		if(line.hasOption("l")) {
			file = line.getOptionValue("l");
		}
		try {
			if(file != null) {
				java.io.File f = new java.io.File(file);
				if (f.exists()) {
					org.apache.log4j.xml.DOMConfigurator.configure(file);
					logger.info("加载Xml日志配置：" + file);
				} else {
					org.apache.log4j.BasicConfigurator.configure();
					logger.info("加载默认日志配置");
				}
	    	} else {
				org.apache.log4j.BasicConfigurator.configure();
				logger.info("加载默认日志配置");	    		
	    	}
		} catch(Exception e) {
			System.err.println( "读取日志配置文件出错:" + e.getMessage() );
	        System.exit(-1);
		}
		
		System.out.println("加载配置");
		String configfile = "conf/jobserver.properties";
		if(line.hasOption('c')) {
			configfile = line.getOptionValue("c");
		}
		
		try {
			ConfigurationFactory.build(configfile);
		} catch (ConfigurationException e) {
			System.err.println( "读取JobServer配置文件出错:" + e.getMessage() );
	        System.exit(-1);
		}
		
		System.out.println("初始化Hibernate");
		Config.build(ConfigurationFactory.getConfig());
		
		File cfgfile = new File(Config.get("hibernate.config.file"));
		if(cfgfile.exists() && cfgfile.isFile()) {
			HibernateSessionFactory.build(cfgfile);
			logger.info("加载Hibernate配置：" + cfgfile.getPath());
		} else {
			System.err.println( "Hibernate配置文件出错: 指定的文件不存在。" + Config.get("hibernate.config.file"));
	        System.exit(-1);			
		}
		
		
		System.out.println("初始化邮件池");
		try {
			String email = Config.get("jobserver.email");
		
			// 启用邮件提醒功能
			if(email != null && email.equals("true")) {
				int core_pool_size = Integer.parseInt(Config.get("jobserver.mailthreadpool.core_pool_size"));
				int max_pool_size = Integer.parseInt(Config.get("jobserver.mailthreadpool.max_pool_size"));
				long keep_alive_time = Integer.parseInt(Config.get("jobserver.mailthreadpool.keep_alive_time"));
				int work_queue_size = Integer.parseInt(Config.get("jobserver.mailthreadpool.work_queue_size"));
				
				MailThreadPoolFactory.build(core_pool_size, max_pool_size, keep_alive_time, work_queue_size);
				
				logger.info(String.format("初始化邮件池 - 线程池核心数:%d, 线程池最大数:%d, 超时时间:%d秒, 压栈队列大小：%d。", 
						core_pool_size, max_pool_size, keep_alive_time, work_queue_size));
			}
		} catch(Exception e) {
			System.err.println( "初始化邮件池失败，配置不正确。");
	        System.exit(-1);			
		}
		
		System.out.println("初始化作业池");
		try {
			int core_pool_size = Integer.parseInt(Config.get("jobserver.jobthreadpool.core_pool_size"));
			int max_pool_size = Integer.parseInt(Config.get("jobserver.jobthreadpool.max_pool_size"));
			long keep_alive_time = Integer.parseInt(Config.get("jobserver.jobthreadpool.keep_alive_time"));
			int work_queue_size = Integer.parseInt(Config.get("jobserver.jobthreadpool.work_queue_size"));

			JobThreadPoolFactory.build(core_pool_size, max_pool_size, keep_alive_time, work_queue_size);
			logger.info(String.format("初始化作业池: %d - %d - %d - %d。", 
					core_pool_size, max_pool_size, keep_alive_time, work_queue_size));
		} catch(Exception e) {
			System.err.println( "初始化作业池失败。" );
	        System.exit(-1);
		}

		JobServer service = new JobServer();
		service.start();
	}

}
