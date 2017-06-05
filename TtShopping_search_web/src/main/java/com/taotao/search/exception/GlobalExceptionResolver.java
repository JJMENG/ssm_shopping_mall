package com.taotao.search.exception;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

public class GlobalExceptionResolver implements HandlerExceptionResolver{
	
	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionResolver.class);
	
	@Override
	public ModelAndView resolveException(HttpServletRequest req, HttpServletResponse res,
					Object handler,Exception exception) {
		
		// 控制台打印异常
		exception.printStackTrace();
		// 像日志文件中写入异常
		logger.error("系统发生异常",exception);
		// 发邮件
		// 发短信
		// 展示错误页面
		ModelAndView model = new ModelAndView();
		model.addObject("message", "您的电脑有问题，请稍后重试");
		model.setViewName("error/exception");
		return model;
	}

}
