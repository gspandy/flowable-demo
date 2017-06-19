/*
 * Activiti Modeler component part of the Activiti project
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

/**
 * Utility methods are grouped together here.
 */
var EDITOR = EDITOR || {};

EDITOR.UTIL = {

    getParameterByName: function (name) {
        name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
        var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
            results = regex.exec(location.search);
        return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
    },
    
    getProjectName: function(){
    	var pathName = window.document.location.pathname;
    	console.info( pathName.substring(1,pathName.substr(1).indexOf('/')+1))
    	return pathName.substring(1,pathName.substr(1).indexOf('/')+1);
    },
    
    getJsonByElement : function (element) {
    	if(element){
    		if(element.constructor == String){
    			return JSON.parse(element);
    		}else{
    			return angular.copy(element);
    		}
    	}
    	return element;
    },
    getRootParent : function(activity){
    	if(activity.parent){
    		return EDITOR.UTIL.getRootParent(activity.parent);
    	}else{
    		return activity;
    	}
    },
    getPreActivity : function(activity) {
    	 var iteratedActivities = [];//记录遍历的节点防止出现循环情况
         var preActivityMap = {};
         EDITOR.UTIL.getIncomingActivity(activity, iteratedActivities, preActivityMap);
         return preActivityMap;
    },
    
    getIncomingActivity : function(activity, iteratedActivities, preActivityMap) {
        if (activity && activity.getIncomingShapes && iteratedActivities.indexOf(activity)==-1) {
        	iteratedActivities.push(activity);
            var incomingShapes = activity.getIncomingShapes();
            if(incomingShapes.length>0){
	            for (var i=0; i<incomingShapes.length; i++) {
	                var sourceActivity = incomingShapes[i].getSource();
		    		EDITOR.UTIL.getChildNode(sourceActivity,preActivityMap);
	                EDITOR.UTIL.getIncomingActivity(sourceActivity, iteratedActivities, preActivityMap);
	            }
            }else{
            	//如果没有进来的线判断是否有父节点，可能存在内嵌父流程
            	 EDITOR.UTIL.getIncomingActivity(activity.parent, iteratedActivities, preActivityMap);
            }
        }
    },
    
    getChildNode : function(activity,preActivityMap){
    	if(activity){
	    	if(activity.getStencil().id().indexOf("UserTask")!=-1){
    			preActivityMap[activity.resourceId]=activity;
	    	}
	    	angular.forEach(activity.nodes, function(value) {
	    		EDITOR.UTIL.getChildNode(value,preActivityMap);
	     	});
    	}
    },
    
    /**
     * Starts at the provided start element, and walks all preceding elements in the graph.
     * Each element is tested to have a certain property and, if it has, adds this property value
     * to the return result list.
     */
    collectPropertiesFromPrecedingElements: function (startElement, propertyType) {
        var visitedElements = [];
        var collectedProperties = [];
        EDITOR.UTIL._visitElementAndCollectProperty(startElement, propertyType, visitedElements, collectedProperties);
        return collectedProperties;
    },

    /**
     * Starts at the provided start element, and walks all preceding elements in the graph.
     * Each element is tested to be a specific stencil id and, if it has, adds the element
     * to the return result list.
     */
    collectElementsFromPrecedingElements: function (startElement, stencilId) {
        var visitedElements = [];
        var collectedElements = [];

        var incomingShapesIterator = startElement.getIncomingShapes();
        if (incomingShapesIterator) {
            for (var i = 0; i < incomingShapesIterator.length; i++) {
                var incomingShape = incomingShapesIterator[i];
                if (visitedElements.indexOf(incomingShape.id) < 0) {
                    EDITOR.UTIL._visitElementAndCollectElement(incomingShape, stencilId, visitedElements, collectedElements);
                }
            }
        }

        return collectedElements;
    },

    _visitElementAndCollectProperty: function (element, propertyType, visitedElementsArray, collectedProperties) {

        visitedElementsArray.push(element.id);

        var property = element.properties[propertyType]
        if (property) {
            collectedProperties.push(property);
        }

        var incomingShapesIterator = element.getIncomingShapes();
        if (incomingShapesIterator) {
            for (var i = 0; i < incomingShapesIterator.length; i++) {
                var incomingShape = incomingShapesIterator[i];
                if (visitedElementsArray.indexOf(incomingShape.id) < 0) {
                    EDITOR.UTIL._visitElementAndCollectProperty(incomingShape, propertyType, visitedElementsArray, collectedProperties);
                }
            }
        }
    },

    _visitElementAndCollectElement: function (element, stencilId, visitedElementsArray, collectedElements) {

        visitedElementsArray.push(element.id);

        var elementStencilId = element.getStencil().id();
        if (elementStencilId && elementStencilId.indexOf(stencilId) >= 0) {
            collectedElements.push(element);
        }

        var incomingShapesIterator = element.getIncomingShapes();
        if (incomingShapesIterator) {
            for (var i = 0; i < incomingShapesIterator.length; i++) {
                var incomingShape = incomingShapesIterator[i];
                if (visitedElementsArray.indexOf(incomingShape.id) < 0) {
                    EDITOR.UTIL._visitElementAndCollectElement(incomingShape, stencilId, visitedElementsArray, collectedElements);
                }
            }
        }
    },

    /**
     * Goes up the chain of parents of the provided element.
     * When the property is encountered, its value is immediately returned.
     * If the chain of parents is completely walked through, undefined is returned.
     */
    getPropertyFromParent: function (element, propertyType) {
        if (element.parent) {
            return EDITOR.UTIL._getPropertyFromParent(element.parent, propertyType);
        } else {
            return undefined;
        }

    },

    _getPropertyFromParent: function (parentElement, propertyType) {
        var property = parentElement.properties[propertyType];
        if (property) {
            return property;
        }

        if (parentElement.parent) {
            return EDITOR.UTIL._getPropertyFromParent(parentElement.parent, propertyType);
        } else {
            return undefined;
        }
    }

};