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

/*
 * Condition expression
 */

var KisBpmConditionExpressionCtrl = [ '$scope', '$modal', function($scope, $modal) {

    // Config for the modal window
    var opts = {
        template:  'editor-app/configuration/properties/condition-expression-popup.html?version=' + Date.now(),
        backdrop:'static',
        scope: $scope
    };

    // Open the dialog
    $modal(opts);
}];

var KisBpmConditionExpressionPopupCtrl = [ '$scope', '$translate', '$http', function($scope, $translate, $http) {

	// Put json representing condition on scope
    if ($scope.property.value !== undefined && $scope.property.value !== null) {

        $scope.conditionExpression = {value: $scope.property.value};
        
    } else {
        $scope.conditionExpression = {value: ''};
    }
    
    $scope.conditionExpression.type1 = 'variable';
    $scope.conditionExpression.type2 = 'static';
	$scope.conditionExpression.param1 = '';
	$scope.conditionExpression.param2 = '';
    $scope.conditionExpression.expression = 'equals';
    $scope.conditionExpression.link = 'empty';
    
    $scope.reset = function() {
    	$scope.conditionExpression.type1 = 'variable';
	    $scope.conditionExpression.type2 = 'static';
    	$scope.conditionExpression.param1 = '';
    	$scope.conditionExpression.param2 = '';
	    $scope.conditionExpression.expression = 'equals';
	    $scope.conditionExpression.link = 'empty';
    };
    
    
    $scope.add = function() {
    	var expressionValue = null;
    	var param1 = null;
    	var param2 = null;
    	var link = null;
    	if($scope.conditionExpression.type1 == 'variable'){
    		param1 = $scope.conditionExpression.param1;
    	}else{
    		param1 = "'" + $scope.conditionExpression.param1 + "'";
    	}
  
    	if($scope.conditionExpression.type2 == 'variable'){
    		param2 = $scope.conditionExpression.param2;
    	}else{
    		param2 = "'" + $scope.conditionExpression.param2 + "'";
    	}
    	
    	if($scope.conditionExpression.link == 'and'){
    		link = "&&";
    	}else if($scope.conditionExpression.link == 'or'){
    		link = "||";
    	}else{
    		link = "";
    	}
    	
    	if($scope.conditionExpression.expression == 'equals'){
        	expressionValue = param1 + " == " + param2;
    	}else if($scope.conditionExpression.expression == 'notEquals'){
        	expressionValue = param1 + " != " + param2;
    	}else if($scope.conditionExpression.expression == 'less'){
        	expressionValue = param1 + " < " + param2;
    	}else if($scope.conditionExpression.expression == 'greater'){
        	expressionValue = param1 + " > " + param2;
    	}else if($scope.conditionExpression.expression == 'lessEquals'){
        	expressionValue = param1 + " <= " + param2;
    	}else if($scope.conditionExpression.expression == 'greaterEquals'){
        	expressionValue = param1 + " >= " + param2;
    	}else if($scope.conditionExpression.expression == 'contains'){
        	expressionValue = "uel.contains("+param1+","+param2+")";
    	}else if($scope.conditionExpression.expression == 'notContains'){
        	expressionValue = "uel.notContains("+param1+","+param2+")";
    	}else if($scope.conditionExpression.expression == 'startsWith'){
        	expressionValue = "uel.startsWith("+param1+","+param2+")";
    	}else if($scope.conditionExpression.expression == 'endsWith'){
        	expressionValue = "uel.endsWith("+param1+","+param2+")";
    	}

    	$scope.conditionExpression.value = $scope.conditionExpression.value.replace(/(\s*$)/g,"")
    	
    	if($scope.conditionExpression.value == ''){
    		$scope.conditionExpression.value = "${"+expressionValue+"}";
    	}else if($scope.conditionExpression.value.indexOf("}")==$scope.conditionExpression.value.length-1){
    		$scope.conditionExpression.value = $scope.conditionExpression.value.substr(0,$scope.conditionExpression.value.length-1)
    			+ " " + link + " "+ expressionValue+"}";
    	}else{
    		$scope.conditionExpression.value = $scope.conditionExpression.value + " " + link + " "+ expressionValue;
    	}
    };
    
    $scope.save = function() {
        $scope.property.value = $scope.conditionExpression.value;
        $scope.updatePropertyInModel($scope.property);
        $scope.close();
    };

    // Close button handler
    $scope.close = function() {
    	$scope.property.mode = 'read';
    	$scope.$hide();
    };
}];