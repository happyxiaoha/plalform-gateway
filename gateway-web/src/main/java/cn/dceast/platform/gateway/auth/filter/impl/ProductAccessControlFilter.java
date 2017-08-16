package cn.dceast.platform.gateway.auth.filter.impl;

import cn.dceast.platform.gateway.auth.filter.AuthFilter;
import cn.dceast.platform.gateway.auth.filter.FilterMatcher;
import cn.dceast.platform.gateway.auth.filter.FilterResponseMessage;
import cn.dceast.platform.gateway.auth.util.MongoDBUtil;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Iterator;

/**
 * Created by LeoYee on 2016-08-16.
 */
public class ProductAccessControlFilter extends AuthFilter {
    private static final String COLL_PRODUCT = "product";
    private static final String COLL_USER_PRODUCT = "user_product";

    private static Logger logger = LoggerFactory.getLogger(ProductAccessControlFilter.class);

    @Override
    public boolean doFilter(HttpServletRequest request, HttpServletResponse response) {
        logger.info("Product Access Control Beginning.");
        String callerName = getAppkeyInfo(request).getOwnerName(); //当前调用接口的用户
        String apiUri = getApi(request);//api地址
        String serviceName = getAppName(request);//serviceName
        BasicDBObject query = new BasicDBObject("productApi",new BasicDBObject("$elemMatch",new BasicDBObject("serviceName",serviceName).append("apiUri",apiUri)))
                                        .append("owner",callerName);
        BasicDBObject field = new BasicDBObject("owner",1)
                                        .append("productApi",1)
                                        .append("calcCostType",1)
                                        .append("sumCount",1);

        BasicDBObject product = FilterMatcher.getUserProductInfo(query,field);
        //没有记录时说明用户没有购买该产品
        if(product==null){
            setErrorMessageOfJson(request, response, FilterResponseMessage.CODE_104101);
            logger.error("To "+callerName+":Buy this product first");
            return false;
        }else{
            //先判断产品是否为按次收费，若为按次收费则判断余额是否大于0
            if(("count").equals(product.getString("calcCostType"))){
                //判断当前调用的api是否收费
                BasicDBList apiList = (BasicDBList)product.get("productApi");
                Boolean flag = false;
                for (Iterator<Object> iterator = apiList.iterator(); iterator.hasNext();) {
                    BasicDBObject api = (BasicDBObject) iterator.next();
                    //flag设为当前调用的api的收费性质
                    if(api.getString("apiUri").equals(apiUri)&&api.getString("serviceName").equals(serviceName)){
                        flag = api.getBoolean("free");
                    }
                    //免费api要移除
                    if (api.getBoolean("free", true)) {
                        iterator.remove();
                    }
                }
                if(flag) {
                    return flag;
                }
                //查询余额
                int balance = FilterMatcher.calculateUserProductBalance(product);
                if(balance <= 0){
                    setErrorMessageOfJson(request, response, FilterResponseMessage.CODE_104102);
                    logger.error(String.format("Customer out of Money:%s ",balance));
                    return false;
                }
            }
            return true;

        }
    }

}
