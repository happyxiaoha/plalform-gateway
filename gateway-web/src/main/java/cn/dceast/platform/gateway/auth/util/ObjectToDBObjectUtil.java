package cn.dceast.platform.gateway.auth.util;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 对象转换成{@link com.mongodb.DBObject}对象工具类
 *
 * Created by hongkai on 2016/2/19.
 */
public class ObjectToDBObjectUtil {

	/**
	 * 转换成{@link com.mongodb.DBObject}对象
	 *
	 * @param sourceObject
	 *            待转换对象
	 * @param ignoreFields
	 *            需要忽略的字段列表
	 * @return
	 * @throws java.lang.reflect.InvocationTargetException
	 * @throws IllegalAccessException
	 */
	public static DBObject convertToDBObject(Object sourceObject, String... ignoreFields)
			throws InvocationTargetException, IllegalAccessException {
		DBObject result = new BasicDBObject();
		Method[] methods = sourceObject.getClass().getMethods();
		List<String> ignoreFiledList = getIgnoreFiledList(ignoreFields);
		for (Method method : methods) {
			String methodName = method.getName();
			if (methodName.equals("getClass")) {
				continue;
			}
			if (!methodName.startsWith("get")) {
				continue;
			}
			String fieldName = getFieldName(methodName);
			if (ignoreFiledList.contains(fieldName)) {
				continue;
			}
			Object value = method.invoke(sourceObject, (Object[]) null);
			result.put(fieldName, value);
		}
		return result;
	}

	/**
	 * 获取忽略字段的List
	 *
	 * @param ignoreFields
	 * @return
	 */
	private static List<String> getIgnoreFiledList(String... ignoreFields) {
		if (ignoreFields == null || ignoreFields.length == 0) {
			return new ArrayList<String>();
		}
		return Arrays.asList(ignoreFields);
	}

	/**
	 * 通过get方法名取得字段名
	 *
	 * @param methodName
	 * @return
	 */
	private static String getFieldName(String methodName) {
		methodName = methodName.substring(3);
		return lowerCaseFirstLetter(methodName);
	}

	/**
	 * 首字母转小写
	 *
	 * @param name
	 * @return
	 */
	private static String lowerCaseFirstLetter(String name) {
		char[] letters = name.toCharArray();
		if (!isUpperCaseLetter(letters[0])) {
			// 不是大写字母，不需要转换，直接返回
			return name;
		}
		// 65～90为26个大写英文字母，97～122号为26个小写英文字母
		letters[0] += 32;
		return String.valueOf(letters);
	}

	/**
	 * 判断字母是否是大写
	 *
	 * @param letter
	 * @return
	 */
	private static boolean isUpperCaseLetter(char letter) {
		return letter >= 65 && letter <= 90;
	}

}
