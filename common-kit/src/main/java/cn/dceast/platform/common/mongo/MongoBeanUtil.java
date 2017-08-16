package cn.dceast.platform.common.mongo;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * {@link Object} 与 {@link com.mongodb.DBObject} 相互转换工具类
 *
 * Created by hongkai on 2016/2/19.
 */
public class MongoBeanUtil {

    private static final Logger logger = LoggerFactory.getLogger(MongoBeanUtil.class);

    /**
     * {@link Object} 转换成{@link com.mongodb.DBObject}对象
     *
     * @param sourceObject  需要被转换的对象
     * @param ignoreNullFields 是否过滤空字段
     * @param ignoreFields  需要忽略的字段列表
     * @return
     * @throws java.lang.reflect.InvocationTargetException
     * @throws IllegalAccessException
     */
    public static BasicDBObject convertToDBObject(Object sourceObject, boolean ignoreNullFields,  String... ignoreFields) {
        BasicDBObject result = new BasicDBObject();
        Method[] methods = sourceObject.getClass().getMethods();
        List<String> ignoreFiledList = getIgnoreFiledList(ignoreFields);
        for(Method method : methods){
            String methodName = method.getName();
            if(methodName.equals("getClass")){
                continue;
            }
            if(!methodName.startsWith("get")){
                continue;
            }
            String fieldName = getFieldName(methodName);
            if(ignoreFiledList.contains(fieldName)){
                continue;
            }
            Object value;
            try {
                value = method.invoke(sourceObject, (Object[])null);
            } catch (Exception e) {
                logger.error(String.format("get field value failed: %s", fieldName));
                throw new RuntimeException(e);
            }
            if(ignoreNullFields){
                if(null == value){
                    continue;
                }
                if(value instanceof Number && 0 == ((Number)value).intValue()){
                    continue;
                }
            }
            result.put(fieldName, value);
        }
        return result;
    }

    /**
     * {@link com.mongodb.DBObject} 转换成 {@link Object}
     * @param <T>
     *
     * @param targetClass 被转换的类型
     * @param source 源对象
     * @param ignoreFields 忽略的字段
     * @return
     * @throws Exception
     */
    public static <T> T convertToObject(Class<T> targetClass, DBObject source, String... ignoreFields) {
        T result = null;
        try {
            result = targetClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("the specified class object cannot be created");
        }
        Method[] methods = targetClass.getMethods();
        List<String> ignoreFiledList = getIgnoreFiledList(ignoreFields);
        for (Method method : methods) {
            String methodName = method.getName();
            if (!methodName.startsWith("set")) {
                continue;
            }
            String fieldName = getFieldName(methodName);
            if (ignoreFiledList.contains(fieldName)) {
                continue;
            }
            Object value = source.get(fieldName);
            try{
                method.invoke(result, value);
            }catch(Exception e){
                String valueType2String = "";
                String value2String = "";
                fieldName = (fieldName == null ? "" : fieldName);
                if(value != null){
                    valueType2String = value.getClass().toString();
                    value2String = value.toString();
                }
                logger.error(String.format("field convert failed: %s, value is %s, type of value is %s",
                        fieldName, value2String, valueType2String));
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    /**
     * 获取忽略字段的List
     *
     * @param ignoreFields
     * @return
     */
    private static List<String> getIgnoreFiledList(String... ignoreFields){
        if(ignoreFields == null || ignoreFields.length == 0){
            return new ArrayList<String>();
        }
        return Arrays.asList(ignoreFields);
    }

    /**
     * 通过get/set方法名取得字段名
     *
     * @param methodName
     * @return
     */
    private static String getFieldName(String methodName){
        methodName = methodName.substring(3);
        return lowerCaseFirstLetter(methodName);
    }

    /**
     *首字母转小写
     *
     * @param name
     * @return
     */
    private static String lowerCaseFirstLetter(String name) {
        char[] letters = name.toCharArray();
        if (!isUpperCaseLetter(letters[0])) {
            //不是大写字母，不需要转换，直接返回
            return name;
        }
        //65～90为26个大写英文字母，97～122号为26个小写英文字母
        letters[0] += 32;
        return String.valueOf(letters);
    }

    /**
     * 判断字母是否是大写
     *
     * @param letter
     * @return
     */
    private static boolean isUpperCaseLetter(char letter){
        return letter >= 65 && letter <= 90;
    }


}
