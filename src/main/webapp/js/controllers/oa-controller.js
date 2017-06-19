/**
 * 请假单数据控制层
 * 
 * @author wengwh
 * @Date 2017-02-06
 * 
 */
angular.module('plumdo.controllers').controller('OaCtrl',['$scope','OaService','$uibModal','$state', function($scope,OaService,$uibModal,$state) { 
	$scope.models = {};
	$scope.queryParams = {};

	$scope.queryOas = function(tableParams){
		PLUMDO.OtherUtil.mergeTableParams($scope.queryParams, tableParams);

		OaService.getOas($scope.queryParams).success(function(data, status, header, config){
			$scope.models = {};
			$scope.models.data = data;
		});
	};

	$scope.tableOptions = {
		id:'model',
		data:'models',
		colModels:[
			{name:'申请人ID',index:'applyUserId',sortable:false,width:'7%'},
			{name:'申请理由',index:'applyReason',sortable:false,width:'15%'},
			{name:'创建时间',index:'createTime',sortable:false,width:'15%'},
			{name:'结束时间',index:'endTime',sortable:false,width:'15%'},
			{name:'流程实例ID',index:'processInstanceId',sortable:false,width:'10%'},
			{name:'当前节点',index:'taskName',sortable:false,width:'10%'},
			{name:'操作',index:'',width:'25%',
				formatter:function(){
					return '<div class="btn-group">'+
					'<button class="btn btn-info btn-xs" ng-click=completeOa(row.id) type="button"><i class="fa fa-cogs"></i>&nbsp;完成任务</button>'+
					'<button class="btn btn-primary btn-xs" ng-click=returnOa(row.id) type="button"><i class="fa fa-pencil"></i>&nbsp;回退任务</button>'+
					'<button class="btn btn-success btn-xs" ng-click=flowImage(row.processInstanceId) type="button"><i class="fa fa-trash-o"></i>&nbsp;流程图</button>'+
					'</div>';
				}
			}
		],
		loadFunction:$scope.queryOas,
		queryParams:$scope.queryParams,
	};

	$scope.flowImage = function(processInstanceId){
		window.open(PLUMDO.URL.processImage(processInstanceId));
	};
	
	$scope.completeOa = function(oaId){
		$scope.confirmModal({
			title:'确认完成任务',
			confirm:function(isConfirm){
				if(isConfirm){
					OaService.completeOa(oaId).success(function(data, status, header, config){
						$scope.showSuccessMsg('完成任务成功');
						$scope.queryOas();
					});
				}
			}
		});
	};
	
	$scope.returnOa = function(oaId){
		$scope.confirmModal({
			title:'确认回退任务',
			confirm:function(isConfirm){
				if(isConfirm){
					OaService.returnOa(oaId).success(function(data, status, header, config){
						$scope.showSuccessMsg('回退任务成功');
						$scope.queryOas();
					});
				}
			}
		});
	};
	

	$scope.openModal = function (oaId) {
		$scope.oaId = oaId;
		$uibModal.open({
			templateUrl: 'views/oa/edit.html',
			controller: 'OaModalCtrl',
			scope: $scope
		});
	};

}]);

angular.module('plumdo.controllers').controller('OaModalCtrl',['$scope','OaService','$uibModalInstance', function($scope,OaService,$uibModalInstance) { 
	$scope.formdata = {};
	
	$scope.modalTitle='添加请假单';
	$scope.ok = function () {
		OaService.startOa($scope.formdata).success(function(data){
			$uibModalInstance.close();
			$scope.showSuccessMsg('添加请假单成功');
			$scope.queryOas();
		});
	};

	$scope.cancel = function () {
		$uibModalInstance.dismiss('cancel');
	};
}]);