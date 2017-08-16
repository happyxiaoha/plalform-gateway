ngx.log(ngx.INFO, "ngx.var.cacheCode===============", ngx.var.cacheCode)
ngx.log(ngx.INFO, "ngx.var.http_useraccount=======", ngx.var.http_useraccount)
--短信接口or支付接口需向业务端传入user-account头字段
if (ngx.var.http_useraccount ~= '' and ngx.var.http_useraccount ~= nil) then
	ngx.req.set_header("user-account", ngx.var.http_useraccount)
end
--000000代表缓存命中，则请求cachebackend
if ngx.var.cacheCode == '000000' then
	ngx.var.testapi = 'cachebackend/gateway-web-1.8.0/cacheFilter';
	ngx.req.set_header("cacheUrl",ngx.var.http_cacheUrl);
end

--为000002时，需向业务系统发送requestDes
if ngx.var.cacheCode == '000002' then
	ngx.req.set_header("requestDes",ngx.var.http_requestDes);
end