var SummaryPlugin = Plugin.extend({
    _type: 'summary',
    _isContainer: false,
    _render: false,
    initPlugin: function(data) {
        if (data.controller) {
            var controller = this.resolveParams(data.controller);
            var message;
            if (this._theme._controllerMap[controller]) {
                message = this._theme._controllerMap[controller].feedback();
            } else if (this._stage._stageControllerName === controller) {
                message = this._stage._stageController.feedback();
            } else if (this._stage._controllerMap[controller]) {
                message = this._stage._controllerMap[controller].feedback();
            }
            if (message) {
                if (message.type == 'text') {
                    this.renderTextSummary(message.asset, data);
                }
            }
        }
    },
    renderTextSummary: function(text, data) {
        data.$t = text;
        PluginManager.invoke('text', data, this._parent, this._stage, this._theme);
    }
});
PluginManager.registerPlugin('summary', SummaryPlugin);
