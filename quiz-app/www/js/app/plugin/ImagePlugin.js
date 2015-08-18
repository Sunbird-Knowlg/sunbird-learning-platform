var ImagePlugin = Plugin.extend({
	_type: 'image',
	_isContainer: false,
	initPlugin: function(data) {
		var instance = this;
		var s = new createjs.Bitmap(this._theme.getAsset(data.asset));
		var dims = this.relativeDims();
	    var sb = s.getBounds();
	    s.x = dims.x;
	    s.y = dims.y;
	    if (dims.h && dims.h > 0) {
	        s.scaleY = dims.h / sb.height;
	    }
	    if (dims.w && dims.w > 0) {
	        s.scaleX = dims.w / sb.width;
	    }
	    this._self = s;
	    this._self.width=dims.w;
	    this._self.height=dims.h;
	    this.animate_on_show = data.animate_on_show;
	    if(data.animate) {
	    	this.animations = {};
	    	if(_.isArray(data.animate)) {
	    		data.animate.forEach(function(animate) {
	    			this.animations[animate.id] = {};
		    		if(animate.type == 'tween') {
		    			this.animations[animate.id].animateFn = this.getAnimationFn(animate, animate.to);
		    		}
	    		})
	    	} else {
	    		this.animations[data.animate.id] = {};
	    		if(data.animate.type == 'tween') {
	    			this.animations[data.animate.id].animateFn = this.getAnimationFn(data.animate, data.animate.to);
	    		}
	    	}
	    }
	    if (data.hide) {
	    	this._self.visible = false;
		}
		this.render();
	},
	getAnimationFn: function(animate, to) {
		var instance = this;
		if(!_.isArray(to)) {
			to = [to];
		}
		var fn = '(function() {return function(plugin){';
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
		if(animate.widthChangeEvent) {
			fn += '.addEventListener("change", ' + instance.getWidthHandler() + ')';
		}
		fn += '}})()';
		return fn;
	},
	getWidthHandler: function() {
		return function(event) {
			var sb = plugin.getBounds();
	    	plugin.scaleY = plugin.height / sb.height;
	    	plugin.scaleX = plugin.width / sb.width;
		}
	}
});
pluginManager.registerPlugin('image', ImagePlugin);