package cn.dceast.platform.common.string;


public class StringMaskUtil {
	/**
	 * 标准的11位手机号码mask
	 * @param phoneNo
	 * @return
	 */
	public static String maskPhoneNo(String phoneNo){
		if(StringUtil.isEmpty(phoneNo)){
			return "";
		}
		
		String result="";
		try{
			result=phoneNo.substring(0,3)+"****"+phoneNo.substring(7);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return result;
	}	
	
	/**
	 * mail mask
	 * 规则 mask at符号之前的4个字符。不足4个字符，只保留首位字符，剩余字符mask
	 * @param mail
	 * @return
	 */
	public static String maskMail(String mail){
		String name=mail.substring(0,mail.indexOf("@"));
		if(StringUtil.isEmpty(name)){
			return "";
		}
		
		String result="";
		if(name.length()>4){
			result=name.substring(0,name.length()-4)+"****";
		}else{
			result=name.substring(0,1)+StringUtil.repeat('*', name.length()-1);
		}
		
		result=result+mail.substring(mail.indexOf("@"));
		return result;
	}
	
	public static void main(String[] args){
		
		System.out.println(maskMail("@126.com"));
	}
}
