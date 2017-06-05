package com.taotao.sso.serviceImpl;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import com.taotao.common.pojo.TaotaoResult;
import com.taotao.common.utils.JsonUtils;
import com.taotao.jedis.JedisClient;
import com.taotao.mapper.TbUserMapper;
import com.taotao.pojo.TbUser;
import com.taotao.pojo.TbUserExample;
import com.taotao.pojo.TbUserExample.Criteria;
import com.taotao.sso.service.UserService;

/**
 * 客户服务的Service
 * @author 浩瀚
 *
 */
@Service
public class UserServiceImpl implements UserService {
	
	@Autowired
	private TbUserMapper tbUserMapper;
	
	@Autowired
	private JedisClient jedisClient;
	
	// redis中用户的Session
	@Value("${USER_SESSION}")
	private String User_Session;
	
	// redis的过期时间
	@Value("${SESSION_EXPIRE}")
	private Integer SESSION_EXPIRE;
	
	@Override
	public TaotaoResult checkData(String data, int type) {
		
		TbUserExample userExample = new TbUserExample();
		// 设置查询条件
		Criteria criteria = userExample.createCriteria();
		// 1 -- 判断username是否可用  2 -- 判断phone是否可用 3 -- 判断email是否可用
		if(type == 1){
			criteria.andUsernameEqualTo(data);
		}else if(type == 2){
			criteria.andPhoneEqualTo(data);
		}else if(type == 3){
			criteria.andEmailEqualTo(data);
		}else {
			return TaotaoResult.build(400, "非法数据");
		}
		// 执行查询
		List<TbUser> example = tbUserMapper.selectByExample(userExample);
		// 返回结果
		if(example != null && example.size() > 0){
			// 查询到数据，返回false
			return TaotaoResult.ok(false);
		}
		// 数据可用
		return TaotaoResult.ok(true);
	}

	@Override
	public TaotaoResult registUser(TbUser user) {
		// 验证数据有效性
		if(StringUtils.isBlank(user.getUsername())){
			return TaotaoResult.build(400, "用户名为空");
		}
		// 判断用户名是否重复
		TaotaoResult checkData = this.checkData(user.getUsername(), 1);
		if(!(boolean)checkData.getData()){
			return TaotaoResult.build(400, "用户名重复");
		}
		// 判断密码是否为空
		if(StringUtils.isBlank(user.getPassword())){
			return TaotaoResult.build(400, "密码为空");  
		}
		// 如果电话号码不为空 判断电话信息是否重复
		if(StringUtils.isBlank(user.getPhone())){
			if(!(boolean)checkData.getData()){
				return TaotaoResult.build(400, "电话号码重复");
			}
		}
		// 判断邮箱信息是否重复
		if(StringUtils.isBlank(user.getEmail())){
			if(!(boolean)checkData.getData()){
				return TaotaoResult.build(400, "邮箱信息重复");
			}
		}
		// 补全属性
		user.setCreated(new Date());
		user.setUpdated(new Date());
		// 密码md5加密
		String md5Password = DigestUtils.md5DigestAsHex(user.getPassword().getBytes());
		user.setPassword(md5Password);
		// 存入数据库
		tbUserMapper.insert(user);
		// 返回结果
		return TaotaoResult.ok();
	}

	@Override
	public TaotaoResult login(String username, String password) {
		// 判断用户名和密码是否正确
		TbUserExample example = new TbUserExample();
		Criteria criteria = example.createCriteria();
		// 封装查询条件
		criteria.andUsernameEqualTo(username);
		// 执行查询
		List<TbUser> resultList = tbUserMapper.selectByExample(example);
		if(resultList == null || resultList.size() == 0){
			// 返回登录失败
			return TaotaoResult.build(400, "用户名错误");
		}
		TbUser tbUser = resultList.get(0);
		// 密码要进行md5加密然后在校验
		if(!DigestUtils.md5DigestAsHex(password.getBytes())
				.equals(tbUser.getPassword())){
			// 密码错误，返回登录失败
			return TaotaoResult.build(400, "密码错误");
		}
		// 生成token，使用uuid
		String token = UUID.randomUUID().toString();
		// 将密码设为NULL
		tbUser.setPassword(null);
		// 把用户信息保存到redis，key是token,value是用户信息
		jedisClient.set(User_Session + ":" +token, JsonUtils.objectToJson(tbUser));
		// 设置key的过期时间
		jedisClient.expire(User_Session + ":" +token, SESSION_EXPIRE);
		// 返回登录成功，将token返回
		return TaotaoResult.ok(token);
	}

	@Override
	public TaotaoResult getUserByToken(String token) {
		// 从Redis中查询数据
		String json = jedisClient.get(User_Session + ":" +token);
		if(StringUtils.isBlank(json)){
			return TaotaoResult.build(400, "用户登录已经过期");
		}
		// 重置Session的过期时间
		jedisClient.expire(User_Session + ":" +token, SESSION_EXPIRE);
		// 把JSON转换成User对象
		TbUser user = JsonUtils.jsonToPojo(json, TbUser.class);
		return TaotaoResult.ok(user);
		//return TaotaoResult.ok(json);
	}

}
