/**
 * 六合彩详情数据服务层
 * 
 * @author wengwh
 * @Date 2017-02-06
 * 
 */
angular.module('plumdo.services').service('ModelService',['$http', function($http) {
	return {
		getModels : function(params) {
			return $http({
				method: 'GET',
				url:PLUMDO.URL.getModels(),
				params:params
			});
		},
		createModel : function(data) {
			return $http({
				method: 'POST',
				url:PLUMDO.URL.createModel(),
				data:data
			});
		},
		updateModel : function(modelId,data) {
			return $http({
				method: 'PUT',
				url:PLUMDO.URL.updateModel(modelId),
				data:data
			});
		},
		deleteModel : function(modelId) {
			return $http({
				method: 'DELETE',
				url:PLUMDO.URL.deleteModel(modelId)
			});
		},
		getModel : function(modelId) {
			return $http({
				method: 'GET',
				url:PLUMDO.URL.getModel(modelId)
			});
		},
		deployModel : function(modelId) {
			return $http({
				method: 'POST',
				url:PLUMDO.URL.deployModel(modelId)
			});
		},
	};
}]);