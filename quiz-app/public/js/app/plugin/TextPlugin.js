var TextPlugin = Plugin.extend({
	_type: 'text',
	_isContainer: false,
	initPlugin: function(data) {
		var instance = this;
		var fontsize = data.fontsize || 20;
		var dims = this.relativeDims();
		if (data.width) {
	    	var exp = data.width;	
	    	var cw = this._parent.dimensions().w;
	    	var width = parseFloat(cw * data.w / 100);
	    	var scale = parseFloat(width/exp);
	    	fontsize = parseFloat(fontsize * scale);
	    }
	    var font = fontsize + 'px ' + data.font || 'Arial';
		var text = new createjs.Text(data.$t || '', font, data.color || '#000000');
	    text.x = dims.x;
	    text.y = dims.y;
	    text.lineWidth = dims.w;
	    text.textAlign = 'left';
	    text.textBaseline = 'middle';
	    this._self = text;
	    this.render();
	}
});
pluginManager.registerPlugin('text', TextPlugin);