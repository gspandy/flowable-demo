package com.plumdo.rest.model;

import java.util.List;

import com.plumdo.rest.AbstractPaginateList;
import com.plumdo.rest.RestResponseFactory;



public class ModelsPaginateList extends AbstractPaginateList {

	protected RestResponseFactory restResponseFactory;

	public ModelsPaginateList(RestResponseFactory restResponseFactory) {
		this.restResponseFactory = restResponseFactory;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected List processList(List list) {
		return restResponseFactory.createModelResponseList(list);
	}
}
