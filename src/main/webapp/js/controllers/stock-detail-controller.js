/**************************StockDetailCtrl****************************/
angular.module('plumdo.controllers').controller('StockDetailCtrl',['$scope','StockDetailService','$uibModal', '$stateParams','$state', function($scope,StockDetailService,$uibModal, $stateParams,$state) { 
    $scope.stockDetails = {};

    $scope.queryParams =  $stateParams.queryParams;

    $scope.queryStockDetails = function(tableParams){
    	PLUMDO.OtherUtil.mergeTableParams($scope.queryParams, tableParams);
    	
    	StockDetailService.getStockDetails($scope.queryParams).success(function(data, status, header, config){
    		$scope.stockDetails = data;
    	});
    };
    
    $scope.tableOptions = {
		id:'stockDetailModel',
        data:'stockDetails',
		colModels:[
           {name:'编码',index:'stockCode',sortable:true,width:'10%'},
           {name:'名称',index:'stockName',sortable:true,width:'10%'},
           {name:'开盘价',index:'beginPrice',sortable:true,width:'7%'},
           {name:'收盘价',index:'endPrice',sortable:true,width:'7%'},
           {name:'最高价',index:'highestPrice',sortable:true,width:'7%'},
           {name:'最低价',index:'latestPrice',sortable:true,width:'7%'},
           {name:'交易数',index:'stockNum',sortable:true,width:'12%'},
           {name:'交易金额',index:'stockMoney',sortable:true,width:'13%'},
           {name:'交易时间',index:'stockDate',sortable:true,width:'10%'},
           {name:'操作',index:'',width:'100',
        	   	formatter:function(){
					return '<div class="btn-group">'+
					'<button class="btn btn-primary btn-xs" ng-click=openModal(row.detailId) type="button"><i class="fa fa-pencil"></i>&nbsp;修改</button>'+
					'<button class="btn btn-danger btn-xs" ng-click=deleteStockDetail(row.detailId) type="button"><i class="fa fa-trash-o"></i>&nbsp;删除</button>'+
					'</div>';
        	   	}
           }
        ],
        loadFunction:$scope.queryStockDetails,
        queryParams:$scope.queryParams,
        sortName:'stockCode',
        sortOrder:'asc',
        pageSize:10,
        pageList:[10,25,50]
    };

    $scope.deleteStockDetail = function(detailId){
    	$scope.confirmModal({
    		title:'确认删除股票',
    		confirm:function(isConfirm){
    			if(isConfirm){
	    			StockDetailService.deleteStockDetail(detailId).success(function(data, status, header, config){
	    				$scope.queryStockDetails();
	    			});
    			}
    		}
    	});
    };
    
    $scope.collectStockDetails = function(){
    	$scope.confirmModal({
    		title:'确认采集股票',
    		confirm:function(isConfirm){
    			if(isConfirm){
	    			StockDetailService.collectStockDetails(10).success(function(data, status, header, config){
	    				$scope.queryStockDetails();
	    			});
    			}
    		}
    	});
    };
    
    $scope.openModal = function (detailId) {
    	$scope.detailId = detailId;
        $uibModal.open({
            templateUrl: 'views/stock-detail/edit.html',
            controller: 'StockDetailModalCtrl',
            scope: $scope
        });
    };
	   
}]);

angular.module('plumdo.controllers').controller('StockDetailModalCtrl',['$scope','StockDetailService','$uibModalInstance', function($scope,StockDetailService,$uibModalInstance) { 
	$scope.formdata = {};
	
	if($scope.detailId){
		$scope.modalTitle="修改股票详情";
		
		StockDetailService.getStockDetail($scope.detailId).success(function(data){
			$scope.formdata = data;
		});
		
		$scope.ok = function () {
			StockDetailService.updateStockDetail($scope.detailId,$scope.formdata).success(function(data){
		        $uibModalInstance.close();
		        $scope.queryStockDetails();
			});
	    };
		
	}else{
		$scope.modalTitle="添加股票详情";
		
		$scope.ok = function () {
			StockDetailService.createStockDetail($scope.formdata).success(function(data){
		        $uibModalInstance.close();
		        $scope.queryStockDetails();
			});
		};
	}

    $scope.cancel = function () {
        $uibModalInstance.dismiss('cancel');
    };
}]);

    
