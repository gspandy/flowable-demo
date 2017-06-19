/**
 * 六合彩详情数据服务层
 * 
 * @author wengwh
 * @Date 2017-02-06
 * 
 */
angular.module('plumdo.services').service('LotteryDetailService',['$http', function($http) {
	return {
		getLotteryDetails : function(params) {
			return $http({
				method: 'GET',
				url:PLUMDO.URL.getLotteryDetails(),
				params:params
			});
		},
		createLotteryDetail : function(data) {
			return $http({
				method: 'POST',
				url:PLUMDO.URL.createLotteryDetail(),
				data:data
			});
		},
		updateLotteryDetail : function(detailId,data) {
			return $http({
				method: 'PUT',
				url:PLUMDO.URL.updateLotteryDetail(detailId),
				data:data
			});
		},
		deleteLotteryDetail : function(detailId) {
			return $http({
				method: 'DELETE',
				url:PLUMDO.URL.deleteLotteryDetail(detailId)
			});
		},
		getLotteryDetail : function(detailId) {
			return $http({
				method: 'GET',
				url:PLUMDO.URL.getLotteryDetail(detailId)
			});
		},
	};
}]);