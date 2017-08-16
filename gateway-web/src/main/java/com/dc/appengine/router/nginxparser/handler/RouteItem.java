package com.dc.appengine.router.nginxparser.handler;


public class RouteItem {
	
	/**
	 * 可支持的协议类型
	 */
	public enum ProtocolType {
		HTTP,TCP,HTTPS
	}
	public enum Opnum {
		ADD,DELETE
	}
	private Opnum op;
	private ProtocolType protocol;//tcp or http
	// ??? inPort与inContext构成upstream块名
	private String inPort;      //8080
	private String inContext;    // test
	private String outPort;     //http://10.126.3.1:9080/test
	private String outIp;
	private String outContext;
	private String weight;
	private String monitorType;
	private String monitorString;
	private String limitConn;
	/**
	 * 是否为Session粘连
	 */
	private boolean isSticky = false;
	/*
	 * https为单向认证or双向认证。false为单向认证。
	 */
	private boolean isTwoway = false;
	
	
	
	
	
	public RouteItem() {
		
	}
	public RouteItem(Opnum op, ProtocolType protocol, String inPort, String inContext, String outPort, String outIp,
			String outContext,String weight,String monitorType,String monitoryString,String limitConn) {
		
		this.op = op;
		this.protocol = protocol;
		this.inPort = inPort;
		this.inContext = inContext;
		this.outPort = outPort;
		this.outIp = outIp;
		this.outContext = outContext;
		this.weight = weight;
		this.monitorType=monitorType;
		this.monitorString=monitoryString;
		this.limitConn=limitConn;
	}

	public Opnum getOp() {
		return op;
	}
	public void setOp(Opnum op) {
		this.op = op;
	}
	public ProtocolType getProtocol() {
		return protocol;
	}
	public void setProtocol(ProtocolType protocol) {
		this.protocol = protocol;
	}
	public String getInPort() {
		return inPort;
	}
	public void setInPort(String inPort) {
		this.inPort = inPort;
	}
	public String getInContext() {
		return inContext;
	}
	public void setInContext(String inContext) {
		this.inContext = inContext;
	}
	public String getOutPort() {
		return outPort;
	}
	public void setOutPort(String outPort) {
		this.outPort = outPort;
	}
	public String getOutIp() {
		return outIp;
	}
	public void setOutIp(String outIp) {
		this.outIp = outIp;
	}
	public String getOutContext() {
		return outContext;
	}
	public void setOutContext(String outContext) {
		this.outContext = outContext;
	}
	public String getWeight() {
		return weight;
	}
	public void setWeight(String weight) {
		this.weight = weight;
	}
	public boolean isSticky() {
		return isSticky;
	}
	public void setSticky(boolean isSticky) {
		this.isSticky = isSticky;
	}
	
	/**
	 * 
	 */
	public boolean isTwoway() {
		return isTwoway;
	}
	
	/**
	 * 
	 */
	public void setIsTwoway(boolean isTwoway) {
		this.isTwoway=isTwoway;
	}	
	
	public void setMonitorType(String monitorType){
		this.monitorType=monitorType;
	}
	
	public String getMonitorType(){
		return monitorType;
	}
	
	public void setMonitorString(String monitorString){
		this.monitorString=monitorString;
	}
	
	public String getMonitorString(){
		return monitorString;
	}
	
	public String getlimitConn(){
		return limitConn;
	}
	public void setlimitConn(String limitConn){
		this.limitConn=limitConn;
	}
	

}
