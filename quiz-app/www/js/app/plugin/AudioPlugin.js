var AudioPlugin = Plugin.extend({
    _type: 'audio',
    _isContainer: false,
    _id: undefined,
    _state: 'stop',
    _render: false,
    initPlugin: function(data) {
        this._id = data.asset;
    },
    play: function(action) {
        EventManager.processAppTelemetry(action, 'LISTEN', this);
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
    togglePlay: function(action) {
        if(this._state == 'play') {
            this.pause(action);
        } else {
            this.play(action);
        }
    },
    pause: function(action) {
        if(this._state == 'play') {
            this._self.paused = true;
            this._state = 'paused';
            EventManager.processAppTelemetry(action, 'PAUSE_LISTENING', this);
        }
    },
    stop: function(action) {
        if(this._state == 'play') {
            this._self.stop();
            EventManager.processAppTelemetry(action, 'STOP_LISTENING', this);
        }
    }

});
PluginManager.registerPlugin('audio', AudioPlugin);