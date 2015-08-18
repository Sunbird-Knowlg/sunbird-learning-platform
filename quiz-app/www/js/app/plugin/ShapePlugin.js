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
    			graphics.dr(0, 0, dims.w, dims.h);
    			if(data.hitArea) {
		    		var hit = new createjs.Shape();
					hit.graphics.beginFill("#000").r(0, 0, dims.w, dims.h);
					this._self.hitArea = hit;
		    	}
    			break;
    		case 'circle':
    			graphics.dc(0, 0, dims.w);
    			if(data.hitArea) {
		    		var hit = new createjs.Shape();
					hit.graphics.beginFill("#000").dc(0, 0, dims.w);
					this._self.hitArea = hit;
		    	}
    			break;
    		default:
    	}
        this._self.x = dims.x;
        this._self.y = dims.y;
        this._self.regX = dims.w/2;
        this._self.regY = dims.h/2;
        if(data.rotate) {
            this._self.rotation = data.rotate;
        }
    	this.render();
    }
});
pluginManager.registerPlugin('shape', ShapePlugin);