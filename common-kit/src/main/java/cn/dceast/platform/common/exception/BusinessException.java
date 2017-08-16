package cn.dceast.platform.common.exception;

public class BusinessException extends RuntimeException {
	
	private String errorCode;
	private String message;

	/**
	 * 
	 */
	private static final long serialVersionUID = -186340342461530958L;

	public BusinessException() {
		super();
	}

	public BusinessException(String arg0, Throwable arg1, boolean arg2,
			boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public BusinessException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public BusinessException(String arg0) {
		super(arg0);
	}

	public BusinessException(Throwable arg0) {
		super(arg0);
	}
	
	public BusinessException(String errorCode,String message){
		super(message);
		
		this.errorCode=errorCode;
		this.message=message;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public String getMessage() {
		return message;
	}

}
