ngx.log(ngx.INFO, "ngx.var.realHost===============", ngx.var.realHost)
ngx.log(ngx.INFO, "ngx.var.tempAuthorization===============", ngx.var.tempAuthorization)
ngx.log(ngx.INFO, "ngx.var.nginxresourcetype===============", ngx.var.nginxresourcetype)
--serviceUrl为java后端在request头中设置的serviceContext
ngx.log(ngx.INFO, "ngx.var.serviceUrl==================",ngx.var.serviceUrl)

if ngx.var.realHost ~= '' then
	ngx.var.tempHost = ngx.var.realHost
end	
if ngx.var.tempAuthorization ~= '' then
	ngx.var.newAuthorization = ngx.var.tempAuthorization
end

if ngx.var.nginxresourcetype ~= '' then
    --是个微服务
    if ngx.var.serviceUrl ~= '' then
        --此请求为.css,.jpg等
        ngx.var.partofupstream = ngx.var.serviceUrl
    else
        --partofupstream存储微服务名    
    	ngx.var.partofupstream = ngx.var.contexta
	end
end