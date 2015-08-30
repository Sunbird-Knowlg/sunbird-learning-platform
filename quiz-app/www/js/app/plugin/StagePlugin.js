var StagePlugin = Plugin.extend({
    _type: 'stage',
    _repeat: 1,
    _render: true,
    _stageData: undefined,
    _choices: [],
    params: {},
    initPlugin: function(data) {
        var count = this._theme._stageRepeatCount[data.id] || 0;
        if (count <= 0) {
            count = 0;
        }
        this.getStageData(data, count);
        if (this._repeat <= 1) {
            this._theme._stageRepeatCount[data.id] = 1;
        } else {
            this._theme._stageRepeatCount[data.id] = count + 1;
        }
        var instance = this;
        this._self = new creatine.Scene();;
        var dims = this.relativeDims();
        this._self.x = dims.x;
        this._self.y = dims.y;
        for (k in data) {
            if(k === 'param') {
                if(_.isArray(data[k])) {
                    var instance = this;
                    data[k].forEach(function(param) {
                        instance.params[param.name] = param.value;
                    });
                } else {
                    this.params[data[k].name] = data[k].value;
                }
            }
        }
        for (k in data) {
            if (PluginManager.isPlugin(k)) {
                PluginManager.invoke(k, data[k], this, this, this._theme);
            }
        }
        if (!this._theme._assessmentData[this._data.id]) {
            this._theme._assessmentData[this._data.id] = {};
        }
        for (var i = 1; i <= this._repeat; i++) {
            if (!this._theme._assessmentData[this._data.id][i]) {
                this._theme._assessmentData[this._data.id][i] = 0;
            }
        }
    },
    getStageData: function(data, count) {
        if (this._theme._themeData) {
            var stageData = this._theme._themeData[data.id];
            if (stageData) {
                if (_.isArray(stageData) && stageData.length > 0) {
                    this._repeat = stageData.length;
                    if (count >= this._repeat) {
                        count = 0;
                    }
                    this._stageData = stageData[count];
                } else {
                    this._stageData = stageData;
                }
            }
        }
    },
    evaluate: function(action) {
        var dataItem = this._stageData;
        var valid = true;
        var evalFields = action.fields.split(',');
        evalFields.forEach(function(inputId) {
            if (valid) {
                var inputPlugIn = PluginManager.getPluginObject(inputId);
                if (inputPlugIn) {
                    var ansParam = inputPlugIn._data.param;
                    var expected = dataItem.answer[ansParam];
                    var actual = document.getElementById(inputId).value;
                    if (_.isObject(expected)) {
                        valid = _.isEqual(expected, actual);
                    } else {
                        valid = (expected == actual);
                    }
                } else {
                    valid = false;
                }
            }
        });
        if (valid) {
            this.dispatchEvent(action.success);
            var itemIndex = this._theme._stageRepeatCount[this._data.id];
            this._theme._assessmentData[this._data.id][itemIndex] = 1;
        } else {
            this.dispatchEvent(action.failure);
        }
    },
    reload: function(action) {
        var count = this._theme._stageRepeatCount[this._data.id];
        count -= 1;
        this._theme._stageRepeatCount[this._data.id] = count;
        this._theme.replaceStage(this._data.id, action);
    }
});
PluginManager.registerPlugin('stage', StagePlugin);
