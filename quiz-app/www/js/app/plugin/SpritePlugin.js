var SpritePlugin = Plugin.extend({
	_isContainer: false,
	initPlugin: function(data) {
		var dims = this.relativeDims();
		var spritesheet = this._theme.getAsset(data.asset);
		var grant = new createjs.Sprite(spritesheet, data.start);
		grant.x = dims.x;
		grant.y = dims.y;
		this._self = grant;
	    if (!data.hide) {
	    	this.render();
		}
	},
	playAnimation: function(animation) {
		this._self.gotoAndPlay(animation);
	}
});
pluginManager.registerPlugin('sprite', SpritePlugin);