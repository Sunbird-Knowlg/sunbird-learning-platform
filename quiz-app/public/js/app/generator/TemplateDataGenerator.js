var TemplateDataGenerator = {	
    _getItems: function(data) {
    	var list = [];
    	if (_.isObject(data)) {
    		var total_items = data.total_items;
			var item_sets = data.item_sets;
			var items = data.items;
			item_sets.forEach(function(map) {
				list = TemplateDataGenerator._selectTemplate(map.id, map.count, items);
			});
    	}		
		return list;
		
	},
	_selectTemplate: function(id, count, items, list) {
		var set = items[id];
		if(set.length == count)	{
			for(var i = 0; i < count; i++)
				list = TemplateDataGenerator._generateItems(set[i], 1, list);
		}		
		if(count > set.length){
			var tempArr = [];
			for(var i = 0; i < set.length; i++)
				list = TemplateDataGenerator._generateItems(set[i], 1, list);
			for(var i = 0; i < count - set.length; i++) {
				var randNum = _.random(0, set.length - 1);
				list = TemplateDataGenerator._generateItems(set[randNum], 1, list) 
			}
		}
		if(count < set.length) {
			for(var i = 0; i < count; i++){
				var randNum = _.random(0, set.length - 1);
				list = TemplateDataGenerator._generateItems(set[randNum], 1, list)
			}
		}
		return list;
	},
	_generateItems: function(pick, count, list) {
		var itemArr = [];		
		if(pick.parameterized){
			for(var i = 0; i < count; i++)
				itemArr[i] = JSON.stringify(pick);
			for(var i = 0; i < count; i++)
				itemArr[i] = JSON.parse(itemArr[i]);
			for(var i = 0; i < count; i++) {		
				if(itemArr[i].model){
					var length = itemArr[i].model.length;			
					_.map(itemArr[i].model, function(num, key) {
						_.map(itemArr[i].model[key], function(num, key1){
							var values = [];						
					    	if(_.isObject(num)){
					    		var j = 0;
					    		_.map(num, function(value, key2){	
					    			values[j++] = value;
					    		});	
					    		var temp =  _.random(values[0], values[1]);
					    		itemArr[i].model[key][key1] = temp;	
					    	}
					    	if(_.isString(num)){				    		
					    		if(num.substring(0,1) == '$') {
				    			itemArr[i].model[key][key1] = TemplateDataGenerator._computeExpression(num, itemArr, i);
					    		}
					    	}
					 	});
					    
					});				
				}
				_.map(itemArr[i].answer, function(num, key){
					_.map(itemArr[i].answer[key], function(num, key1){					
				    	if(_.isString(num)){				    		
				    		if(num.substring(0,1) == '$') {			
				    			itemArr[i].answer[key][key1] = TemplateDataGenerator._computeExpression(num, itemArr, i);;
				    		}
				    	}
				 	});
				    
				});
			}
		}		
		list = _.union(list, itemArr);
		return list;
	},	
	_computeExpression: function(str, itemArr, i) {
		var paramList = [];
		var tempArr = TemplateDataGenerator._splitByOperator(str.substring(2, str.length-1));
		var fn = '(function()  {return function(';
		for(var k = 0; k < tempArr.length; k++){
			fn += tempArr[k] +','
			paramList.push(itemArr[i].model[tempArr[k]]);
		}
		fn = fn.substring(0, fn.length - 1) + '){return(' + str.substring(2, str.length-1) + ')}})()';
		return eval(fn).apply(null, paramList);
	},
	_splitByOperator: function(str){  
		var matchPattern = /[^\s()*/%+-]+/g;  
		var splitPattern = /[\s()*/%+-]+/g;  		   
		var temp =  str.match(matchPattern); 
		var tempArr = [];
		for(var i = 0; i < temp.length ; i++){
			var temp1 = temp[i].split('.');
			tempArr.push(temp1[0]);
		}
		return tempArr;
	}
}
