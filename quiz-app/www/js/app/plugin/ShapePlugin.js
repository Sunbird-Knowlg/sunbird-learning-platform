var ShapePlugin = Plugin.extend({
	_isContainer: false,
    initPlugin: function(data) {
    	this._self = new createjs.Shape();
    	var graphics = this._self.graphics;
    	var dims = this.relativeDims();
		if(data.fill) {
			graphics.beginFill(data.fill);
		}
		if(data.stroke) {
			graphics.beginStroke(data.stroke);
		}
    	switch(data.type) {
    		case 'rect':
    			graphics.dr(dims.x, dims.y, dims.w, dims.h);
    			if(data.hitArea) {
		    		var hit = new createjs.Shape();
					hit.graphics.beginFill("#000").r(dims.x, dims.y, dims.w, dims.h);
					this._self.hitArea = hit;
		    	}
    			break;
    		case 'circle':
    			graphics.dc(dims.x, dims.y, dims.w);
    			if(data.hitArea) {
		    		var hit = new createjs.Shape();
					hit.graphics.beginFill("#000").dc(dims.x, dims.y, dims.w);
					this._self.hitArea = hit;
		    	}
    			break;
    		default:
    	}
    	this.render();
    }
});
pluginManager.registerPlugin('shape', ShapePlugin);