if ngx.var.cacheCode == '000000' then
						--如果换成命中则直接请求缓存接口，不再做url变换
						ngx.var.realupstream = '';
						--这里设置proxy的header没用，因server中的 proxy_set_header Host $http_tvhost;将覆盖掉如下设置
						--ngx.req.set_header("Host", "127.0.0.1:48081")
						return;
					end

					local redis = require "resty.redis"
					local red = redis:new()
					red:set_timeout(1000) -- 1 sec
					local ok, err = red:connect("172.16.49.77", 6379)
					if not ok then
						ngx.say("failed to connect: ", err)
						return
					end
					
					local res, err = red:auth("123")
					if not res then
						ngx.say("failed to authenticate: ", err)
						return
					end
					red:select(3)
					local res, err = red:get("/"..ngx.var.vhost)
					ngx.log(ngx.INFO, "/"..ngx.var.vhost.."'s value from redis==========", res)
					ngx.log(ngx.INFO, "original ngx.var.realupstream===============", ngx.var.realupstream)
					
					
					-------------------------处理请求中的args ?
					if (res ~= nil and res ~= ngx.null) then
					    --redis返回的value中是否包含"?"
						local result = string.find(res,"?");
						if result ~= nil then
							if ngx.var.args ~= nil then
							    --将网关入口url中的参数拼到trueContext中
								ngx.var.realupstream = res .. "&" .. ngx.var.args	
							else
							    --网关入口url中没有args
								ngx.var.realupstream = res
							end
						else
						    --直接将网关入口url中的args拼到res上
							if ngx.var.args ~= nil then
								ngx.var.realupstream = res .. "?" .. ngx.var.args
							else
								ngx.var.realupstream = res
							end
						end
					end
					-----------------------
					
					
					if ngx.var.secondDomainsecondContext == "service" then
					--放开如下三行注释将出现url中ticket重复的情况:http://ctrip.com/we/home?Allianceid=30613&ticket=f96?ticket=f96
					     --if ngx.var.args ~= nil then
					        --ngx.var.realupstream = ngx.var.realupstream .. "?" .. ngx.var.args
					    --end
					        ngx.log(ngx.INFO, "service final ngx.var.realupstream=============",ngx.var.realupstream)
					else
					    ngx.log(ngx.INFO, "final ngx.var.realupstream===============", ngx.var.realupstream)
					end
					
					local ok, err = red:close()
						if not ok then
							ngx.say("failed to close redis: ", err)
							return
					end