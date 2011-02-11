package lifei.jobserver.helper;

public final class LoggingHelper {

	private LoggingHelper() {
		// TODO Auto-generated constructor stub
	}
	
	/** 配置日志文件 */
	public static void initLogger(String file) {
    	if(file != null) {
			java.io.File f = new java.io.File(file);
			if (f.exists()) {
				// read the logging properties from configuration file
				org.apache.log4j.xml.DOMConfigurator.configure(file);
				return;
			} 
    	}
		org.apache.log4j.BasicConfigurator.configure();
	}
}
