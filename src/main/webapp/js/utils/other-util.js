'use strict';

var PLUMDO = PLUMDO || {};

PLUMDO.OtherUtil = {
	
    mergeTableParams : function(queryParams,tableParams){
    	if(angular.isObject(tableParams)){
        	angular.extend(queryParams, tableParams);
    	}else if(angular.isNumber(tableParams)){
    		queryParams.pageNum = tableParams;
    	}
    },
	    
};
