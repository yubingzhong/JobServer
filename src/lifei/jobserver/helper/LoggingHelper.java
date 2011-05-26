package lifei.jobserver.helper;

public final class LoggingHelper {

	private LoggingHelper() {
	}
	
	/** 配置日志文件 */
	public static void initLogger(String file) {
    	if(file != null) {
			java.io.File f = new java.io.File(file);
			if (f.exists()) {
				org.apache.log4j.xml.DOMConfigurator.configure(file);
				return;
			} 
    	}
		org.apache.log4j.BasicConfigurator.configure();
	}
}
