package com.taotao.order.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.taotao.common.pojo.TaotaoResult;
import com.taotao.common.utils.CookieUtils;
import com.taotao.common.utils.JsonUtils;
import com.taotao.order.pojo.OrderInfo;
import com.taotao.order.service.OrderService;
import com.taotao.pojo.TbItem;
import com.taotao.pojo.TbUser;

/**
 * 订单确认页面Controller
 * @author 浩瀚
 *
 */
@Controller
public class OrderCartController {
	
	@Value("${CART_KEY}")
	private String Cart_key;
	
	@Autowired
	private OrderService orderService;
	
	/**
	 * 展示订单确认页面
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping("/order/order-cart")
	public String orderCart(HttpServletRequest request,Model model){
		// 用户必须是登录状态
		// 取用户id
		TbUser user = (TbUser) request.getAttribute("user");
		System.out.println(user.getUsername());
		// 根据用户信息取收货地址列表，使用静态数据
		// 把收货地址列表传递给页面
		// 从Cookie中取购物车列表展示到页面
		List<TbItem> cartItemList = this.getCartItemList(request);
		request.setAttribute("cartList", cartItemList);
		// 返回逻辑视图
		return "order-cart";
	}
	
	/**
	 * 获取Cookie中的商品信息
	 * @param request
	 * @return
	 */
	private List<TbItem> getCartItemList(HttpServletRequest request){
		// 从cookie中取购物车商品列表
		String json = CookieUtils.getCookieValue(request,Cart_key,true);
		if(StringUtils.isBlank(json)){
			// 如果没有内容，返回一个空的列表
			return new ArrayList<>();
		}
		List<TbItem> list = JsonUtils.jsonToList(json, TbItem.class);
		return list;
	} 
	
	/**
	 * 创建订单
	 * @param orderInfo
	 * @return
	 */
	@RequestMapping(value="/order/create",method=RequestMethod.POST)
	public String createOrder(OrderInfo orderInfo,Model model){
		// 生成订单
		TaotaoResult orderResult = orderService.createOrder(orderInfo);
		// 设置跳转页面中的数据
		model.addAttribute("orderId", orderResult.getData().toString());
		model.addAttribute("payment", orderResult.getData().toString());
		// 预计送达时间，预计三天后送达,使用joda-time包对时间进行操作
		DateTime dateTime = new DateTime();
		dateTime = dateTime.plusDays(3);
		model.addAttribute("date", dateTime.toString("yyyy-MM-dd"));
		// 返回逻辑视图
		return "success";
	}
}
