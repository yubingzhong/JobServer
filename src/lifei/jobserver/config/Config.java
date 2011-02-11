package lifei.jobserver.config;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

public class Config {
	
	private static Logger logger = Logger.getLogger("Config");
	
	private static Map<String, String> _map = new HashMap<String, String>();

	private Config() {
	}
	
	public static Map<String, String> getMap() {
		return _map;
	}
	
	public static String get(String key) {
		String ret = _map.get(key);
		
		return ret == null ? "":ret;
	}
	
	public static void put(String key, String value) {
		_map.put(key, value);
	}

	public static void build(Configuration config) {
		
		// 路径默认值
		Config.put("jobserver.path.runtimes.dir", 
				System.getProperty("user.dir") + System.getProperty("file.separator") + "runtimes");
		
		// 邮箱设置默认值
		Config.put("jobserver.email", "false");
		Config.put("jobserver.email.smtp", null);
		Config.put("jobserver.email.username", null);
		Config.put("jobserver.email.password", null);
		Config.put("jobserver.email.ssl", "false");
		Config.put("jobserver.email.cc", null);
		
		// 日志默认值
		Config.put("logging.config.file", "conf/logger.xml");
		
		// Hibernet配置文件
		Config.put("hibernate.config.file", "conf/hibernate.cfg.xml");	
		
		// Job线程池		
		Config.put("jobserver.jobthreadpool.core_pool_size", "10");
		Config.put("jobserver.jobthreadpool.max_pool_size",  "15");
		Config.put("jobserver.jobthreadpool.keep_alive_time", "0");
		Config.put("jobserver.jobthreadpool.work_queue_size", "15");
		
		// 邮件线程池
		Config.put("jobserver.mailthreadpool.core_pool_size", "10");
		Config.put("jobserver.mailthreadpool.max_pool_size",  "15");
		Config.put("jobserver.mailthreadpool.keep_alive_time", "0");
		Config.put("jobserver.mailthreadpool.work_queue_size", "15");
		
		// 端口号
		Config.put("jobserver.port", "10086");
		
		@SuppressWarnings("unchecked")
		Iterator<String> iter = config.getKeys();
		
		while(iter.hasNext()) {
			String key = iter.next();
			_map.put(key, config.getString(key));
			logger.info("读取配置参数：" + key + "=" + config.getString(key));
		}		
	}

}
