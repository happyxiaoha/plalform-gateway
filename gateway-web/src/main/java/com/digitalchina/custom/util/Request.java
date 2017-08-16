package com.digitalchina.custom.util;


public class Request {
	private String url;
	private Object data;
	private int status;
	private String result;
	public Request(){
		
	}
	public Request(String url){
		if(!url.startsWith("http://")){
			url="http://"+url;
		}
		this.url=url;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
}
