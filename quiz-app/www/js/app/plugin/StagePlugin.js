var StagePlugin = Plugin.extend({
    _type: 'stage',
    _repeat: 1,
    _stageData: undefined,
    initPlugin: function(data) {
        var count = this._theme._stageRepeatCount[data.id] || 0;
        if (count <= 0) {
            count = 0;
        }
        this._theme._stageRepeatCount[data.id] = count + 1;
        this.getStageData(data, count);
        var instance = this;
        this._self = new creatine.Scene();;
        var dims = this.relativeDims();
        this._self.x = dims.x;
        this._self.y = dims.y;
        this.render();

        for (k in data) {
            if (pluginManager.isPlugin(k)) {
                pluginManager.invoke(k, data[k], this, this, this._theme);
            } else {
                // Handle plugin specific data like animations, events
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
    registerEvent: function(instance, eventData) {
        if (eventData.transition) {
            instance.on(eventData.on, function(event) {
                var count = instance._theme._stageRepeatCount[instance._data.id];
                if (eventData.on == 'previous') {
                    count -= 2;
                    instance._theme._stageRepeatCount[instance._data.id] = count;
                }
                if (count >= instance._repeat) {
                    instance._theme.replaceStage(this._self, eventData.transition, eventData);
                } else {
                    instance._theme.replaceStage(this._self, instance._data.id, eventData);
                }
            });
        } else if (eventData.eval) {
            if (!instance._theme._assessmentData[instance._data.id]) {
                instance._theme._assessmentData[instance._data.id] = {};
            }
            for (var i = 1; i <= instance._repeat; i++) {
                if (!instance._theme._assessmentData[instance._data.id][i]) {
                    instance._theme._assessmentData[instance._data.id][i] = 0;
                }
            }
            instance.on(eventData.on, function(event) {
                var dataItem = instance._stageData;
                var valid = true;
                var evalFields = eventData.eval.split(',');
                evalFields.forEach(function(inputId) {
                    if (valid) {
                        var inputPlugIn = pluginManager.getPluginObject(inputId);
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
                    instance.dispatchEvent(eventData.success);
                    var itemIndex = instance._theme._stageRepeatCount[instance._data.id];
                    instance._theme._assessmentData[instance._data.id][itemIndex] = 1;
                } else {
                    instance.dispatchEvent(eventData.failure);
                }
            });
        } else if (eventData.show || eventData.hide) {
            instance.on(eventData.on, function(event) {
                instance._theme.disableInputs();
                var showIds = [];
                if (eventData.show) {
                    var showIds = eventData.show.split(",");
                    showIds.forEach(function(id) {
                        var plugIn = pluginManager.getPluginObject(id);
                        if (plugIn) {
                            if (plugIn.animate_on_show) {
                                console.log('plugIn.animations', plugIn.animations);
                                var animationFn = eval(plugIn.animations[plugIn.animate_on_show].animateFn);
                                animationFn.apply(null, [plugIn._self]);
                            } else {
                                plugIn._self.visible = true;
                            }
                        }
                    });
                }
                if (eventData.hide) {
                    var hideIds = eventData.hide.split(",");
                    hideIds.forEach(function(id) {
                        var plugIn = pluginManager.getPluginObject(id);
                        if (plugIn && plugIn._parent) {
                            plugIn._parent.removeChild(plugIn._self);
                        }
                    });
                }
            });
        } else if (eventData.reload) {
            instance.on(eventData.on, function(event) {
                var count = instance._theme._stageRepeatCount[instance._data.id];
                count -= 1;
                instance._theme._stageRepeatCount[instance._data.id] = count;
                instance._theme.replaceStage(this._self, instance._data.id, eventData);
            });
        } else if (eventData.start_page) {
            instance.on(eventData.on, function(event) {
                instance._theme.startPage();
            });
        } else if (eventData.audio) {
            instance.on(eventData.on, function() {
                switch(eventData.type) {
                    case 'play':
                        commandManager.play(eventData.asset);
                        break;
                    case 'pause':
                        commandManager.pause(eventData.asset);
                        break;
                    case 'toggle':
                        commandManager.toggle(eventData.asset);
                        break;
                    default:
                }
            });
        }
    }
});
pluginManager.registerPlugin('stage', StagePlugin);
