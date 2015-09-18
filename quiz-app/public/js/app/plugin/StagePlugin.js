var StagePlugin = Plugin.extend({
    _type: 'stage',
    _render: true,
    params: {},
    _stageController: undefined,
    _stageControllerName: undefined,
    _templateVars: {},
    _controllerMap: {},
    initPlugin: function(data) {
        var instance = this;
        this._self = new creatine.Scene();;
        var dims = this.relativeDims();
        this._self.x = dims.x;
        this._self.y = dims.y;
        if (data.iterate && data.var) {
            var controllerName = data.var.trim();
            var stageController = this._theme._controllerMap[data.iterate.trim()];
            if (stageController) {
                this._stageControllerName = controllerName;
                this._stageController = stageController;
                this._stageController.next();
            }
        }
        for (k in data) {
            if(k === 'param') {
                if(_.isArray(data[k])) {
                    var instance = this;
                    data[k].forEach(function(param) {
                        instance.setParamValue(param);
                    });
                } else {
                    this.setParamValue(data[k]);
                }
            } else if (k === 'controller') {
                if(_.isArray(data[k])) {
                    data[k].forEach(function(p) {
                        this.addController(p);
                    });
                } else {
                    this.addController(data[k]);
                }
            }
        }
        for (k in data) {
            if (PluginManager.isPlugin(k)) {
                PluginManager.invoke(k, data[k], this, this, this._theme);
            }
        }
    },
    setParamValue: function(p) {
        if (p.value) {
            this.params[p.name] = p.value;
        } else if (p.model) {
            this.params[p.name] = this.getModelValue(p.model);
        }
    },
    addController: function(p) {
        var controller = ControllerManager.get(p.type, p.id, this._theme.baseDir);
        if (controller) {
            this._controllerMap[p.name] = controller;
        }
    },
    getController: function(name) {
        var c;
        if (this._templateVars[name]) {
            name = this._templateVars[name];
        }
        if (this._stageControllerName === name) {
            c = this._stageController;
        } else if (this._controllerMap[name]) {
            c = this._controllerMap[name];
        } else if (this._theme._controllerMap[name]) {
            c = this._theme._controllerMap[name];
        }
        return c;
    },
    getTemplate: function(controller) {
        var c = this.getController(controller);
        var t;
        if (c) {
            t = c.getTemplate();   
        }
        return t;
    },
    getModelValue: function(param) {
        var val;
        if (param) {
            var tokens = param.split('.');
            if (tokens.length >= 2) {
                var name = tokens[0].trim();
                var idx = param.indexOf('.');
                var paramName = param.substring(idx+1);
                var controller = this.getController(name);
                if (controller) {
                    val = controller.getModelValue(paramName);
                }
            }
        }
        return val;
    },
    setModelValue: function(param, val) { 
        if (param) {
            var tokens = param.split('.');
            if (tokens.length >= 2) {
                var name = tokens[0].trim();
                var idx = param.indexOf('.');
                var paramName = param.substring(idx+1);
                var controller = this.getController(name);
                if (controller) {
                    val = controller.setModelValue(paramName, val);
                }
            }
        }
    },
    evaluate: function(action) {
        var valid = false;
        if (this._stageController) {
            var result = this._stageController.evalItem();
            if (result) {
                valid = result.pass;    
            }
        }
        if (valid) {
            this.dispatchEvent(action.success);
        } else {
            this.dispatchEvent(action.failure);
        }
    },
    reload: function(action) {
        if (this._stageController) {
            this._stageController.decrIndex(1);
        }
        this._theme.replaceStage(this._data.id, action);
    }
});
PluginManager.registerPlugin('stage', StagePlugin);
