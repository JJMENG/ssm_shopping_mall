package com.taotao.order.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.taotao.common.pojo.TaotaoResult;
import com.taotao.common.utils.CookieUtils;
import com.taotao.common.utils.JsonUtils;
import com.taotao.pojo.TbUser;
import com.taotao.sso.service.UserService;

public class LoginInterceptor implements HandlerInterceptor {

	@Value("${TOKEN_KEY}")
	private String TOKEN_KEY;
	
	@Value("${SSO_URL}")
	private String SSO_URL;
	
	@Autowired
	private UserService userService;
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		// 从Cookie中取token信息
		String token = CookieUtils.getCookieValue(request,TOKEN_KEY);
		// 如果取不到token，跳转到sso登录页面，需要把当前请求的url作为参数传递给SSO,sso登录成功之后跳转回请求页面
		if(StringUtils.isBlank(token)){
			System.out.println("取不到token");
			// 取当前请求的URL
			String url = request.getRequestURL().toString();
			// 跳转到登录页面
			response.sendRedirect(SSO_URL + "/page/login?url=" + url);
			// 拦截用户请求
			return false;
		}
		// 取到token，调用sso系统的服务判断用户是否登录
		TaotaoResult result = userService.getUserByToken(token);
		if(result.getStatus() != 200){
			System.out.println("取到token");
			// 取当前请求的URL
			String url = request.getRequestURL().toString();
			// 跳转到登录页面
			response.sendRedirect(SSO_URL + "/page/login?url=" + url);
			// 拦截用户请求
			return false;  
		}
		// 如果取到用户信息，放行
		// 将用户信息放入request中
		TbUser user = (TbUser) result.getData();
		request.setAttribute("user",user);
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		// TODO Auto-generated method stub

	}

}
