package com.taotao.jedis;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TestJedisSpring {

	@Test
	public void testJedisClientPool() throws Exception {
		//��ʼ��Spring����
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring/applicationContext-jedis.xml");
		//�������л��JedisClient����
		JedisClient jedisClient = applicationContext.getBean(JedisClient.class);
		//ʹ��JedisClient����redis
		jedisClient.set("jedisclient", "mytest");
		String result = jedisClient.get("jedisclient");
		System.out.println(result);
	}
}
