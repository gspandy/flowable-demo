/**
 * 妖股详情数据控制层
 * 
 * @author wengwh
 * @Date 2016-10-25
 * 
 */
angular.module('plumdo.controllers').controller('StockMonsterCtrl',['$scope','StockMonsterService','$uibModal', function($scope,StockMonsterService,$uibModal) { 
	$scope.stockMonsters = {};
	$scope.queryParams = {};

	$scope.queryStockMonsters = function(tableParams){
		PLUMDO.OtherUtil.mergeTableParams($scope.queryParams, tableParams);
		StockMonsterService.getStockMonsters($scope.queryParams).success(function(data, status, header, config){
			$scope.stockMonsters = data;
		});
	};

	    
	$scope.tableOptions = {
		id:'model',
		data:'stockMonsters',
			colModels:[
			{name:'股票编码',index:'stockCode',sortable:true,width:'25%'},
			{name:'股票名称',index:'stockName',sortable:true,width:'25%'},
			{name:'采集时间',index:'collectTime',sortable:true,width:'25%'},
			{name:'操作',index:'',width:'25%',
				formatter:function(){
					return '<div class="btn-group">'+
					'<button class="btn btn-primary btn-xs" ng-click=openModal(row.monsterId) type="button"><i class="fa fa-pencil"></i>&nbsp;修改</button>'+
					'<button class="btn btn-danger btn-xs" ng-click=deleteStockMonster(row.monsterId) type="button"><i class="fa fa-trash-o"></i>&nbsp;删除</button>'+
					'</div>';
				}
			}
		],
		loadFunction:$scope.queryStockMonsters,
        queryParams:$scope.queryParams,
		sortName:'stockCode',
		sortOrder:'asc',
		pageSize:10,
		pageList:[10,25,50,100]
	};

	$scope.deleteStockMonster = function(monsterId){
		$scope.confirmModal({
			title:'确认删除妖股详情',
			confirm:function(isConfirm){
				if(isConfirm){
					StockMonsterService.deleteStockMonster(monsterId).success(function(data, status, header, config){
						$scope.showSuccessMsg('删除妖股详情成功');
						$scope.queryStockMonsters();
					});
				}
			}
		});
	};

	$scope.openModal = function (monsterId) {
		$scope.monsterId = monsterId;
		$uibModal.open({
			templateUrl: 'views/stock-monster/edit.html',
			controller: 'StockMonsterModalCtrl',
			scope: $scope
		});
	};

}]);

angular.module('plumdo.controllers').controller('StockMonsterModalCtrl',['$scope','StockMonsterService','StockInfoService','$uibModalInstance', function($scope,StockMonsterService,StockInfoService,$uibModalInstance) { 	
	$scope.formdata = {};
	$scope.stockInfos = [];
	$scope.stockInfo = {};
	
	StockInfoService.getStockInfos({pageSize:-1}).success(function(data, status, header, config){
		$scope.stockInfos = data.records;
	});
	
	if($scope.monsterId){
		$scope.modalTitle="修改妖股详情";

		StockMonsterService.getStockMonster($scope.monsterId).success(function(data){
			$scope.formdata = data;
			$scope.stockInfo.selected = data;
		});

		$scope.ok = function () {
			angular.extend($scope.formdata, $scope.stockInfo.selected);
			StockMonsterService.updateStockMonster($scope.monsterId,$scope.formdata).success(function(data){
				$uibModalInstance.close();
				$scope.showSuccessMsg('修改妖股详情成功');
				$scope.queryStockMonsters();
			});
		};

	}else{
		$scope.modalTitle="添加妖股详情";
		$scope.ok = function () {
			angular.extend($scope.formdata, $scope.stockInfo.selected);
			StockMonsterService.createStockMonster($scope.formdata).success(function(data){
				$uibModalInstance.close();
				$scope.showSuccessMsg('添加妖股详情成功');
				$scope.queryStockMonsters();
			});
		};
	}

	$scope.cancel = function () {
		$uibModalInstance.dismiss('cancel');
	};
}]);