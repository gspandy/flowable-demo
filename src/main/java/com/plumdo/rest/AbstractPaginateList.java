package com.plumdo.rest;

import java.util.List;
import java.util.Map;

import org.flowable.engine.common.api.FlowableIllegalArgumentException;
import org.flowable.engine.common.api.query.Query;
import org.flowable.engine.common.api.query.QueryProperty;
import org.flowable.engine.impl.AbstractQuery;


public abstract class AbstractPaginateList {
	
	@SuppressWarnings("rawtypes")
	public DataResponse paginateList(Map<String, String> requestParams, PaginateRequest paginateRequest, Query query,
			String defaultSort, Map<String, QueryProperty> properties) {
	  	
		if (paginateRequest == null) {
	  		paginateRequest = new PaginateRequest();
	  	}
	  	
	  	// In case pagination request is incomplete, fill with values found in URL if possible
	  	if (paginateRequest.getStart() == null) {
	  		paginateRequest.setStart(RequestUtil.getInteger(requestParams, "start", 0));
	  	}
	  	
	  	if (paginateRequest.getSize() == null) {
	  		paginateRequest.setSize(RequestUtil.getInteger(requestParams, "size", 10));
	  	}
	  	
	  	if (paginateRequest.getOrder() == null) {
	  		paginateRequest.setOrder(requestParams.get("order"));
	  	}
	  	
	  	if (paginateRequest.getSort() == null) {
	  		paginateRequest.setSort(requestParams.get("sort"));
	  	}
	      
	  	// Use defaults for paging, if not set in the PaginationRequest, nor in the URL
	  	Integer start = paginateRequest.getStart();
	  	if(start == null || start < 0) {
	  		start = 0;
	  	}
	  	
	    Integer size = paginateRequest.getSize();
	    if(size == null || (size!=-1 && size < 0)) {
	    	size = 10;
	    }
	    
	    String sort = paginateRequest.getSort();
	    if(sort == null) {
	      sort = defaultSort;
	    }
	    String order = paginateRequest.getOrder();
	    if(order == null) {
	      order = "asc";
	    }

	    // Sort order
	    if (sort != null && !properties.isEmpty()) {
	    	QueryProperty qp = properties.get(sort);
	    	if (qp == null) {
	    		throw new FlowableIllegalArgumentException("Value for param 'sort' is not valid, '" + sort + "' is not a valid property");
	    	}
	    	((AbstractQuery) query).orderBy(qp);
	    	if (order.equals("asc")) {
	    		query.asc();
	    	}else if (order.equals("desc")) {
	    		query.desc();
	    	}else {
	    		throw new FlowableIllegalArgumentException("Value for param 'order' is not valid : '" + order + "', must be 'asc' or 'desc'");
	    	}
	    }

	    // Get result and set pagination parameters
	    List list = null;
	    //size等于-1不做分页
	    if(size == -1){
	    	start = 0;
	    	list = processList(query.list());
	    }else{
	    	list = processList(query.listPage(start, size));
	    }
	    DataResponse response = new DataResponse();
	    response.setStart(start);
	    response.setSize(list.size()); 
	    response.setSort(sort);
	    response.setOrder(order);
	    response.setTotal(query.count());
	    response.setData(list);
	    return response;
	}
	  
	  
	  
	@SuppressWarnings("rawtypes")
	public DataResponse paginateList(Map<String, String> requestParams, Query query,
	      String defaultSort, Map<String, QueryProperty> properties) {
		return paginateList(requestParams, null, query, defaultSort, properties);
	}
	  
	@SuppressWarnings("rawtypes")
	protected abstract List processList(List list);
}
