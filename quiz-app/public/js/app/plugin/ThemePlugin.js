var ThemePlugin = Plugin.extend({
    update: false,
    loader: undefined,
    _director: false,
    _gameAreaLeft: 0,
    initPlugin: function(data) {
        this._self = new createjs.Stage(data.canvasId);
        //this._director = new creatine.Director(this._self);
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
        this._gameAreaLeft = w / 2;
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
        if(this._data.stage) {
            var stage = _.findWhere(this._data.stage, {start: true});
            pluginManager.invoke('stage', stage, this, this);
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
    replaceStage: function(stageId) {
        console.log('ReplaceStage event stageId', stageId);
        var stage = _.findWhere(this._data.stage, {id: stageId});
        pluginManager.invoke('stage', stage, this, this);
        this.update();
    }
});
pluginManager.registerPlugin('theme', ThemePlugin);