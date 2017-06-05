package com.taotao.order.serviceImpl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.taotao.common.pojo.TaotaoResult;
import com.taotao.jedis.JedisClient;
import com.taotao.mapper.TbOrderItemMapper;
import com.taotao.mapper.TbOrderMapper;
import com.taotao.mapper.TbOrderShippingMapper;
import com.taotao.order.pojo.OrderInfo;
import com.taotao.order.service.OrderService;
import com.taotao.pojo.TbOrderItem;
import com.taotao.pojo.TbOrderShipping;
/**
 * 订单处理Service
 * @author 浩瀚
 *
 */
@Service
public class OrderServiceImpl implements OrderService {
	
	@Autowired
	private TbOrderMapper orderMapper;
	
	@Autowired
	private TbOrderItemMapper orderItemMapper;
	
	@Autowired
	private TbOrderShippingMapper orderShippingMapper;
	
	@Autowired
	private JedisClient jedisClient;
	
	// 订单ID
	@Value("${ORDER_ID_GEN}")
	private String ORDER_ID_GEN_KEY;
	
	// 订单ID的初始值
	@Value("${ORDER_ID_BEGIN_VALUE}")
	private String ORDER_ID_BEGIN_VALUE;
	
	/**
	 * 生成订单
	 */
	@Override
	public TaotaoResult createOrder(OrderInfo orderInfo) {
		// 生成订单号，使用redis的incr生成
		if(jedisClient.exists(ORDER_ID_GEN_KEY)){
			// 如果不存在，设置初始值
			jedisClient.set(ORDER_ID_GEN_KEY, ORDER_ID_BEGIN_VALUE);
		}
		String orderId =jedisClient.incr(ORDER_ID_GEN_KEY).toString();
		// 向订单表插入数据，需要补全pojo属性
		orderInfo.setOrderId(orderId);
		// 免邮费
		orderInfo.setPostFee("0");
		// 订单状态：1、未付款，2、已付款，3、未发货，4、已发货，5、交易成功，6、交易关闭
		orderInfo.setStatus(1);
		// 订单创建时间
		orderInfo.setCreateTime(new Date());
		orderInfo.setUpdateTime(new Date());
		// 向订单表中插入数据
		orderMapper.insert(orderInfo);
		//向订单明细表插入数据。
		List<TbOrderItem> orderItems = orderInfo.getOrderItems();
		for (TbOrderItem tbOrderItem : orderItems) {
			// 获得明细主键
			String oid = jedisClient.incr(ORDER_ID_GEN_KEY).toString();
			tbOrderItem.setId(oid);
			tbOrderItem.setOrderId(orderId);
			// 插入明细数据
			orderItemMapper.insert(tbOrderItem);
		}
		// 向订单物流表插入数据
		TbOrderShipping orderShipping = orderInfo.getOrderShipping();
		orderShipping.setOrderId(orderId);
		orderShipping.setCreated(new Date());
		orderShipping.setUpdated(new Date());
		orderShippingMapper.insert(orderShipping);
		// 返回定单号
		return TaotaoResult.ok(orderId);
	}

}
