AnimationManager = {
	animationsCache: {},
	handle: function(action, plugin) {
		var instance;
		if(action.asset) {
			instance = PluginManager.getPluginObject(action.asset);
		} else {
			instance = plugin;
		}
		var fn = AnimationManager.animationsCache[action.id];
		var to = action.to;
		if(!fn) {
			if(!_.isArray(to)) {
				to = [to];
			}
			fn = '(function() {return function(plugin){';
			fn += 'createjs.Tween.get(plugin, {override:true})';
			to.forEach(function(to) {
				var data = JSON.parse(to.__cdata);
				var relDims = instance.getRelativeDims(data);
				data.x = relDims.x;
				data.y = relDims.y;
				data.width = relDims.w;
				data.height = relDims.h;
				fn += '.to(' + JSON.stringify(data) + ',' + to.duration + ', createjs.Ease.' + to.ease + ')';
			});
			if(action.widthChangeEvent) {
				fn += '.addEventListener("change", ' + AnimationManager.getWidthHandler() + ')';
			}
			fn += '}})()';
			AnimationManager.animationsCache[action.id] = fn;
		}
		var animationFn = eval(fn);
        animationFn.apply(null, [instance._self]);
	},
	getWidthHandler: function() {
		return function(event) {
			var sb = plugin.getBounds();
	    	plugin.scaleY = plugin.height / sb.height;
	    	plugin.scaleX = plugin.width / sb.width;
		}
	}
}