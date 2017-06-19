var PLUMDO = PLUMDO || {};

PLUMDO.URL = {

    getStockInfos: function(){
    	return PLUMDO.CONFIG.contextRoot + '/stock-infos';
    },
    createStockInfo: function(){
    	return PLUMDO.CONFIG.contextRoot + '/stock-infos';
    },
    getStockInfo: function(stockId){
    	return PLUMDO.CONFIG.contextRoot + '/stock-infos/'+stockId;
    },
    updateStockInfo: function(stockId){
    	return PLUMDO.CONFIG.contextRoot + '/stock-infos/'+stockId;
    },
    deleteStockInfo: function(stockId){
    	return PLUMDO.CONFIG.contextRoot + '/stock-infos/'+stockId;
    },
    getStockDetails: function(){
    	return PLUMDO.CONFIG.contextRoot + '/stock-details';
    },
    createStockDetail: function(){
    	return PLUMDO.CONFIG.contextRoot + '/stock-details';
    },
    getStockDetail: function(detailId){
    	return PLUMDO.CONFIG.contextRoot + '/stock-details/'+detailId;
    },
    updateStockDetail: function(detailId){
    	return PLUMDO.CONFIG.contextRoot + '/stock-details/'+detailId;
    },
    deleteStockDetail: function(detailId){
    	return PLUMDO.CONFIG.contextRoot + '/stock-details/'+detailId;
    },
    collectStockDetails: function(threadNum){
    	return PLUMDO.CONFIG.contextRoot + '/stock-details/collect?threadNum='+threadNum;
    },
    getStockGolds: function(){
    	return PLUMDO.CONFIG.contextRoot + '/stock-report/stock-golds';
    },
    getStockWeaks: function(){
    	return PLUMDO.CONFIG.contextRoot + '/stock-report/stock-weaks';
    },
	getStockHotPlates: function(){
		return PLUMDO.CONFIG.contextRoot + '/stock-hot-plates';
	},
	createStockHotPlate: function(){
		return PLUMDO.CONFIG.contextRoot + '/stock-hot-plates';
	},
	getStockHotPlate: function(hotPlateId){
		return PLUMDO.CONFIG.contextRoot + '/stock-hot-plates/'+hotPlateId;
	},
	updateStockHotPlate: function(hotPlateId){
		return PLUMDO.CONFIG.contextRoot + '/stock-hot-plates/'+hotPlateId;
	},
	deleteStockHotPlate: function(hotPlateId){
		return PLUMDO.CONFIG.contextRoot + '/stock-hot-plates/'+hotPlateId;
	},
	getStockMonsters: function(){
		return PLUMDO.CONFIG.contextRoot + '/stock-monsters';
	},
	createStockMonster: function(){
		return PLUMDO.CONFIG.contextRoot + '/stock-monsters';
	},
	getStockMonster: function(monsterId){
		return PLUMDO.CONFIG.contextRoot + '/stock-monsters/'+monsterId;
	},
	updateStockMonster: function(monsterId){
		return PLUMDO.CONFIG.contextRoot + '/stock-monsters/'+monsterId;
	},
	deleteStockMonster: function(monsterId){
		return PLUMDO.CONFIG.contextRoot + '/stock-monsters/'+monsterId;
	},
	getLotteryDetails: function(){
		return PLUMDO.CONFIG.contextRoot + '/lottery-details';
	},
	createLotteryDetail: function(){
		return PLUMDO.CONFIG.contextRoot + '/lottery-details';
	},
	getLotteryDetail: function(detailId){
		return PLUMDO.CONFIG.contextRoot + '/lottery-details/'+detailId;
	},
	updateLotteryDetail: function(detailId){
		return PLUMDO.CONFIG.contextRoot + '/lottery-details/'+detailId;
	},
	deleteLotteryDetail: function(detailId){
		return PLUMDO.CONFIG.contextRoot + '/lottery-details/'+detailId;
	}
};