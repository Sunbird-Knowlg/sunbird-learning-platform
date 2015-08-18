var AudioPlugin = Plugin.extend({
	_type: 'audio',
	_isContainer: false,
	initPlugin: function(data) {
            var instance = this;
            var s = new createjs.Bitmap(this._theme.getAsset('sound_image'));
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
            // createjs.Sound.registerSound("assets/thunder.mp3", data.asset);
            var eventName = data.event || "click";
            var asset = this._theme.getAsset(data.asset);
            s.addEventListener(eventName, function(event) {
                createjs.Sound.play(data.asset);
            });
            this._self = s;
            if (!data.hide) {
                this.render();
            }
	}
});
pluginManager.registerPlugin('audio', AudioPlugin);