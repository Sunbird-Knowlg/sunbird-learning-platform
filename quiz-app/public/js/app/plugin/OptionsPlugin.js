var OptionsPlugin = Plugin.extend({
    _type: 'options',
    _isContainer: false,
    _render: false,
    initPlugin: function(data) {
        var model = data.options;
        var value = undefined;
        if (this._parent._controller && model) {
        	value = this._parent._controller.getModelValue(model);
        }
        var layout = data.layout;
        if (value && _.isArray(value) && value.length > 0) {
        	if (layout === 'table' && _.isFinite(data.cols)) {
        		this.renderTableLayout(value);
        	}
        }
    },
    renderTableLayout: function(value) {
    	var count = value.length;
    	var cols = this._data.cols;
    	var rows = Math.ceil(count/cols);
    	var instance = this;
    	var marginX = 0;
    	if (_.isFinite(this._data.marginX)) {
    		marginX = this._data.marginX;
    	}
    	var marginY = 0;
    	if (_.isFinite(this._data.marginY)) {
    		marginY = this._data.marginY;
    	}
    	var padX = this._data.padX || 0;
        var padY = this._data.padY || 0;
    	var cw = (this._data.w - ((cols-1) * marginX))/cols;
    	var ch = (this._data.h - ((rows-1) * marginY))/rows;
    	var index = 0;
    	for (var r=0; r<rows; r++) {
    		for (var c=0; c<cols; c++) {
    			if (c*r < count) {
    				var data = {};
    				data.x = instance._data.x + (c * (cw + marginX));
    				data.y = instance._data.y + (r * (ch + marginY));
    				data.w = cw;
    				data.h = ch;
    				data.padX = padX;
                    data.padY = padY;
                    data.snapX = instance._data.snapX;
                    data.snapY = instance._data.snapY;
    				data.option = instance._data.options + '[' + index + ']';
    				index = index + 1;
    				PluginManager.invoke('option', data, instance._parent, instance._stage, instance._theme);
    			}
    		}
    	}
    }
});
PluginManager.registerPlugin('options', OptionsPlugin);
