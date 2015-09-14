var EmbedPlugin = Plugin.extend({
    _type: 'embed',
    _isContainer: false,
    _render: false,
    initPlugin: function(data) {
        var instance = this;
        if (data.template) {
            var templateId = this._stage.getTemplate(data.template);
            var template = this._theme._templateMap[templateId];
            if (template) {
                for (var k in data) {
                    if (k === 'template') continue;
                    if (k.startsWith('var-')) {
                        this._stage._templateVars[k.substring(4)] = data[k];
                    } else {
                        this._stage._templateVars[k] = data[k];
                    }
                }
                for (k in template) {
                    if (PluginManager.isPlugin(k)) {
                        PluginManager.invoke(k, template[k], this._parent, this._stage, this._theme);
                    }
                }
            }
        }
    }
});
PluginManager.registerPlugin('embed', EmbedPlugin);
