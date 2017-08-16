-------------------------------------------------------------
local handler = function (premature,code,message,status,result,flownum,orderDetailId,responseStatus,request_uri,cacheCode,requestDes,resp_body)
    local http = require("resty.http")
    local httpc = http.new()

    httpc:set_timeout(2000)

    local resp, err = httpc:request_uri("http://127.0.0.1:48081", {
            method = "POST",
            path = "/gateway-web-1.8.0/apiCallFailedProcess",
			body=resp_body,
            headers = {
                   ["code"] = code,
                   ["message"] = message,
                   ["status"] = status,
				   ["result"] = result,
				   ["flownum"] = flownum,
				   ["orderDetailId"] = orderDetailId,
				   ["responseStatus"] = responseStatus,
				   ["request_uri"] = request_uri,
				   ["cacheCode"] = cacheCode,
				   ["requestDes"] = requestDes
            }
    })
    if not resp then
        ngx.log(ngx.ERR, "Call gateway failed: "..orderDetailId.." : "..reqUri, err)
        return
    end

    httpc:set_keepalive(60000, 10)
end
-------------------------------------------------------
local handler2 = function (premature,code,message,status,result,flownum,orderDetailId,responseStatus,request_uri,cacheCode,requestDes,resp_body)
    local http = require("resty.http")
    local httpc = http.new()

    httpc:set_timeout(2000)

    local resp, err = httpc:request_uri("http://127.0.0.1:48081", {
            method = "POST",
            path = "/gateway-web-1.8.0/setCacheResource",
			body=resp_body,
            headers = {
                   ["code"] = code,
                   ["message"] = message,
                   ["status"] = status,
				   ["result"] = result,
				   ["flownum"] = flownum,
				   ["orderDetailId"] = orderDetailId,
				   ["responseStatus"] = responseStatus,
				   ["request_uri"] = request_uri,
				   ["cacheCode"] = cacheCode,
				   ["requestDes"] = requestDes
            }
    })
    if not resp then
        ngx.log(ngx.ERR, "Call gateway /setCacheResource failed: "..orderDetailId.." : "..reqUri, err)
        return
    end

    httpc:set_keepalive(60000, 10)
end

-----------------------------------------------------------------------------------------------------------


local cjson = require "cjson"
--业务系统返回代码
local response_status = tonumber(ngx.var.status)
ngx.log(ngx.INFO, "Call gateway status:============ ", response_status)
--业务系统返回body
ngx.log(ngx.INFO,"ngx.var.resp_body============",ngx.var.resp_body)
ngx.log(ngx.INFO,"ngx.var.request_uri===========", ngx.var.request_uri)


--
ngx.log(ngx.INFO,"ngx.var.authflownum===========", ngx.var.authflownum)
--cacheCode为缓存接口返回的Code
ngx.log(ngx.INFO,"ngx.var.cacheCode=============",ngx.var.cacheCode)
ngx.log(ngx.INFO,"ngx.var.requestDes============",ngx.var.requestDes)
--



--如下四条为业务系统的header中值
ngx.log(ngx.INFO,"ngx.var.upstream_http_code===========", ngx.var.upstream_http_code)
ngx.log(ngx.INFO,"ngx.var.upstream_http_message===========", ngx.var.upstream_http_message)
ngx.log(ngx.INFO,"ngx.var.upstream_http_status===========", ngx.var.upstream_http_status)
ngx.log(ngx.INFO,"ngx.var.upstream_http_result===========", ngx.var.upstream_http_result)


--ngx.HTTP_BAD_REQUEST值为400
if (response_status >= ngx.HTTP_BAD_REQUEST) then
	if (ngx.var.upstream_http_code == nil) then
		local ok, err = ngx.timer.at(0, handler, ngx.var.upstream_http_code, ngx.var.upstream_http_message, ngx.var.upstream_http_status,ngx.var.upstream_http_result,ngx.var.authflownum,ngx.var.orderDetailId,ngx.var.status,ngx.var.request_uri,nil,nil,nil)
	else
		local ok, err = ngx.timer.at(0, handler, ngx.var.upstream_http_code, ngx.var.upstream_http_message, ngx.var.upstream_http_status,ngx.var.upstream_http_result,ngx.var.authflownum,ngx.var.orderDetailId,nil,ngx.var.request_uri,nil,nil,nil)
		if not ok then
			ngx.log(ngx.ERR, "failed to create the timer: ", err)
			return
		end
	end
else
--业务系统请求成功
	if (response_status == ngx.HTTP_OK) then
		--不用这种decode方式获取errCode值了，因在body-filter.lua中无法完全取出response body。
		--local data = cjson.decode(ngx.var.resp_body)
		firstIndex,secondIndex = string.find(ngx.var.resp_body,"errCode")
		if(firstIndex == nil or secondIndex == nil) then
			firstIndex,secondIndex = string.find(ngx.var.resp_body,"errcode")
		end
		if(firstIndex == nil or secondIndex == nil) then
			firstIndex,secondIndex = string.find(ngx.var.resp_body,"rtnCode")
		end
		local result_errCode = string.sub(ngx.var.resp_body,firstIndex+10,secondIndex+9)
		ngx.log(ngx.INFO,"result_errCode===========",result_errCode)
		if (result_errCode ~= "000000") then
			--业务系统不正常返回，记录失败日志
			if (ngx.var.upstream_http_code == nil) then
				local ok, err = ngx.timer.at(0, handler, ngx.var.upstream_http_code, ngx.var.upstream_http_message, ngx.var.upstream_http_status,ngx.var.upstream_http_result,ngx.var.authflownum,ngx.var.orderDetailId,ngx.var.status,ngx.var.request_uri,nil,nil,ngx.var.resp_body)
				if not ok then
					ngx.log(ngx.ERR, "failed to create the timer: ", err)
					return
				end
			else
				local ok, err = ngx.timer.at(0, handler, ngx.var.upstream_http_code, ngx.var.upstream_http_message, ngx.var.upstream_http_status,ngx.var.upstream_http_result,ngx.var.authflownum,ngx.var.orderDetailId,nil,ngx.var.request_uri,nil,nil,ngx.var.resp_body)
				if not ok then
					ngx.log(ngx.ERR, "failed to create the timer: ", err)
					return
				end
			end	
		else
			--业务系统正常返回，更新缓存
			local ok, err = ngx.timer.at(0, handler2, ngx.var.upstream_http_code, ngx.var.upstream_http_message, ngx.var.upstream_http_status,ngx.var.upstream_http_result,ngx.var.authflownum,ngx.var.orderDetailId,ngx.var.status,ngx.var.request_uri,ngx.var.cacheCode,ngx.var.requestDes,ngx.var.resp_body)
		end
	end
end