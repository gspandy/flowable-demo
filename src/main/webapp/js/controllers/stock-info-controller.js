/**************************StockInfoCtrl****************************/
angular.module('plumdo.controllers').controller('StockInfoCtrl',['$scope','StockInfoService','$uibModal','$state', function($scope,StockInfoService,$uibModal,$state) { 
    $scope.stockInfos = {};
    $scope.queryParams = {};

    $scope.queryStockInfos = function(tableParams){
    	PLUMDO.OtherUtil.mergeTableParams($scope.queryParams, tableParams);
    	StockInfoService.getStockInfos($scope.queryParams).success(function(data, status, header, config){
    		$scope.stockInfos = data;
    	});
    };
    
    $scope.tableOptions = {
		id:'model',
        data:'stockInfos',
		colModels:[
           {name:'编码',index:'stockCode',sortable:true,width:'20%'},
           {name:'名称',index:'stockName',sortable:true,width:'30%'},
           {name:'板块类别',index:'stockType',sortable:true,width:'20%',
        	   formatter:function(){
					return '{{row.stockType=="sz"?"深圳":"上海"}}';
				}
           },
           {name:'操作',index:'',width:'40%',
        	   	formatter:function(){
					return '<div class="btn-group">'+
					'<button class="btn btn-primary btn-xs" ng-click=openModal(row.stockId) type="button"><i class="fa fa-pencil"></i>&nbsp;修改</button>'+
					'<button class="btn btn-danger btn-xs" ng-click=deleteStockInfo(row.stockId) type="button"><i class="fa fa-trash-o"></i>&nbsp;删除</button>'+
					'<button class="btn btn-info btn-xs" ng-click=toStockDetail(row.stockCode,row.stockName) type="button"><i class="fa fa-th-list"></i>&nbsp;详情</button>'+
					'</div>';
				}
           }
        ],
        queryParams:$scope.queryParams,
        loadFunction:$scope.queryStockInfos,
        sortName:'stockCode',
        sortOrder:'asc',
        pageSize:10,
        pageList:[10,25,50]
    };

    $scope.deleteStockInfo = function(stockId){
    	$scope.confirmModal({
    		title:'确认删除股票',
    		confirm:function(isConfirm){
    			if(isConfirm){
	    			StockInfoService.deleteStockInfo(stockId).success(function(data, status, header, config){
	    				$scope.queryStockInfos();
	    			});
    			}
    		}
    	});
    };
    
    $scope.toStockDetail = function(stockCode,stockName){
    	 $state.go('stock-detail', {
    		 queryParams:{
    			 stockCode:stockCode,
    			 stockName:stockName
    		 }
    	 });
    }; 
    
    $scope.openModal = function (stockId) {
    	$scope.stockId = stockId;
        $uibModal.open({
            templateUrl: 'views/stock-info/edit.html',
            controller: 'StockInfoModalCtrl',
            scope: $scope
        });
    };
	   
}]);

angular.module('plumdo.controllers').controller('StockInfoModalCtrl',['$scope','StockInfoService','$uibModalInstance', function($scope,StockInfoService,$uibModalInstance) { 
	$scope.formdata = {};
	
	if($scope.stockId){
		$scope.modalTitle="修改股票";
		
		StockInfoService.getStockInfo($scope.stockId).success(function(data){
			$scope.formdata = data;
		});
		
		$scope.ok = function () {
			StockInfoService.updateStockInfo($scope.stockId,$scope.formdata).success(function(data){
		        $uibModalInstance.close();
		        $scope.queryStockInfos();
			});
	    };
		
	}else{
		$scope.modalTitle="添加股票";
		$scope.formdata.stockType="sz";
		$scope.ok = function () {
			StockInfoService.createStockInfo($scope.formdata).success(function(data){
		        $uibModalInstance.close();
		        $scope.queryStockInfos();
			});
		};
	}
	
    $scope.cancel = function () {
        $uibModalInstance.dismiss('cancel');
    };
}]);
