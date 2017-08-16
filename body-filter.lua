--body-filter.lua
local maxlen = 10000
ngx.ctx.buffered = ngx.ctx.buffered or ""
if #ngx.ctx.buffered < maxlen then
    ngx.ctx.buffered = ngx.ctx.buffered .. string.sub(ngx.arg[1], 1, maxlen - #ngx.ctx.buffered)
end
if ngx.arg[2] then
    ngx.var.resp_body = ngx.ctx.buffered
end