var TemplateDataGenerator = {	
    _getItems: function(data) {
    	var list = [];
    	if (_.isObject(data)) {
    		var total_items = data.total_items;
			var item_sets = data.item_sets;
			var items = data.items;
			if (item_sets && items) {
				item_sets.forEach(function(map) {
					list = TemplateDataGenerator._selectTemplate(map.id, map.count, items, list);
				});
				if (total_items && list.length > total_items) {
					list = _.first(list, total_items);
				}
			}
    	}		
		return list;
	},
	_selectTemplate:  function(id, count, items, list) {
		var set = items[id];
	// select randombly without any assumptions
		for(var i = 0; i < count; i++){
			var randNum = _.random(0, set.length - 1);
			list = TemplateDataGenerator._generateItems(set[randNum], list)
		}
	// select randombly with some assumptions
		// if(set.length == count)	{
		// 	for(var i = 0; i < count; i++)
		// 		list = TemplateDataGenerator._generateItems(set[i], list);
		// }		
		// if(count > set.length){
		// 	var tempArr = [];
		// 	for(var i = 0; i < set.length; i++)
		// 		list = TemplateDataGenerator._generateItems(set[i], list);
		// 	for(var i = 0; i < count - set.length; i++) {
		// 		var randNum = _.random(0, set.length - 1);
		// 		list = TemplateDataGenerator._generateItems(set[randNum], list) 
		// 	}
		// }
		// if(count < set.length) {
		// 	for(var i = 0; i < count; i++){
		// 		var randNum = _.random(0, set.length - 1);
		// 		list = TemplateDataGenerator._generateItems(set[randNum], list)
		// 	}
		// }
		return list;
	},
	_generateItems: function(pick, list) {
		var item, tempItem;		
		if(pick.parameterized) {
			item = JSON.stringify(pick);
			item = JSON.parse(item);
			tempItem = JSON.stringify(pick);
			tempItem = JSON.parse(tempItem);
			if(item.model)						
				item = TemplateDataGenerator._getParamsData(item, tempItem);
			if(item.restrictions)
				while(!(TemplateDataGenerator._applyRestrictions(item)))
					item = TemplateDataGenerator._getParamsData(item, tempItem);
			if(item.answer) {
				_.map(item.answer, function(param, key) {
					_.map(item.answer[key], function(obj, key1){					
				    	if(_.isString(obj)){				    		
				    		if(obj.substring(0,1) == '$') 		
				    			item.answer[key][key1] = TemplateDataGenerator._computeExpression(obj, item);
				    	}
				 	});
				    
				});
			}
		}
		delete item["restrictions"];
		list.push(item);
		return list;
	},	
	_getParamsData: function(item,tempItem) {
		if(tempItem.model){			
			_.map(tempItem.model, function(param, key) {
				_.map(tempItem.model[key], function(count, key1){
		    		if(_.isObject(count))
		    			item.model[key][key1] = _.random(count.min, count.max);
			    	if(_.isString(count)){				    		
			    		if(count.substring(0,1) == '$')
		    				item.model[key][key1] = TemplateDataGenerator._computeExpression(count, item);
			    	}
			 	});
			    
			});				
		}
		return item;
	},
	_applyRestrictions: function(item) {
		var bool = true;
		if(item.restrictions) {
			_.each(item.restrictions, function(str) {
				var tempArr = TemplateDataGenerator._splitByOperator(str.substring(2, str.length - 1));
				var paramList = [];
				var fn = '(function()  {return function('; 
				for(var j = 0; j < tempArr.length; j = j + 2){
					if(!parseInt(tempArr[j])) {
						fn += tempArr[j] + ',';
						paramList.push(item.model[tempArr[j]]);
					}
				}
				fn = fn.substring(0, fn.length - 1) + '){return(' + str.substring(2, str.length-1) + ')}})()';
				bool *= (eval(fn).apply(null, paramList));								
			});
		}
		return bool;
	},
	_computeExpression: function(str, item) {
		var paramList = [];
		var tempArr = TemplateDataGenerator._splitByOperator(str.substring(2, str.length-1));
		var fn = '(function()  {return function(';
		for(var k = 0; k < tempArr.length; k++){
			fn += tempArr[k] +','
			paramList.push(item.model[tempArr[k]]);
		}
		fn = fn.substring(0, fn.length - 1) + '){return(' + str.substring(2, str.length-1) + ')}})()';
		return eval(fn).apply(null, paramList);
	},
	_splitByOperator: function(str){  
		var matchPattern = /[^\s()*/%+-]+/g;  
		//var splitPattern = /[\s()*/%+-]+/g;  		   
		var temp =  str.match(matchPattern); 
		var tempArr = [];
		for(var i = 0; i < temp.length ; i++){
			var temp1 = temp[i].split('.');
			tempArr.push(temp1[0]);
		}
		return tempArr;
	}
}
