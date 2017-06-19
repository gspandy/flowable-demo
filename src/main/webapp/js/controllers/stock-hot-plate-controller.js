
/**
 * 热门板块数据控制层
 * 
 * @author wengwh
 * @Date 2016-10-21
 * 
 */
angular.module('plumdo.controllers').controller('StockHotPlateCtrl',['$scope','StockHotPlateService','$uibModal','$state', function($scope,StockHotPlateService,$uibModal,$state) { 
	$scope.stockHotPlates = {};
	$scope.queryParams = {};

	$scope.queryStockHotPlates = function(tableParams){
    	PLUMDO.OtherUtil.mergeTableParams($scope.queryParams, tableParams);

		StockHotPlateService.getStockHotPlates($scope.queryParams).success(function(data, status, header, config){
			$scope.stockHotPlates = data;
		});
	};

	$scope.tableOptions = {
		id:'model',
		data:'stockHotPlates',
			colModels:[
			{name:'热门板块名称',index:'plateName',sortable:true,width:'33%'},
			{name:'采集时间',index:'collectTime',sortable:true,width:'33%'},
			{name:'操作',index:'',width:'40%',
				formatter:function(){
					return '<div class="btn-group">'+
					'<button class="btn btn-primary btn-xs" ng-click=openModal(row.hotPlateId) type="button"><i class="fa fa-pencil"></i>&nbsp;修改</button>'+
					'<button class="btn btn-danger btn-xs" ng-click=deleteStockHotPlate(row.hotPlateId) type="button"><i class="fa fa-trash-o"></i>&nbsp;删除</button>'+
					'</div>';
				}
			}
		],
		loadFunction:$scope.queryStockHotPlates,
        queryParams:$scope.queryParams,
		sortName:'plateName',
		sortOrder:'asc',
		pageSize:10,
		pageList:[10,25,50,100]
	};

	$scope.deleteStockHotPlate = function(hotPlateId){
		$scope.confirmModal({
			title:'确认删除热门板块',
			confirm:function(isConfirm){
				if(isConfirm){
					StockHotPlateService.deleteStockHotPlate(hotPlateId).success(function(data, status, header, config){
						$scope.showSuccessMsg('删除热门板块成功');
						$scope.queryStockHotPlates();
					});
				}
			}
		});
	};

	$scope.openModal = function (hotPlateId) {
		$scope.hotPlateId = hotPlateId;
		$uibModal.open({
			templateUrl: 'views/stock-hot-plate/edit.html',
			controller: 'StockHotPlateModalCtrl',
			scope: $scope
		});
	};

}]);

angular.module('plumdo.controllers').controller('StockHotPlateModalCtrl',['$scope','StockHotPlateService','$uibModalInstance', function($scope,StockHotPlateService,$uibModalInstance) { 	$scope.formdata = {};

	if($scope.hotPlateId){
		$scope.modalTitle="修改热门板块";

		StockHotPlateService.getStockHotPlate($scope.hotPlateId).success(function(data){
			$scope.formdata = data;
		});

		$scope.ok = function () {
			StockHotPlateService.updateStockHotPlate($scope.hotPlateId,$scope.formdata).success(function(data){
				$uibModalInstance.close();
				$scope.showSuccessMsg('修改热门板块成功');
				$scope.queryStockHotPlates();
			});
		};

	}else{
		$scope.modalTitle="添加热门板块";
		$scope.ok = function () {
			StockHotPlateService.createStockHotPlate($scope.formdata).success(function(data){
				$uibModalInstance.close();
				$scope.showSuccessMsg('添加热门板块成功');
				$scope.queryStockHotPlates();
			});
		};
	}

	$scope.cancel = function () {
		$uibModalInstance.dismiss('cancel');
	};
}]);