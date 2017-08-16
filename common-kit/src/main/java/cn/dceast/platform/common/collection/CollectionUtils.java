package cn.dceast.platform.common.collection;

import java.util.Collection;

/**
 * 集合工具包
 * @author 未来
 *
 */
public class CollectionUtils {
	/**
	 * 集合数据类型是否为空
	 * @param c
	 * @return
	 * @author ZWL
	 * @date 2014-12-10 下午2:23:39
	 */
	public static boolean isEmpty(Collection<?> c){
		if(c==null || c.size()==0){
			return true;
		}
		
		return false;
	}
}
