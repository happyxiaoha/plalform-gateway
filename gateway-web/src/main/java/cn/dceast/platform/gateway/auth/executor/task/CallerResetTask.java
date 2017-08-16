package cn.dceast.platform.gateway.auth.executor.task;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import cn.dceast.platform.gateway.auth.util.InnerConstants;
import cn.dceast.platform.gateway.auth.util.MongoDBUtil;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.QueryOperators;

@Component
@Configurable
@EnableScheduling
public class CallerResetTask{
	
	@Scheduled(cron = "00 00 00 * * ?")
	public void execute() {
		
		BasicDBObject query=new BasicDBObject();
		query.append("countOfDay", new BasicDBObject(QueryOperators.NE, 0));
		
		BasicDBObject field=new BasicDBObject();
		field.append("$set", new BasicDBObject("countOfDay", 0));
		
		DBCursor cursor = MongoDBUtil.getColl(InnerConstants.COLL_API_DAILY_REQUEST_STATICS).find(query);
		while(cursor.hasNext()){
			DBObject db = cursor.next();
			MongoDBUtil.getColl("api_daily_request_statics").update(db, field);
		}
	}

}
