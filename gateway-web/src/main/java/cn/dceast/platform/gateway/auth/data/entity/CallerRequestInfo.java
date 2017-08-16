package cn.dceast.platform.gateway.auth.data.entity;

import java.util.Date;


/**
 * api 调用信息
 *
 * Created by hongkai on 2016/2/15.
 */
public class CallerRequestInfo extends LogBean{
    private String uri;
    private String callerName;
    private Date callTime;
    private String callerIP;
    private String resourceType;
    private String resourceId;

    
    public CallerRequestInfo() {
		super();
	}

	public CallerRequestInfo(String uri, String callerName, Date callTime, String callerIP) {
        this.uri = uri;
        this.callerName = callerName;
        this.callTime = callTime;
        this.callerIP = callerIP;
    }

    public String getUri(){
        return uri;
    }

    public String getCallerName() {
        return callerName;
    }

    public Date getCallTime() {
        return callTime;
    }

    public String getCallerIP() {
        return callerIP;
    }

    public void setUri(String uri){
        this.uri = uri;
    }

    public void setCallerName(String callerName) {
        this.callerName = callerName;
    }

    public void setCallTime(Date callTime) {
        this.callTime = callTime;
    }

    public void setCallerIP(String callerIP) {
        this.callerIP = callerIP;
    }

    public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}
	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	@Override
    public String toString() {
        return "CallerRequestInfo{" +
                ", uri='" + uri + '\'' +
                ", callerName='" + callerName + '\'' +
                ", callTime=" + callTime +
                ", callerIP='" + callerIP + '\'' +
                '}';
    }
}
