package com.digitalchina.custom.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ConfigClient {
	private String host;
	private int port=5091;
	private String userName;
	private String password;
	private String type="md5";
	private String appName;
	private String appVersion;
	private long timeout=6000;
	private static boolean success=false;
 
	private static List<Map<String,String>> rows = null;
	/**
	 * @author hannn
	 * @param userName paas用户名
	 * @param password paas密码
	 * @param password paas密码
	 * @param appName 应用名
	 * @param appVersion 应用版本
	 * 初始化配置中心客户端基本参数
	 * */
	public ConfigClient(String host ,String userName,String password,String appName,String appVersion){
		this.host=host;
		this.port=5091;
		this.userName=userName;
		this.password=password;
		this.appName=appName;
		this.appVersion=appVersion;
	}
	public ConfigClient(){
		
	}
	/**
	 * 获取配置中心配置 初步进行解析
	 * @return
	 */
	private boolean getConfig(){
		if(success){
			return true;
		}
		if(!check()){
			return false;
		}
		int timeUnit=1000;
		int time=0;
		String url=buildUrl();
		String params=buildParams();
		while(!success){
				success=sendRequest(url,params);
				time++;
				if((time*timeUnit)>=timeout){
					System.out.println("connect time out!");
					break;
				}
				try {
					Thread.sleep(timeUnit);
				} catch (InterruptedException e) {
				}
		}
		return true;
	}
	/**
	 * 获取配置中心地址
	 * @return
	 */
	public String getHost() {
		return host;
	}
	/**
	 * 设置配置中心地址
	 * @param host
	 */
	public void setHost(String host) {
		this.host = host;
	}
	/**
	 * 获取配置中心端口
	 * @return
	 */
	public int getPort() {
		return port;
	}
	/**
	 * 设置配置中心端口 默认5091
	 * @return
	 */
	public void setPort(int port) {
		this.port = port;
	}
	/**
	 * 获取配置中心用户名
	 * @return
	 */
	public String getUserName() {
		return userName;
	}
	/**
	 * 设置配置中心用户名
	 * @return
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}
	/**
	 * 获取配置中心密码
	 * @return
	 */
	public String getPassword() {
		return password;
	}
	/**
	 * 设置配置中心密码
	 * @return
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	/**
	 * 获取配置中心密码类型 （md5默认 ,  password）
	 * md5 表示当前密码为md5之后的结果，
	 * password 表示当前密码为明文密码 
	 */
	public String getType() {
		return type;
	}
	/**
	 *	设置密码类型
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 *	获取所需配置应用名称
	 */
	public String getAppName() {
		return appName;
	}
	/**
	 *	设置所需配置应用名称
	 */
	public void setAppName(String appName) {
		this.appName = appName;
	}
	/**
	 *	获取所需配置应用版本名称
	 */
	public String getAppVersion() {
		return appVersion;
	}
	/**
	 *	设置所需配置应用版本名称
	 */
	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}
	private boolean check(){
		if (host==null || "".equals(host)){
			System.out.println("please set the host of configcenter");
			return false;
		}
		if (userName==null || "".equals(userName)){
			System.out.println("please set the userName of configcenter");
			return false;
		}
		if (password==null || "".equals(password)){
			System.out.println("please set the userName of configcenter user:"+userName);
			return false;
		}
		if (appName==null || "".equals(appName)){
			System.out.println("please set the appName of the config you want");
			return false;
		}
		if (appName==null || "".equals(appName)){
			System.out.println("please set the appVersion of the config you want");
			return false;
		}
		return true;
	}
	private String buildUrl(){
		StringBuilder urlB= new StringBuilder("http://");
		urlB.append(host);
		urlB.append(":");
		urlB.append(port);
		urlB.append("/masterl/ws/configs/getConfig");
		return urlB.toString();
	}
	private String buildParams(){
		StringBuilder sbF= new StringBuilder("type=");
		sbF.append(type);
		sbF.append("&userName=");
		sbF.append(userName);
		sbF.append("&password=");
		sbF.append(password);
		sbF.append("&appName=");
		sbF.append(appName);
		sbF.append("&appVersion=");
		sbF.append(appVersion);
		return sbF.toString();
	}
	private boolean sendRequest(String url,String params){
		String temp;
		try {
			temp = WebClient.sendPost(url,params);
		} catch (Exception e) {
			return false;
		}
		Map<String,String> map=JSON.parseObject(temp, new TypeReference<Map<String,String>>(){});
		if(map.get("result")!=null || "false".equals(map.get("result"))){
			String msg=map.get("msg");
			System.out.println("get config error:"+msg+" try again");
			return false;
		}
		String tnum=map.get("total");
		if(tnum ==null || "".equals(tnum)){
			rows= new ArrayList<Map<String,String>>();
			return true;
		}
		int numAll=Integer.valueOf(tnum);
		if(numAll==0){
			rows= new ArrayList<Map<String,String>>();
			return true;
		}
		Object rowObject= map.get("rows");
		if(rowObject ==null){
			System.out.println("no content");
			return false;
		}
		
		try{
			rows=JSON.parseObject(rowObject.toString(), new TypeReference<List<Map<String,String>>>(){});
		}catch(Exception e){
			System.out.println("parse json error");
			return false;
		}
		return true;
	}
	public void clear(){
		success=false;
	}
	public Properties getPropertyConfig(){
		Properties properties= new Properties();
		getConfig();
		if(success){
			for(Map<String,String> unit:rows){
				String key=unit.get("key");
				if(key==null || "".equals(key)){
					continue;
				}
				String value=unit.get("value");
				if(value==null){
					value="";
				}
				properties.setProperty(key, value);
			}
		}
		return properties;
	}
	

	
	public List<Map<String,String>> getMapConfig(){
		getConfig();
		return rows;
	}
	
}
