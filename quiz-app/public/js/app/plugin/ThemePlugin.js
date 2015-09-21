var ThemePlugin = Plugin.extend({
    _type: 'theme',
    _render: false,
    update: false,
    baseDir: '',
    loader: undefined,
    _director: false,
    _currentScene: undefined,
    _currentStage: undefined,
    _canvasId: undefined,
    inputs: [],
    _animationEffect: {effect:'moveOut'},
    _themeData: undefined,
    _controllerMap: {},
    _isContainer: false,
    _templateMap: {},
    initPlugin: function(data) {
        this._canvasId = data.canvasId;
        this._self = new createjs.Stage(data.canvasId);
        this._director = new creatine.Director(this._self);
        this._dimensions = {
            x:0,
            y: 0,
            w: this._self.canvas.width,
            h: this._self.canvas.height
        }
        createjs.Touch.enable(this._self);
        this._self.enableMouseOver(10);
        this._self.mouseMoveOutside = true;
    },
    updateCanvas: function(w, h) {
        this._self.canvas.width = w;
        this._self.canvas.height = h;
        this._dimensions = {
            x:0,
            y: 0,
            w: this._self.canvas.width,
            h: this._self.canvas.height
        }
    },
    start: function(basePath) {
        var instance = this;
        AssetManager.init(this._data, basePath);
        AssetManager.initStage(this._data.startStage, null, null, function() {
            instance.render();
        });
    },
    render: function() {
        var instance = this;
        ControllerManager.reset();
        if (this._data.controller) {
            if (_.isArray(this._data.controller)) {
                this._data.controller.forEach(function(p) {
                    instance.addController(p);
                });
            } else {
                instance.addController(this._data.controller);
            }
        }
        if (this._data.template) {
            if (_.isArray(this._data.template)) {
                this._data.template.forEach(function(t) {
                    instance._templateMap[t.id] = t;
                });
            } else {
                instance._templateMap[this._data.template.id] = this._data.template;
            }
        }
        if (!_.isArray(this._data.stage)) this._data.stage = [this._data.stage];
        if(this._data.stage) {
            this._data.stage.forEach(function(s) {
                instance.initStageControllers(s);
            });
            this.invokeStage(this._data.startStage);
        }
        this.update();
        $('#gameAreaLoad').hide();
    },
    addController: function(p) {
        var controller = ControllerManager.get(p.type, p.id, this.baseDir);
        if (controller) {
            this._controllerMap[p.name] = controller;
        }
    },
    initStageControllers: function(stage) {
        if (stage.controller) {
            if (_.isArray(stage.controller)) {
                stage.controller.forEach(function(p) {
                    ControllerManager.get(p.type, p.id, this.baseDir);
                });
            } else {
                ControllerManager.get(stage.controller.type, stage.controller.id, this.baseDir);
            }
        }
    },
    reRender: function() {
        this._self.clear();
        this._self.removeAllChildren();
        this.render();
    },
    update: function() {
        this._self.update();
    },
    getAsset: function(aid) {
        return AssetManager.getAsset(this._currentStage, aid);
    },
    addChild: function(child, childPlugin) {
        var instance = this;
        child.on('sceneenter', function() {
            instance.enableInputs();
            childPlugin.dispatchEvent('enter');
            instance.preloadStages();
            Renderer.update = true;
            childPlugin.uncache();
        });
        var nextIdx = this._currIndex++;
        if(this._currentScene) {
            this._currentScene.dispatchEvent('exit');
            this._currentScene = childPlugin;
            this._director.replace(child, this.getTransitionEffect(this._animationEffect));
        } else {
            this._currentScene = childPlugin;
            this._director.replace(child);
        }
        childPlugin.setIndex(nextIdx);
    },
    replaceStage: function(stageId, effect) {
        this.disableInputs();
        this.inputs = [];
        this._animationEffect = effect;
        this.invokeStage(stageId);
    },
    invokeStage: function(stageId) {
        var stage = _.findWhere(this._data.stage, {id: stageId});
        if(stage.extends) {
            baseStage = _.findWhere(this._data.stage, {id: stage.extends});
            stage = this.mergeStages(stage, baseStage);
        }
        this._currentStage = stageId;
        PluginManager.invoke('stage', stage, this, null, this);
    },
    preloadStages: function() {
        var stagesToLoad = this.getStagesToPreLoad(this._currentScene._data);
        AssetManager.initStage(stagesToLoad.stage, stagesToLoad.next, stagesToLoad.prev);
    },
    mergeStages: function(stage1, stage2) {
        for(k in stage2) {
            if(k === 'id') continue;
            var attr = stage2[k];
            if(stage1[k]) {
                if(!_.isArray(stage1[k])) {
                    stage1[k] = [stage1[k]];
                }
                if(_.isArray(attr)) {
                    stage1[k].push.apply(stage1[k], attr);
                } else {
                    stage1[k].push(attr);
                }
            } else {
                stage1[k] = attr;
            }
        }
        return stage1;
    },
    transitionTo: function(action) {
        var stage = this._currentScene;
        if (action.transitionType === 'previous') {
            if (stage._stageController && stage._stageController.hasPrevious()) {
                stage._stageController.decrIndex(2);
                this.replaceStage(stage._data.id, action);
            } else {
                if (stage._stageController) {
                    stage._stageController.setIndex(-1);
                    if (action.reset == true) {
                        stage._stageController.reset();
                    }
                }
                this.replaceStage(action.value, action);
            }
        } else {
            if (stage._stageController && stage._stageController.hasNext()) {
                this.replaceStage(stage._data.id, action);
            } else {
                if (stage._stageController && action.reset == true) {
                    stage._stageController.reset();
                }
                this.replaceStage(action.value, action);
            }
        }
    },
    disableInputs: function() {
        this.inputs.forEach(function(inputId) {
            document.getElementById(inputId).style.display = 'none';
        })
    },
    enableInputs: function() {
        this.inputs.forEach(function(inputId) {
            document.getElementById(inputId).style.display = 'block';
        })
    },
    getTransitionEffect: function(animation) {
        var d = this.getDirection(animation.direction),
            e = this.getEase(animation.ease),
            t = animation.duration;
        animation.effect = animation.effect || 'scroll';
        var effect;
        switch (animation.effect.toUpperCase()) {
            case "SCALEIN":
                effect = new creatine.transitions.ScaleIn(e, t);
                break;
            case "SCALEOUT":
                effect = new creatine.transitions.ScaleOut(e, t);
                break;
            case "SCALEINOUT":
                effect = new creatine.transitions.ScaleInOut(e, t);
                break;
            case "MOVEIN":
                effect = new creatine.transitions.MoveIn(d, e, t);
                break;
            case "SCROLL":
                effect = new creatine.transitions.Scroll(d, e, t);
                break;
            case "FADEIN":
                effect = new creatine.transitions.FadeIn(e, t);
                break;
            case "FADEOUT":
                effect = new creatine.transitions.FadeOut(e, t);
                break;
            case "FADEINOUT":
                effect = new creatine.transitions.FadeInOut(e, t);
                break;
            default:
                effect = new creatine.transitions.MoveOut(d, e, t);
        }
        return effect;
    },
    getDirection: function(d) {
        if(d === undefined) {
            return d;
        }
        return eval('creatine.' + d.toUpperCase())
    },
    getEase: function(e) {
        if(e === undefined) {
            return e;
        }
        return eval('createjs.Ease.' + e);
    },
    getStagesToPreLoad: function(stageData) {
        var params = stageData.param;
        if(!params) params = [];
        if(!_.isArray(params)) params = [params];
        var next = _.findWhere(params, {name: 'next'}),
            prev = _.findWhere(params, {name: 'previous'});
        var nextStageId = undefined, prevStageId = undefined;
        if(next) nextStageId = next.value;
        if(prev) prevStageId = prev.value;
        return {stage: stageData.id, next: nextStageId, prev: prevStageId};
    },
    cleanUp: function() {
        createjs.Touch.disable(this._self);
    },
    pause: function() {
        if(this._currentStage) {
            AssetManager.stopStageAudio(this._currentStage);
        }
        TelemetryService.interrupt("BACKGROUND", this._currentStage);
    },
    resume: function() {
        TelemetryService.interrupt("RESUME", this._currentStage);
    }
});
PluginManager.registerPlugin('theme', ThemePlugin);