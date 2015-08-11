//<image asset="spaceship" onclick="play(sound-launch);raise(EVT_NEXT)" enableTelemetry="True"/>
var ImagePlugin = Plugin.extend({
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
	    this.render();
	    if(data.onclick) {
	    	this._self.cursor = "pointer";
	    	//raise click event
	    	this._self.on('click', function() {
	    		console.log('Dispatching event - ', data.onclick);
	    		instance._parent.dispatchEvent(data.onclick);
	    	});
	    }
	}
});
pluginManager.registerPlugin('image', ImagePlugin);
createjs.EventDispatcher.initialize(ImagePlugin.prototype);