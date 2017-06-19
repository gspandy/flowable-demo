var PLUMDO = PLUMDO || {};

PLUMDO.URL = {

    getModels: function(){
    	return PLUMDO.CONFIG.contextRoot + '/model';
    },
    createModel: function(){
    	return PLUMDO.CONFIG.contextRoot + '/model';
    },
    getModel: function(modelId){
    	return PLUMDO.CONFIG.contextRoot + '/model/'+modelId;
    },
    updateModel: function(modelId){
    	return PLUMDO.CONFIG.contextRoot + '/model/'+modelId;
    },
    deleteModel: function(modelId){
    	return PLUMDO.CONFIG.contextRoot + '/model/'+modelId;
    },
    deployModel: function(modelId){
    	return PLUMDO.CONFIG.contextRoot + '/model/'+modelId+'/deploy';
    },
    designModel: function(modelId){
    	return PLUMDO.CONFIG.contextRoot + '/modeler/index.html?modelId='+modelId;
    },
    processImage: function(processInstanceId){
    	return PLUMDO.CONFIG.contextRoot + '/process-instance/'+processInstanceId+'/image';
    },
    getOas: function(){
    	return PLUMDO.CONFIG.contextRoot + '/oa';
    },
    startOa: function(){
    	return PLUMDO.CONFIG.contextRoot + '/oa/start';
    },
    getOa: function(oaId){
    	return PLUMDO.CONFIG.contextRoot + '/oa/'+oaId;
    },
    completeOa: function(oaId){
    	return PLUMDO.CONFIG.contextRoot + '/oa/'+oaId+'/complete';
    },
    returnOa: function(oaId){
    	return PLUMDO.CONFIG.contextRoot + '/oa/'+oaId+'/return';
    }
};