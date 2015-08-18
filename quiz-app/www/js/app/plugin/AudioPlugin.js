var AudioPlugin = Plugin.extend({
	_type: 'audio',
	_isContainer: false,
	initPlugin: function(data) {
            var instance = this;
            var s = null;
            if(data.parentasset) {
                var pluginObj = pluginManager.getPluginObject(data.parentasset);
                if(pluginObj == null) {
                    console.log('No plugin object:'+data.parentasset);
                }
                s = pluginObj._self;
            } else {
                s = new createjs.Bitmap(this._theme.getAsset('sound_image'));
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
            }
            // createjs.Sound.registerSound("assets/thunder.mp3", data.asset);
            var events = (data.event)?data.event.split(",") : ["click"];
            for (var i = 0; i < events.length; i++) {
                s.addEventListener(events[i], function(event) {
                    createjs.Sound.play(data.asset, createjs.Sound.INTERRUPT_NONE);
                });
            };
            if(!data.parentasset) {
                this._self = s;
                this.render();
            }
	}
});
pluginManager.registerPlugin('audio', AudioPlugin);