var TemplatePlugin = Plugin.extend({
    _type: 'template',
    _isContainer: false,
    _render: false,
    initPlugin: function(data) {
        var instance = this;
        if (data.id && this._stage._stageController) {
            var template = this._stage._stageController.getTemplate();
            if (template && data.id === template) {
                for (k in data) {
                    if (PluginManager.isPlugin(k)) {
                        PluginManager.invoke(k, data[k], this._parent, this._stage, this._theme);
                    }
                }
            }
        }
    }
});
PluginManager.registerPlugin('template', TemplatePlugin);
