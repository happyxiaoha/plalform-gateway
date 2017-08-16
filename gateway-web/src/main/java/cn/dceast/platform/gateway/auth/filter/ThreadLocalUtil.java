package cn.dceast.platform.gateway.auth.filter;

public class ThreadLocalUtil {

	  private static ThreadLocal<String> threadLocal = new ThreadLocal();

	  public static void setThreadLocalValue(String value) {
		  threadLocal.set(value);
	  }

	  public static String getThreadLocalValue() {
	    return threadLocal.get(); 
	 }
	
}
