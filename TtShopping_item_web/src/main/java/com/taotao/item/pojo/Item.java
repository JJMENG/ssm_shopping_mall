package com.taotao.item.pojo;

import com.taotao.pojo.TbItem;
/**
 * 商品详情页的pojo
 * @author 浩瀚
 *
 */
public class Item extends TbItem {
	
	public Item (TbItem tbItem){
		// 初始化数据
		this.setId(tbItem.getId());
		this.setTitle(tbItem.getTitle());
		this.setSellPoint(tbItem.getSellPoint());
		this.setPrice(tbItem.getPrice());
		this.setNum(tbItem.getNum());
		this.setBarcode(tbItem.getBarcode());
		this.setImage(tbItem.getImage());
		this.setCid(tbItem.getCid());
		this.setStatus(tbItem.getStatus());
		this.setCreated(tbItem.getCreated());
		this.setUpdated(tbItem.getUpdated());
	}
	
	public String[] getImages(){
		if(this.getImage()!=null && ("").equals(this.getImage())){
			String images = this.getImage();
			String[] imageString = images.split(",");
			return imageString;
		}
		return null;
	}
	
}
