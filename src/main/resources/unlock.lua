-- 比较线程中与锁中的标示是否一致
if (redis.call("get", KEYS[1]) == ARGV[1]) then
	-- 如果一致则删除锁
	return redis.call("del", KEYS[1])
else
	-- 不一致则返回0
	return 0
end