AudioManager = {
	instances: {},
	play: function(action, instance) {
		instance = instance || AudioManager.instances[action.asset] || {};
        if(instance.object) {
            if(instance.object.paused) {
                instance.object.paused = false;
            } else if([createjs.Sound.PLAY_FINISHED, createjs.Sound.PLAY_INTERRUPTED, createjs.Sound.PLAY_FAILED].indexOf(instance.object.playState) !== -1) {
                instance.object.play();
            }
        } else {
            instance.object = createjs.Sound.play(action.asset, {interrupt:createjs.Sound.INTERRUPT_ANY});
            instance._data = {id: action.asset};
            AudioManager.instances[action.asset] = instance;
            AssetManager.addStageAudio(Renderer.theme._currentStage, action.asset);
        }
        EventManager.processAppTelemetry(action, 'LISTEN', instance);
    },
    togglePlay: function(action) {
    	var instance = AudioManager.instances[action.asset] || {};
        if(instance.object) {
            if(instance.object.playState === createjs.Sound.PLAY_FINISHED || instance.object.paused) {
                AudioManager.play(action, instance);    
            } else if (!instance.object.paused) {
                AudioManager.pause(action, instance);
            }
        } else {
            AudioManager.play(action, instance);
        }
    },
    pause: function(action, instance) {
    	instance = instance || AudioManager.instances[action.asset];
        if(instance.object && instance.object.playState === createjs.Sound.PLAY_SUCCEEDED) {
            instance.object.paused = true;
            EventManager.processAppTelemetry(action, 'PAUSE_LISTENING', instance);
        }
    },
    stop: function(action) {
    	var instance = AudioManager.instances[action.asset] || {};
        if(instance.object && instance.object.playState !== createjs.Sound.PLAY_FINISHED) {
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
            try {
                instance.object.destroy();
            } catch(err) {
                console.log('Error', err);
            }
            instance.object = undefined;
            instance.state = undefined;
        }
    }
}