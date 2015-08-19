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
		if(data.type == 'choice') {
			this._stage._choices.push(this);
		}
		this.render();
	},
	getWidthHandler: function() {
		return function(event) {
			var sb = plugin.getBounds();
	    	plugin.scaleY = plugin.height / sb.height;
	    	plugin.scaleX = plugin.width / sb.width;
		}
	},
	toggleShadow: function() {
		if(this._self.shadow) {
			this._self.shadow = undefined;
		} else {
			this._self.shadow = new createjs.Shadow(this._data.shadowColor, 0, 0, 30);
		}
	},
	removeShadow: function() {
		this._self.shadow = undefined;
	}
});
pluginManager.registerPlugin('image', ImagePlugin);