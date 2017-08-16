package cn.dceast.platform.gateway.auth.data.entity;

public class 	Api {
	private String appName;
	private String apiName;
	private String url;
	private Integer maxCountOfDay;
	private boolean notAuth;
	
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public String getApiName() {
		return apiName;
	}
	public void setApiName(String apiName) {
		this.apiName = apiName;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public Integer getMaxCountOfDay() {
		return maxCountOfDay;
	}
	public void setMaxCountOfDay(Integer maxCountOfDay) {
		this.maxCountOfDay = maxCountOfDay;
	}
	public boolean isNotAuth() {
		return notAuth;
	}
	public void setNotAuth(boolean notAuth) {
		this.notAuth = notAuth;
	}
	
}
