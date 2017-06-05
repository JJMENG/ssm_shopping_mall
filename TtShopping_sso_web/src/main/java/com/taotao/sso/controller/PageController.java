package com.taotao.sso.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 展示登录和注册页面Controller
 * @author 浩瀚
 *
 */
@Controller
public class PageController {
	
	@RequestMapping("/page/login")
	public String showLogin(String url,Model model) throws Exception{
		System.out.println("跳转的地址为："+url);
		model.addAttribute("redirect", url);
		return "login";
	}
	
	@RequestMapping("/page/register")
	public String showRegist() throws Exception{
		return "register";
	}
	
}
