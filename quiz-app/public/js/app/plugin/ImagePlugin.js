var ImagePlugin = Plugin.extend({
    _type: 'image',
    _isContainer: false,
    _render: true,
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
    }
});
PluginManager.registerPlugin('image', ImagePlugin);