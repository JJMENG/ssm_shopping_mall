package com.taotao.common.pojo;

import java.io.Serializable;
import java.util.List;

/**
 * 搜索后显示
 * @author 浩瀚
 *
 */
public class SearchItemResult implements Serializable{
	
	private Long totalPages;
	private Long recordCount;
	private List<SearchItem> itemList;
	
	public Long getTotalPages() {
		return totalPages;
	}
	public void setTotalPages(long pages) {
		this.totalPages = pages;
	}
	public List<SearchItem> getItemList() {
		return itemList;
	}
	public void setItemList(List<SearchItem> itemList) {
		this.itemList = itemList;
	}
	public Long getRecordCount() {
		return recordCount;
	}
	public void setRecordCount(Long recordCount) {
		this.recordCount = recordCount;
	}
	@Override
	public String toString() {
		return "SearchItemResult [totalPages=" + totalPages + ", recordCount=" + recordCount + ", itemList=" + itemList
				+ "]";
	}
	
	
	
}
