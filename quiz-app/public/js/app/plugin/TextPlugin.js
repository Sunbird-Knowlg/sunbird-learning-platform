var TextPlugin = Plugin.extend({
    _type: 'text',
    _isContainer: false,
    _render: true,
    initPlugin: function(data) {
        var instance = this;
        var fontsize = data.fontsize || 20;
        var dims = this.relativeDims();
        if (data.w) {
            var exp = parseFloat(PluginManager.defaultResWidth * data.w / 100);
            var cw = this._parent.dimensions().w;
            var width = parseFloat(cw * data.w / 100);
            var scale = parseFloat(width / exp);
            fontsize = parseFloat(fontsize * scale);
        }
        var font = fontsize + 'px ' + data.font || 'Arial';
        if (data.weight) {
            font = data.weight + ' ' + font;
        }
        var textStr = '';
        if (data.$t || data.__text) {
            textStr = (data.$t || data.__text);
        } else if (data.model) {
            textStr = (this._stage.getModelValue(data.model) || '');
        } else if (data.param) {
            textStr = (this._stage.params[data.param.trim()] || '');
        }
        var text = new createjs.Text(textStr, font, data.color || '#000000');
        text.x = dims.x;
        text.y = dims.y;
        text.lineWidth = dims.w;
        text.textAlign = 'left';
        text.textBaseline = 'middle';
        this._self = text;
    }
});
PluginManager.registerPlugin('text', TextPlugin);
