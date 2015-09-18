var MCQPlugin = Plugin.extend({
    _type: 'mcq',
    _isContainer: true,
    _render: true,
    _multi_select: false,
    _options: [],
    _controller: undefined,
    initPlugin: function(data) {
        var model = data.model;
        if (model) {
        	var controller = this._stage.getController(model);
        	if (controller) {
        		this._controller = controller;
        		this._multi_select = data.multi_select;
        		if ((typeof this._multi_select) == 'undefined' || this._multi_select == null) {
        			this._multi_select = false;
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
    selectOption: function(option) {
    	var controller = this._controller;
    	if (!this._multi_select) {
    		this._options.forEach(function(o) {
    			if (o._index != option._index && typeof o._self.shadow != 'undefined') {
    				o.removeShadow();
    				controller.setModelValue(o._model, false, 'selected');
    			}
            });
    	}
    	var val = false;
    	if (option._self.shadow) {
            option._self.shadow = undefined;
        } else {
            option._self.shadow = new createjs.Shadow('#0470D8', 0, 0, 30);
            val = true;
        }
        controller.setModelValue(option._model, val, 'selected');
        Renderer.update = true;

    }
});
PluginManager.registerPlugin('mcq', MCQPlugin);
