package cn.dceast.platform.gateway.auth.data.entity;

import java.util.ArrayList;
import java.util.List;

public class RequestSaaSBean {
	private String channel;
	private String areaCode;
	private String resourceId;
	private String userId;
	private String account;
	private String username;
	private List<RequestSaaSRuleList> list = new ArrayList<RequestSaaSRuleList>();
	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	

	public List<RequestSaaSRuleList> getList() {
		return list;
	}

	public void setList(List<RequestSaaSRuleList> list) {
		this.list = list;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getAreaCode() {
		return areaCode;
	}

	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}

	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

}
