var EmbedPlugin = Plugin.extend({
    _type: 'embed',
    _isContainer: false,
    _render: false,
    initPlugin: function(data) {
        var instance = this;
        if (data.object) {
            if (data.object == 'template' && this._stage._stageController) {
                var templateId = this._stage._stageController.getTemplate();   
                var template = this._theme._templateMap[templateId];
                if (template && template.var && template.var != '') {
                    this._stage._templateVar = template.var;
                    for (k in template) {
                        if (PluginManager.isPlugin(k)) {
                            PluginManager.invoke(k, template[k], this._parent, this._stage, this._theme);
                        }
                    }   
                }
            }
        }
    }
});
PluginManager.registerPlugin('embed', EmbedPlugin);
