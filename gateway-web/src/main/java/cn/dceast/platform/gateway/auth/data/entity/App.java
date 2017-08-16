package cn.dceast.platform.gateway.auth.data.entity;

public class App {
	private String name;
	private String serType;
	private String url;
	private Integer maxTps;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSerType() {
		return serType;
	}
	public void setSerType(String serType) {
		this.serType = serType;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}

	public Integer getMaxTps() {
		return maxTps;
	}

	public void setMaxTps(Integer maxTps) {
		this.maxTps = maxTps;
	}
}
