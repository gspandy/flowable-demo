/**
 * 六合彩详情数据控制层
 * 
 * @author wengwh
 * @Date 2017-02-06
 * 
 */
angular.module('plumdo.controllers').controller('LotteryDetailCtrl',['$scope','LotteryDetailService','$uibModal','$state', function($scope,LotteryDetailService,$uibModal,$state) { 
	$scope.lotteryDetails = {};
	$scope.queryParams = {};

	$scope.queryLotteryDetails = function(tableParams){
		PLUMDO.OtherUtil.mergeTableParams($scope.queryParams, tableParams);

		LotteryDetailService.getLotteryDetails($scope.queryParams).success(function(data, status, header, config){
			$scope.lotteryDetails = data;
		});
	};

	$scope.tableOptions = {
		id:'model',
		data:'lotteryDetails',
			colModels:[
			{name:'彩票年份',index:'lotteryYear',sortable:true,width:'10%'},
			{name:'彩票期数',index:'lotteryPeriod',sortable:true,width:'10%'},
			{name:'号码1',index:'lotteryN1',sortable:true,width:'10%'},
			{name:'号码2',index:'lotteryN2',sortable:true,width:'10%'},
			{name:'号码3',index:'lotteryN3',sortable:true,width:'10%'},
			{name:'号码4',index:'lotteryN4',sortable:true,width:'10%'},
			{name:'号码5',index:'lotteryN5',sortable:true,width:'10%'},
			{name:'号码6',index:'lotteryN6',sortable:true,width:'10%'},
			{name:'特码',index:'lotteryCode',sortable:true,width:'10%'},
			{name:'操作',index:'',width:'20%',
				formatter:function(){
					return '<div class="btn-group">'+
					'<button class="btn btn-primary btn-xs" ng-click=openModal(row.detailId) type="button"><i class="fa fa-pencil"></i>&nbsp;修改</button>'+
					'<button class="btn btn-danger btn-xs" ng-click=deleteLotteryDetail(row.detailId) type="button"><i class="fa fa-trash-o"></i>&nbsp;删除</button>'+
					'</div>';
				}
			}
		],
		loadFunction:$scope.queryLotteryDetails,
		queryParams:$scope.queryParams,
		sortName:'lotteryYear',
		sortOrder:'asc',
		pageSize:10,
		pageList:[10,25,50,100]
	};

	$scope.deleteLotteryDetail = function(detailId){
		$scope.confirmModal({
			title:'确认删除六合彩详情',
			confirm:function(isConfirm){
				if(isConfirm){
					LotteryDetailService.deleteLotteryDetail(detailId).success(function(data, status, header, config){
						$scope.showSuccessMsg('删除六合彩详情成功');
						$scope.queryLotteryDetails();
					});
				}
			}
		});
	};

	$scope.openModal = function (detailId) {
		$scope.detailId = detailId;
		$uibModal.open({
			templateUrl: 'views/lottery-detail/edit.html',
			controller: 'LotteryDetailModalCtrl',
			scope: $scope
		});
	};

}]);

angular.module('plumdo.controllers').controller('LotteryDetailModalCtrl',['$scope','LotteryDetailService','$uibModalInstance', function($scope,LotteryDetailService,$uibModalInstance) { 
	$scope.formdata = {};

	if($scope.detailId){
		$scope.modalTitle="修改六合彩详情";

		LotteryDetailService.getLotteryDetail($scope.detailId).success(function(data){
			$scope.formdata = data;
		});

		$scope.ok = function () {
			LotteryDetailService.updateLotteryDetail($scope.detailId,$scope.formdata).success(function(data){
				$uibModalInstance.close();
				$scope.showSuccessMsg('修改六合彩详情成功');
				$scope.queryLotteryDetails();
			});
		};

	}else{
		$scope.modalTitle="添加六合彩详情";
		$scope.ok = function () {
			LotteryDetailService.createLotteryDetail($scope.formdata).success(function(data){
				$uibModalInstance.close();
				$scope.showSuccessMsg('添加六合彩详情成功');
				$scope.queryLotteryDetails();
			});
		};
	}

	$scope.cancel = function () {
		$uibModalInstance.dismiss('cancel');
	};
}]);