package com.taotao.sso.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.taotao.common.pojo.TaotaoResult;
import com.taotao.common.utils.CookieUtils;
import com.taotao.common.utils.JsonUtils;
import com.taotao.pojo.TbUser;
import com.taotao.sso.service.UserService;

/**
 * 用户处理Controller
 * @author 浩瀚
 *
 */
@Controller
public class UserController {
	
	@Autowired
	private UserService userService;
	
	@Value("${TOKEN_KEY}")
	private String TOKEN_KEY;
	
	/**
	 * 验证用户信息
	 * @param param
	 * @param type
	 * @return
	 */
	@RequestMapping("/user/check/{param}/{type}")
	@ResponseBody
	public TaotaoResult checkUserData(@PathVariable(value="param")String param,
			@PathVariable(value="type")int type){
		
		TaotaoResult result = userService.checkData(param, type);
		
		return result;
	}
	/**
	 * 用户注册
	 * @param user
	 * @return
	 */
	@RequestMapping(value="/user/register",method=RequestMethod.POST)
	@ResponseBody
	public TaotaoResult registUser(TbUser user){
		TaotaoResult result = userService.registUser(user);
		return result;
	}
	
	@RequestMapping(value="/user/login", method=RequestMethod.POST)
	@ResponseBody
	public TaotaoResult login(String username, String password, 
			HttpServletResponse response, HttpServletRequest request) {
		TaotaoResult result = userService.login(username, password);
		System.out.println(result.toString());
		//登录成功后写cookie
		if (result.getStatus() == 200) {
			//把token写入cookie
			CookieUtils.setCookie(request, response, TOKEN_KEY, result.getData().toString());
			System.out.println("写入成功");
		}
		return result;
	}
	/*@RequestMapping(value="/user/token/{token}", method=RequestMethod.GET, 
		//指定返回响应数据的content-type
		produces=MediaType.APPLICATION_JSON_UTF8_VALUE)
		@ResponseBody
		public String getUserByToken(@PathVariable String token, String callback) {
		TaotaoResult result = userService.getUserByToken(token);
		//判断是否为jsonp请求
		if (StringUtils.isNotBlank(callback)) {
			return callback + "(" + JsonUtils.objectToJson(result) + ");";
		}
		return JsonUtils.objectToJson(result);
	}*/
	@RequestMapping(value="/user/token/{token}", method=RequestMethod.GET)
	@ResponseBody
	public Object getUserByToken(@PathVariable String token, String callback) {
		TaotaoResult result = userService.getUserByToken(token);
		//判断是否为jsonp请求
		if (StringUtils.isNotBlank(callback)) {
			MappingJacksonValue mappingJacksonValue = new MappingJacksonValue(result);
			//设置回调方法
			mappingJacksonValue.setJsonpFunction(callback);
			return mappingJacksonValue;
		}
		return result;
	}
	
}
