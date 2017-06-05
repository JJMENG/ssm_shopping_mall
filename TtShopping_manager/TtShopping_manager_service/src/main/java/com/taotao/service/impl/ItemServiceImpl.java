package com.taotao.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.taotao.common.pojo.EasyUIDataGridResult;
import com.taotao.common.pojo.TaotaoResult;
import com.taotao.common.utils.IDUtils;
import com.taotao.common.utils.JsonUtils;
import com.taotao.jedis.JedisClient;
import com.taotao.mapper.TbItemDescMapper;
import com.taotao.mapper.TbItemMapper;
import com.taotao.pojo.TbItem;
import com.taotao.pojo.TbItemDesc;
import com.taotao.pojo.TbItemExample;
import com.taotao.service.ItemService;

/**
 * 商品管理Service
 * <p>Title: ItemServiceImpl</p>
 * <p>Description: </p>
 * <p>Company: www.itcast.cn</p> 
 * @version 1.0
 */
@Service
@Transactional(readOnly=false)
public class ItemServiceImpl implements ItemService {

	@Autowired
	private TbItemMapper itemMapper;
	
	@Autowired
	private TbItemDescMapper itemDescMapper;
	
	@Autowired
	private JmsTemplate jmsTemplate;// 使用这个对象发送消息
	
	@Resource(name="itemAddtopic")
	private Destination destination;
	
	@Autowired
	private JedisClient jedisClient;
	
	@Value("${ITEM_INFO}")
	private String Item_Info;
	@Value("${ITEM_DESC}")
	private String Item_desc;
	@Value("${ITEM_EXPIRE}")
	private Integer Item_expire;
	
	@Override
	public TbItem getItemById(long itemId) {
		// 查询缓存中是否有数据
		System.out.println("开始查询");
		try {
			String itemInfo = jedisClient.get(Item_Info+":"+itemId+":BASE");
			if(StringUtils.isNoneBlank(itemInfo)){
				TbItem tbItem = JsonUtils.jsonToPojo(itemInfo, TbItem.class);
				System.out.println("查询缓存成功");
				return tbItem;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 缓存中没有就查询数据库
		TbItem item = itemMapper.selectByPrimaryKey(itemId);
		// 将查询到的结果添加到缓存
		try{
			jedisClient.set(Item_Info+":"+itemId+":BASE",JsonUtils.objectToJson(item));
			jedisClient.expire(Item_Info+":"+itemId+":BASE", Item_expire);
			System.out.println("添加缓存成功");
		}catch(Exception e){
			System.out.println("添加缓存失败");
			e.printStackTrace();
		}
		return item;
	}

	@Override
	public EasyUIDataGridResult getItemList(int page, int rows) {
		//设置分页信息
		PageHelper.startPage(page, rows);
		//执行查询
		TbItemExample example = new TbItemExample();
		List<TbItem> list = itemMapper.selectByExample(example);
		//取查询结果
		PageInfo<TbItem> pageInfo = new PageInfo<>(list);
		EasyUIDataGridResult result = new EasyUIDataGridResult();
		result.setRows(list);
		result.setTotal(pageInfo.getTotal());
		//返回结果
		return result;
	}

	@Override
	public TaotaoResult addItem(TbItem item, String desc) {
		//生成商品id
		final long itemId = IDUtils.genItemId();
		//补全item的属性
		item.setId(itemId);
		//商品状态，1-正常，2-下架，3-删除
		item.setStatus((byte) 1);
		item.setCreated(new Date());
		item.setUpdated(new Date());
		//向商品表插入数据
		itemMapper.insert(item);
		//创建一个商品描述表对应的pojo
		TbItemDesc itemDesc = new TbItemDesc();
		//补全pojo的属性
		itemDesc.setItemId(itemId);
		itemDesc.setItemDesc(desc);
		itemDesc.setUpdated(new Date());
		itemDesc.setCreated(new Date());
		//向商品描述表插入数据
		itemDescMapper.insert(itemDesc);
		// 向ActiveMq发送添加商品信息
		jmsTemplate.send(destination,new MessageCreator() {
			
			@Override
			public Message createMessage(Session session) throws JMSException {
				// 发送商品id
				TextMessage textMessage = session.createTextMessage(itemId+"");
				return textMessage;
			}
		});
		//返回结果
		return TaotaoResult.ok();
	}

	@Override
	public TbItemDesc getItemDesc(long itemId) {
		// 查询缓存中是否有数据
		System.out.println("开始查询");
		try {
			String itemDesc = jedisClient.get(Item_desc+":"+itemId+":BASE");
			if(StringUtils.isNoneBlank(itemDesc)){
				TbItemDesc tbItemDesc = JsonUtils.jsonToPojo(itemDesc, TbItemDesc.class);
				System.out.println("查询缓存成功");
				return tbItemDesc;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 缓存中没有就查询数据库
		TbItemDesc resultDesc = itemDescMapper.selectByPrimaryKey(itemId);
		try{
			// 将查询到的结果添加到缓存
			jedisClient.set(Item_desc+":"+itemId+":DESC",JsonUtils.objectToJson(resultDesc));
			// 设置过期时间
			jedisClient.expire(Item_desc+":"+itemId+":DESC", Item_expire);
			System.out.println("添加缓存成功");
		}catch(Exception e){
			System.out.println("添加缓存失败");
			e.printStackTrace();
		}
		return resultDesc;
	}

}
