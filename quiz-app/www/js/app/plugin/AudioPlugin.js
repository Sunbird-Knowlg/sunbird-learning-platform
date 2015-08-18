var AudioPlugin = Plugin.extend({
    _type: 'audio',
    _isContainer: false,
    _id: undefined,
    _state: 'stop',
    initPlugin: function(data) {
        this._id = data.id;
    },
    play: function() {
        if(this._state == 'paused') {
            this._self.paused = false;
            this._state = 'play';
        } else if(this._self) {
            this._state = 'play';
            this._self.play();
        } else {
            this._state = 'play';
            var instance = this;
            this._self = createjs.Sound.play(this._id);
            this._self.on("complete", function() {
                instance._state = 'stop';
            });
        }
    },
    toggle: function() {
        if(this._state == 'play') {
            this.pause();
        } else {
            this.play();
        }
    },
    pause: function() {
        if(this._state == 'play') {
            this._self.paused = true;
            this._state = 'paused';
        }
    },
    stop: function() {
        if(this._state == 'play') {
            this._self.stop();
        }
    }

});
pluginManager.registerPlugin('audio', AudioPlugin);
