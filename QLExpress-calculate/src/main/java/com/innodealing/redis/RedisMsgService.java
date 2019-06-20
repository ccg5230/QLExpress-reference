package com.innodealing.redis;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @author stephen.ma
 * @date 2016年6月15日
 * @clasename RedisMsgService.java
 * @decription TODO
 */
@Service
public class RedisMsgService {
	
	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	public void saveMsgWithTimeout(String key, String value, long timeout) {
		redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
	}

	public String getMsgContent(String key) {
		return (String) redisTemplate.opsForValue().get(key);
	}
	
	public Object get(String key) {
		return redisTemplate.opsForValue().get(key);
	}
	
	public void saveMsg(String key, String value) {
		redisTemplate.opsForValue().set(key, value);
	}
	
	public void deleteMsg(String key) {
		redisTemplate.delete(key);
	}

	public Boolean expireAt(String key, Date date) {
		return redisTemplate.expireAt(key, date);
	}
	
}
