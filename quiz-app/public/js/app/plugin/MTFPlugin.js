var MTFPlugin = Plugin.extend({
    _type: 'mtf',
    _isContainer: true,
    _render: true,
    _lhs_options: [],
    _rhs_options: [],
    _force: false,
    _controller: undefined,
    initPlugin: function(data) {
        var model = data.model;
        if (model) {
        	var controller = this._stage.getController(model);
        	if (controller) {
        		this._controller = controller;
                this._force = data.force;
                if ((typeof this._force) == 'undefined' || this._force == null) {
                    this._force = false;
                }
        		this._data.x = this._parent._data.x;
        		this._data.y = this._parent._data.y;
        		this._data.w = this._parent._data.w;
        		this._data.h = this._parent._data.h;
        		this._self = new createjs.Container();
				var dims = this.relativeDims();
        		this._self.x = dims.x;
        		this._self.y = dims.y;
        		for(k in data) {
        			if(PluginManager.isPlugin(k)) {
        				PluginManager.invoke(k, data[k], this, this._stage, this._theme);
        			}
        		}
        	}
        }
    },
    getLhsOption: function(index) {
        var option;
        this._lhs_options.forEach(function(opt) {
            if (opt._index == index) {
                option = opt;
            }
        });
        return option;
    },
    setAnswer: function(rhsOption, lhsIndex) {
        this._controller.setModelValue(rhsOption._model, lhsIndex, 'selected');
    }
});
PluginManager.registerPlugin('mtf', MTFPlugin);
