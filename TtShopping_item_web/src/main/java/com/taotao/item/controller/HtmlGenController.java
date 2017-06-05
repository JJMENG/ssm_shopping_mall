package com.taotao.item.controller;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import freemarker.template.Configuration;
import freemarker.template.Template;
/**
 * 网页静态化处理Controller
 * @author 浩瀚
 *
 */
@Controller
public class HtmlGenController {
	
	@Autowired
	private FreeMarkerConfigurer freeMarkerConfigurer;
	
	@RequestMapping("/genHtml")
	@ResponseBody
	public String genHtml() throws Exception{
		// 生成静态页面
		Configuration configuration = freeMarkerConfigurer.getConfiguration();
		Template template = configuration.getTemplate("hello.ftl");
		// 创建数据集
		Map data = new HashMap<>();
		data.put("hello", "hello freemarker");
		// 创建Writter对象
		Writer out = new FileWriter(new File("D:/out/test.html"));
		template.process(data, out);
		// 关闭流
		out.close();
		// 返回结果
		return "OK";
	}
	
}
