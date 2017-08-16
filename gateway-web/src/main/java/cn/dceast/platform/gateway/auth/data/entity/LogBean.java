package cn.dceast.platform.gateway.auth.data.entity;


public class LogBean {

	public String flownum;//流水号
	public String appKey;//Api唯一标识
	public String code;//状态码
	public String message;//消息
	public String status;//状态，success：成功；failed：失败
	public String result;//返回数据 	
	public String areacode;//地区编码
	public long orderDetailId;//订单号
	public String userId;

	public String getFlownum() {
		return flownum;
	}

	public void setFlownum(String flownum) {
		this.flownum = flownum;
	}
	public String getAppKey() {
		return appKey;
	}

	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getAreacode() {
		return areacode;
	}

	public void setAreacode(String areacode) {
		this.areacode = areacode;
	}
	public long getOrderDetailId() {
		return orderDetailId;
	}

	public void setOrderDetailId(long orderDetailId) {
		this.orderDetailId = orderDetailId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
}
