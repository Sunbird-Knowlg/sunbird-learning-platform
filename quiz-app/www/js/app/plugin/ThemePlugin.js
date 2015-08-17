var ThemePlugin = Plugin.extend({
    _type: 'theme',
    update: false,
    loader: undefined,
    _director: false,
    _stageRepeatCount: undefined,
    _currentScene: undefined,
    _canvasId: undefined,
    inputs: [],
    _animationEffect: 'moveOutLeft',
    _themeData: undefined,
    _assessmentData: {},
    initPlugin: function(data) {
        this._canvasId = data.canvasId;
        this._self = new createjs.Stage(data.canvasId);
        this._director = new creatine.Director(this._self);
        this._stageRepeatCount = {};
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
    render: function() {
        if (this._data.datasource) {
            var themeData = undefined;
            if (_.isArray(this._data.datasource)) {
                themeData = this.getAsset(this._data.datasource[0].asset);
            } else {
                themeData = this.getAsset(this._data.datasource.asset);
            }
            this._themeData = themeData;
        }
        if(this._data.stage) {
            var stage = _.findWhere(this._data.stage, {start: true});
            pluginManager.invoke('stage', stage, this, null, this);
        }
        this.update();
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
        return this.loader.getResult(aid);
    },
    addChild: function(child, childPlugin) {
        var instance = this;
        child.on('sceneenter', function() {
            instance.enableInputs();
        })
        var nextIdx = this._currIndex++;
        if(this._currentScene) {
            this.disableInputs();
            this.inputs = [];
            this._director.replace(child, this.getTransition(this._animationEffect));
        } else {
            this._director.replace(child);
        }
        childPlugin.setIndex(nextIdx);
        this._currentScene = child;
    },
    replaceStage: function(prevStage, stageId, effect) {
        this._animationEffect = effect;
        var stage = _.findWhere(this._data.stage, {id: stageId});
        pluginManager.invoke('stage', stage, this, null, this);
    },
    registerEvent: function(instance, eventData) {
        if(eventData.isTest) {
            instance.on(eventData.on, function(event) {
                console.log('Theme Event invoked - ', eventData.on);
            });
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
            document.getElementById(inputId).value = undefined;
        })
    },
    getTransition: function(id) {
        id = id || 'moveOutLeft';
        if(id == 'scaleIn') {
            return new creatine.transitions.ScaleIn;
        } else if(id == 'scaleOut') {
            return new creatine.transitions.ScaleOut;
        } else if(id == 'scaleInOut') {
            return new creatine.transitions.ScaleInOut;
        } else if(id == 'moveOutRight') {
            return new creatine.transitions.MoveOut(creatine.RIGHT);
        } else if(id == 'moveInLeft') {
            return new creatine.transitions.MoveIn(creatine.LEFT);
        } else if(id == 'moveInRight') {
            return new creatine.transitions.MoveIn(creatine.RIGHT);
        } else if(id == 'scroll') {
            return new creatine.transitions.Scroll;
        } else if(id == 'fadeIn') {
            return new creatine.transitions.ScaleIn;
        } else if(id == 'fadeOut') {
            return new creatine.transitions.ScaleOut;
        } else if(id == 'fadeInOut') {
            return new creatine.transitions.ScaleInOut;
        } else {
            return new creatine.transitions.MoveOut(creatine.LEFT);
        }
    },
    startPage: function() {
        window.location.href = 'index.html';
    }
});
pluginManager.registerPlugin('theme', ThemePlugin);