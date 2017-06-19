/*
	@license Angular Treeview version 0.1.6
	ⓒ 2013 AHN JAE-HA http://github.com/eu81273/angular.treeview
	License: MIT


	[TREE attribute]
	angular-treeview: the treeview directive
	tree-id : each tree's unique id.
	tree-model : the tree model on $scope.
	node-id : each node's id
	node-label : each node's label
	node-children: each node's children

	<div
		data-angular-treeview="true"
		data-tree-id="tree"
		data-tree-model="roleList"
		data-node-id="roleId"
		data-node-label="roleName"
		data-node-children="children" >
	</div>
*/

(function ( angular ) {
	'use strict';

	angular.module( 'ngTree', [] ).directive( 'ngTree', ['$compile', function( $compile ) {
		return {
			restrict: 'A',
			link: function ( scope, element, attrs ) {
				var conf = scope[attrs.ngTree];
				//tree id
				var treeId = conf.treeId;
				
				//tree model
				var treeModel = null;
				if(attrs.treeModel){
					treeModel = attrs.treeModel;
				}else{
					treeModel = conf.treeModel;
				}
				//node id
				var nodeId = conf.nodeId || 'id';

				//node label
				var nodeLabel = conf.nodeLabel || 'label';

				//children
				var nodeChildren = conf.nodeChildren || 'children';
				
				//expandedClass
				var expandedClass = conf.expandedClass || 'glyphicon glyphicon-minus';
				
				//collapsedClass
				var collapsedClass = conf.collapsedClass || 'glyphicon glyphicon-plus';
				
				var multiSelect = conf.multiSelect==undefined ? true:conf.multiSelect;
				
				var expandEvent = conf.expandEvent || function(){};
				
				var selectedEvent = conf.selectedEvent || function(){};
				
				var selectedNodes = conf.selectedNodes || [];
				
				var checkEvent = conf.checkEvent || function(){};
				
				var checkNodes = conf.checkNodes || [];

				//tree template
				var template =
					'<ul>' +
						'<li data-ng-repeat="node in ' + treeModel + '">' +
							'<i class="collapsed '+ collapsedClass +' " data-ng-show="!node.leaf && node.collapsed" data-ng-click="' + treeId + '.selectNodeHead(node)"></i>' +
							'<i class="expanded '+ expandedClass +' " data-ng-show="!node.leaf && !node.collapsed" data-ng-click="' + treeId + '.selectNodeHead(node)"></i>' +
							'<i class="normal {{node.iconCls}}" data-ng-show="node.leaf"></i> ' +
							'<span data-ng-class="node.selected"  data-ng-click="' + treeId + '.selectNodeLabel(node)">{{node.' + nodeLabel + '}}</span>' +
							'<input type="checkbox" data-ng-show="node.checkbox" data-ng-model="node.check" data-ng-change="' + treeId + '.checkNode(node)"/> ' +
							'<div data-ng-hide="node.collapsed" ng-tree="'+attrs.ngTree+'"  data-tree-model="node.' + nodeChildren + '" ></div>' +
						'</li>' +
					'</ul>';


				//check tree id, tree model
				if( treeId && treeModel ) {

					//root node
					if( conf.angularTreeview ) {
					
						//create tree object if not exists
						scope[treeId] = scope[treeId] || {};
						
						
						//if node head clicks,
						scope[treeId].selectNodeHead = scope[treeId].selectNodeHead || function( selectedNode ){

							//Collapse or Expand
							selectedNode.collapsed = !selectedNode.collapsed;
							
							expandEvent(selectedNode);
						};
						scope[treeId].checkNode = scope[treeId].checkNode || function( checkedNode ){
							if(checkedNode.check){
								checkNodes.push(checkedNode)
							}else{
								var index = checkNodes.indexOf(checkedNode);
								checkNodes.splice(index, 1);
							}
							checkEvent(checkedNode);
						}
						//if node label clicks,
						scope[treeId].selectNodeLabel = scope[treeId].selectNodeLabel || function( selectedNode ){

							//remove highlight from previous node
							if(!selectedNode.disableSelection){
								if(selectedNode.selected == undefined){
									if(!multiSelect){
										if(scope[treeId].currentNode && scope[treeId].currentNode.selected ) {
											scope[treeId].currentNode.selected = undefined;
											var index = selectedNodes.indexOf(scope[treeId].currentNode);
											selectedNodes.splice(index, 1);
										}
									}
									selectedNode.selected = 'selected';
									selectedNodes.push(selectedNode)
									selectedEvent(selectedNode);
									scope[treeId].currentNode = selectedNode;
								}else{
									selectedNode.selected = undefined; 
									var index = selectedNodes.indexOf(selectedNode);
									selectedNodes.splice(index, 1);
									scope[treeId].currentNode = null;
								}
							}
						};
					}

					//Rendering template.
					element.html('').append( $compile( template )( scope ) );
				}
			}
		};
	}]);
})( angular );
