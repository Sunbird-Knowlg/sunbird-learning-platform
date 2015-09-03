AudioManager = {
	instances: {},
	play: function(action, instance) {
		instance = instance || AudioManager.instances[action.asset] || {};
        if(instance.state == 'paused') {
            instance.object.paused = false;
            instance.state = 'play';
        } else if(instance.object) {
            instance.state = 'play';
            instance.object.play();
        } else {
            instance.state = 'play';
            instance.object = createjs.Sound.play(action.asset, {interrupt:createjs.Sound.INTERRUPT_ANY});
            instance.object.on("complete", function() {
                instance.state = 'stop';
            });
            instance._data = {id: action.asset};
            AudioManager.instances[action.asset] = instance;
            AssetManager.addStageAudio(Renderer.theme._currentStage, action.asset);
        }
        EventManager.processAppTelemetry(action, 'LISTEN', instance);
    },
    togglePlay: function(action) {
    	var instance = AudioManager.instances[action.asset] || {};
        if(instance.state == 'play') {
            AudioManager.pause(action, instance);
        } else {
            AudioManager.play(action, instance);
        }
    },
    pause: function(action, instance) {
    	instance = instance || AudioManager.instances[action.asset];
        if(instance.state == 'play') {
            instance.object.paused = true;
            instance.state = 'paused';
            EventManager.processAppTelemetry(action, 'PAUSE_LISTENING', instance);
        }
    },
    stop: function(action) {
    	var instance = AudioManager.instances[action.asset] || {};
        if(instance.state == 'play') {
            instance.object.stop();
            EventManager.processAppTelemetry(action, 'STOP_LISTENING', instance);
        }
    },
    stopAll: function(action) {
    	createjs.Sound.stop();
		EventManager.processAppTelemetry(action, 'STOP_ALL_SOUNDS');
    },
    destroy: function(soundId) {
        var instance = AudioManager.instances[soundId] || {};
        if(instance.object) {
            instance.object.destroy();
            instance.object = undefined;
            instance.state = undefined;
        }
    }
}