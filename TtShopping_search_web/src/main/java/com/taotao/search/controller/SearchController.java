package com.taotao.search.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.taotao.common.pojo.SearchItemResult;
import com.taotao.search.service.SearchService;

/**
 * 搜索服务Controller
 * @author 浩瀚
 *
 */
@Controller
public class SearchController {
	
	@Autowired
	private SearchService searchService;
	
	@Value("${SEARCH_RESULT_COUNT}")
	private Integer SEARCH_RESULT_COUNT;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@RequestMapping("/search")
	public String search(@RequestParam("q")String queryString,
			@RequestParam(defaultValue="1")Integer page,Model model){
		// 调用服务
		try {
			// 将查询条件转码
			queryString = new String(queryString.getBytes("iso8859-1"),"utf-8");
			SearchItemResult searchResult = searchService.search(queryString, page, SEARCH_RESULT_COUNT);
			// 向页面回显数据
			logger.info("查询到的结果为："+searchResult.toString());
			model.addAttribute("query", queryString);
			model.addAttribute("totalPages", searchResult.getTotalPages());
			model.addAttribute("itemList", searchResult.getItemList());
			model.addAttribute("page", page);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 返回逻辑视图
		return "search";
	}
}
