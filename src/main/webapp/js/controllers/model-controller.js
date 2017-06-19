/**
 * 模型数据控制层
 * 
 * @author wengwh
 * @Date 2017-02-06
 * 
 */
angular.module('plumdo.controllers').controller('ModelCtrl',['$scope','ModelService','$uibModal','$state', function($scope,ModelService,$uibModal,$state) { 
	$scope.models = {};
	$scope.queryParams = {};

	$scope.queryModels = function(tableParams){
		PLUMDO.OtherUtil.mergeTableParams($scope.queryParams, tableParams);

		ModelService.getModels($scope.queryParams).success(function(data, status, header, config){
			$scope.models = data;
		});
	};

	$scope.tableOptions = {
		id:'model',
		data:'models',
		colModels:[
			{name:'名称',index:'name',sortable:true,width:'10%'},
			{name:'标识',index:'key',sortable:true,width:'10%'},
			{name:'分类',index:'category',sortable:true,width:'10%'},
			{name:'版本号',index:'version',sortable:true,width:'7%'},
			{name:'创建时间',index:'createTime',sortable:true,width:'15%'},
			{name:'修改时间',index:'lastUpdateTime',sortable:true,width:'15%'},
			{name:'租户ID',index:'tenantId',sortable:true,width:'7%'},
			{name:'操作',index:'',width:'25%',
				formatter:function(){
					return '<div class="btn-group">'+
					'<button class="btn btn-info btn-xs" ng-click=designModel(row.id) type="button"><i class="fa fa-cogs"></i>&nbsp;设计</button>'+
					'<button class="btn btn-primary btn-xs" ng-click=openModal(row.id) type="button"><i class="fa fa-pencil"></i>&nbsp;修改</button>'+
					'<button class="btn btn-success btn-xs" ng-click=deployModel(row.id) type="button"><i class="fa fa-credit-card"></i>&nbsp;部署</button>'+
					'<button class="btn btn-danger btn-xs" ng-click=deleteModel(row.id) type="button"><i class="fa fa-trash-o"></i>&nbsp;删除</button>'+
					'</div>';
				}
			}
		],
		loadFunction:$scope.queryModels,
		queryParams:$scope.queryParams,
		sortName:'name',
		sortOrder:'asc',
		pageSize:10,
		pageList:[10,25,50,100]
	};

	$scope.designModel = function(modelId){
		window.open(PLUMDO.URL.designModel(modelId));
	};
	
	$scope.deleteModel = function(modelId){
		$scope.confirmModal({
			title:'确认删除模型',
			confirm:function(isConfirm){
				if(isConfirm){
					ModelService.deleteModel(modelId).success(function(data, status, header, config){
						$scope.showSuccessMsg('删除模型成功');
						$scope.queryModels();
					});
				}
			}
		});
	};
	
	$scope.deployModel = function(modelId){
		$scope.confirmModal({
			title:'确认部署模型',
			confirm:function(isConfirm){
				if(isConfirm){
					ModelService.deployModel(modelId).success(function(data, status, header, config){
						$scope.showSuccessMsg('部署模型成功');
						$scope.queryModels();
					});
				}
			}
		});
	};

	$scope.openModal = function (modelId) {
		$scope.modelId = modelId;
		$uibModal.open({
			templateUrl: 'views/model/edit.html',
			controller: 'ModelModalCtrl',
			scope: $scope
		});
	};

}]);

angular.module('plumdo.controllers').controller('ModelModalCtrl',['$scope','ModelService','$uibModalInstance', function($scope,ModelService,$uibModalInstance) { 
	$scope.formdata = {};
	
	$scope.covertMetaInfo = function(){
		$scope.formdata.metaInfo=angular.toJson({"name":$scope.formdata.name,"version":$scope.formdata.version,"description":$scope.formdata.description});
	};
	
	if($scope.modelId){
		$scope.modalTitle='修改模型';

		ModelService.getModel($scope.modelId).success(function(data){
			$scope.formdata = data;
		});

		$scope.ok = function () {
			$scope.covertMetaInfo();
			ModelService.updateModel($scope.modelId,$scope.formdata).success(function(data){
				$uibModalInstance.close();
				$scope.showSuccessMsg('修改模型成功');
				$scope.queryModels();
			});
		};

	}else{
		$scope.modalTitle='添加模型';
		$scope.ok = function () {
			$scope.covertMetaInfo();
			ModelService.createModel($scope.formdata).success(function(data){
				$uibModalInstance.close();
				$scope.showSuccessMsg('添加模型成功');
				$scope.queryModels();
			});
		};
	}

	$scope.cancel = function () {
		$uibModalInstance.dismiss('cancel');
	};
}]);