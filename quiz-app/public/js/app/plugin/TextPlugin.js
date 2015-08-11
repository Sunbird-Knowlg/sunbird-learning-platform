var TextPlugin = Plugin.extend({
	initPlugin: function(data) {
		console.log('text: ' + data.$t);
		var instance = this;
		var text = new createjs.Text(data.$t || '', data.font || '20px Arial', data.color || '#000000');
		var dims = this.relativeDims();
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