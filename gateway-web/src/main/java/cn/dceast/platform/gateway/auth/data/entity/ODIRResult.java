package cn.dceast.platform.gateway.auth.data.entity;


/**
 * /getorderDetailIdCount接口返回成功实体
 * @author liycq
 *
 */
public class ODIRResult{

	private String error = null;
	private String resourceId = null;
	private int orderDetailId = 0;
	private int currentUsed = 0;
	private String userId = null;
	private String areacode = null;
	private String resourceType = null;
	private String number = null;
	private String left = null;
	private String timeLeft = null;

	
	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getTimeLeft() {
		return timeLeft;
	}

	public void setTimeLeft(String timeLeft) {
		this.timeLeft = timeLeft;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getLeft() {
		return left;
	}

	public void setLeft(String left) {
		this.left = left;
	}

	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	public int getCurrentUsed() {
		return currentUsed;
	}

	public void setCurrentUsed(int currentUsed) {
		this.currentUsed = currentUsed;
	}

	public int getOrderDetailId() {
		return orderDetailId;
	}

	public void setOrderDetailId(int orderDetailId) {
		this.orderDetailId = orderDetailId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getAreacode() {
		return areacode;
	}

	public void setAreacode(String areacode) {
		this.areacode = areacode;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}


	
}
