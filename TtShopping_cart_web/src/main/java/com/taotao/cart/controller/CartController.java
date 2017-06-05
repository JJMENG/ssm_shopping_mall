package com.taotao.cart.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.taotao.common.pojo.TaotaoResult;
import com.taotao.common.utils.CookieUtils;
import com.taotao.common.utils.JsonUtils;
import com.taotao.pojo.TbItem;
import com.taotao.service.ItemService;

@Controller
public class CartController {
	
	@Value("${CART_KEY}")
	private String Cart_key;
	
	@Value("${CART_EXPIER}")
	private Integer Cart_Expier;
	
	@Autowired
	private ItemService itemService;
	
	/**
	 * 添加到购物车
	 * @param itemId
	 * @param num
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/cart/add/{itemId}")
	public String addItemCart(@PathVariable Long itemId,@RequestParam(defaultValue="1")Integer num,
			HttpServletRequest request,HttpServletResponse response ){
		// 取购物车商品列表
		List<TbItem> cartItemList = this.getCartItemList(request);
		// 判断商品在购物车中是否存在
		boolean flag = false;
		for (TbItem tbItem : cartItemList) {
			if(tbItem.getId() == itemId.longValue()){
				// 如果存在数量相加
				tbItem.setNum(tbItem.getNum() + num);
				flag = true;
				break;
			}
		}
		// 如果不存在，这添加一个新商品
		if(!flag){
			// 需要调用服务器取商品信息
			TbItem resultItem = itemService.getItemById(itemId);
			// 设置商品数量
			resultItem.setNum(num);
			// 分割图片URL,只取第一张图片
			String image = resultItem.getImage();
			if(StringUtils.isNotBlank(image)){
				String[] imageString = image.split(",");
				resultItem.setImage(imageString[0]);
			}
			// 将商品添加到购物车
			cartItemList.add(resultItem);
		}
		// 将旧的Cookie删除
		CookieUtils.deleteCookie(request, response, Cart_key);
		// 将列表存入Cookie
		CookieUtils.setCookie(request, response, Cart_key, JsonUtils.objectToJson(cartItemList), Cart_Expier);
		return "cartSuccess";
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
	 * 展示购物车信息
	 * @param request
	 * @return
	 */
	@RequestMapping("/cart/cart")
	public String showCartList(HttpServletRequest request){
		// 从Cookie中取购物车信息
		List<TbItem> cartItemList = this.getCartItemList(request);
		// 将列表信息传递给页面
		request.setAttribute("cartList", cartItemList);
		// 返回逻辑视图
		return "cart";
	}
	
	/**
	 * 更新商品数量
	 * @param itemId
	 * @param num
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/cart/update/num/{itemId}/{num}")
	@ResponseBody
	public TaotaoResult updateItemNum(@PathVariable Long itemId,@PathVariable Integer num,
			HttpServletRequest request,HttpServletResponse response){
		// 从Cookie中获取商品信息
		List<TbItem> cartItemList = this.getCartItemList(request);
		// 查询到对应的商品
		for (TbItem tbItem : cartItemList) {
			if(tbItem.getId() == itemId.longValue()){
				// 更新商品信息
				tbItem.setNum(num);
				break;
			}
		}
		// 将购物车列表存入Cookie
		CookieUtils.setCookie(request, response, Cart_key, JsonUtils.objectToJson(cartItemList), Cart_Expier);
		return TaotaoResult.ok();
	}
	
	/**
	 * 删除商品信息
	 * @param ItemId
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/cart/delete/{ItemId}")
	public String deleteCartItem(@PathVariable Long ItemId,HttpServletRequest request,HttpServletResponse response){
		// 从Cookie中查找到对应的商品信息
		List<TbItem> cartItemList = getCartItemList(request);
		// 查询到对应的商品
		for (TbItem tbItem : cartItemList) {
			if(tbItem.getId() == ItemId.longValue()){
				// 删除商品信息
				cartItemList.remove(tbItem);
				break;
			}
		}
		// 将商品列表添加到Cookie
		CookieUtils.setCookie(request, response, Cart_key, JsonUtils.objectToJson(cartItemList), Cart_Expier);
		return "redirect:/cart/cart.html";
	}
}

