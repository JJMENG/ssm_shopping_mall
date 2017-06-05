package com.taotao.search.listener;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;

import com.taotao.common.pojo.SearchItem;
import com.taotao.search.mapper.SearchItemMapper;

/**
 * 监听商品添加事件，同步索引库
 * @author 浩瀚
 *
 */
public class ItemAddMessageListener implements MessageListener{

	@Autowired
	private SearchItemMapper searchItemMapper;
	
	@Autowired
	private SolrServer solrServer;
	
	@Override
	public void onMessage(Message message) {
		try {
			// 从消息中取商品ID
			TextMessage textMessage = (TextMessage) message; 
			String text = textMessage.getText();
			Long itemId = Long.parseLong(text);
			// 消息发送端的事务可能没有提交，但是消息已经到达，所以让这个线程等待事务提交
			Thread.sleep(1000);
			// 根据商品ID查询数据，取得商品数据
			SearchItem searchItem = searchItemMapper.getItemById(itemId);
			// 创建文档对象
			SolrInputDocument document = new SolrInputDocument();
			// 向文档对象中添加域
			document.addField("id", searchItem.getId());
			document.addField("item_title", searchItem.getTitle());
			document.addField("item_sell_point", searchItem.getSell_point());
			document.addField("item_price", searchItem.getPrice());
			document.addField("item_image", searchItem.getImage());
			document.addField("item_category_name", searchItem.getCategory_name());
			document.addField("item_desc", searchItem.getItem_desc());
			// 将文档写入索引库
			solrServer.add(document);
			// 提交事务
			solrServer.commit();
		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}

}
