package com.taotao.content.serviceImpl;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taotao.common.pojo.TaotaoResult;
import com.taotao.common.utils.JsonUtils;
import com.taotao.content.service.ContentService;
import com.taotao.jedis.JedisClient;
import com.taotao.mapper.TbContentMapper;
import com.taotao.pojo.TbContent;
import com.taotao.pojo.TbContentExample;
import com.taotao.pojo.TbContentExample.Criteria;

@Service
@Transactional(readOnly = false)
public class ContentServiceImpl implements ContentService {
	
	@Autowired
	private TbContentMapper contentMapper;
	
	@Autowired
	private JedisClient jedisClient;
	
	@Value("${INDEX_CONTENT}")
	private String INDEX_CONTENT;
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Override
	public TaotaoResult addContent(TbContent content) {
		//补全pojo的属性
		content.setCreated( new Date());
		content.setUpdated(new Date());
		//插入到内容表
		contentMapper.insert(content);
		// 同步缓存
		// 删除对应的缓存信息
		jedisClient.hdel(INDEX_CONTENT, content.getCategoryId().toString());
		logger.info("删除对应缓存");
		//System.out.println("删除对应缓存");
		return TaotaoResult.ok();
	}

	@Override
	public List<TbContent> getContentByCid(long cid) {
		// 先查询缓存
		// 添加缓存不影响业务逻辑
		System.out.println("开始查询");
		try {
			// 查询缓存
			String json = jedisClient.hget(INDEX_CONTENT, cid+"");
			// 查询到结果，将json转换成list
			if(StringUtils.isNoneBlank(json)){
				List<TbContent> list = JsonUtils.jsonToList(json, TbContent.class);
				System.out.println("查询缓存成功");
				return list;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 缓存没有命中，查询数据库
		TbContentExample example = new TbContentExample();
		Criteria criteria = example.createCriteria();
		//设置查询条件
		criteria.andCategoryIdEqualTo(cid);
		//执行查询
		List<TbContent> list = contentMapper.selectByExample(example);
		// 将结果添加到缓存
		try {
			jedisClient.hset("INDEX_CONTENT",cid+"", JsonUtils.objectToJson(list));
			System.out.println("添加缓存成功");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("添加缓存失败");
		}
		// 返回结果
		return list;
	}

	
	
}
