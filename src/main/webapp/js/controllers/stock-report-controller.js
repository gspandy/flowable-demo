/**************************StockReportCtrl****************************/
angular.module('plumdo.controllers').controller('StockReportCtrl',['$scope','StockReportService','$uibModal','$state', function($scope,StockReportService,$uibModal,$state) { 
    $scope.stockGolds = {};
    $scope.stockWeaks = {};
    $scope.queryParams = {};

    $scope.getStockGolds = function(tableParams){
    	PLUMDO.OtherUtil.mergeTableParams($scope.queryParams, tableParams);
    	StockReportService.getStockGolds($scope.queryParams).success(function(data, status, header, config){
    		$scope.stockGolds = data;
    	});
    };
    
    $scope.goldTableOptions = {
		id:'goldModel',
        data:'stockGolds',
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
           {name:'',index:'',width:'10%',
       	   	formatter:function(){
					return '<button class="btn btn-info btn-xs" ng-click=toStockDetail(row.stockCode,row.stockName,row.stockDate) type="button"><i class="fa fa-th-list"></i>&nbsp;详情</button>';
				}
          }
        ],
        queryParams:$scope.queryParams,
        loadFunction:$scope.getStockGolds,
        sortName:'stockDate',
        sortOrder:'asc',
        pageSize:10,
        pageList:[10,25,50,100]
    };
    
    
    $scope.getStockWeaks = function(tableParams){
    	PLUMDO.OtherUtil.mergeTableParams($scope.queryParams, tableParams);

    	if($scope.queryParams.stockDateEnd == null || $scope.queryParams.stockDateEnd == ''){
    		$scope.queryParams.stockDateEnd = PLUMDO.DATEUTIL.getToday();
    	}
    	
    	if($scope.queryParams.stockDateBegin == null || $scope.queryParams.stockDateBegin == ''){
    		$scope.queryParams.stockDateBegin = PLUMDO.DATEUTIL.getDiffDay(-5,$scope.queryParams.stockDateEnd);
    	}
    	
    	if($scope.queryParams.stockRange == null || $scope.queryParams.stockRange == ''){
    		$scope.queryParams.stockRange = 0.01;
    	}
    	
    	StockReportService.getStockWeaks($scope.queryParams).success(function(data, status, header, config){
    		$scope.stockWeaks = data;
    	});
    };
    
    $scope.weakTableOptions = {
		id:'weakModel',
        data:'stockWeaks',
		colModels:[
           {name:'编码',index:'stockCode',sortable:true,width:'20%'},
           {name:'名称',index:'stockName',sortable:true,width:'20%'},
           {name:'板块类别',index:'stockType',sortable:true,width:'20%',
        	   formatter:function(){
					return '{{row.stockType=="sz"?"深圳":"上海"}}';
				}
           },
           {name:'',index:'',width:'10%',
       	   	formatter:function(){
					return '<button class="btn btn-info btn-xs" ng-click=toStockDetail(row.stockCode,row.stockName) type="button"><i class="fa fa-th-list"></i>&nbsp;详情</button>';
				}
          }
        ],
        queryParams:$scope.queryParams,
        loadFunction:$scope.getStockWeaks,
        sortName:'stockCode',
        sortOrder:'asc',
        pageSize:10,
        pageList:[10,25,50,100]
    };
    
    $scope.toStockDetail = function(stockCode,stockName,stockDate){
    	var stockDateBegin = null;
    	var stockDateEnd = null;
    	if(stockDate){
    		stockDateBegin = PLUMDO.DATEUTIL.getDiffDay(-3, stockDate);
    		stockDateEnd = stockDate;
    	}else{
    		stockDateBegin = $scope.queryParams.stockDateBegin;
    		stockDateEnd = $scope.queryParams.stockDateEnd;
    	}
   	 	$state.go('stock-report.weak.detail', {
   	 		queryParams:{
   	 			stockCode:stockCode,
 				stockName:stockName,
 				stockDateBegin:stockDateBegin,
 				stockDateEnd:stockDateEnd
   	 		}
   	 	});
   	 	
   }; 

}]);
