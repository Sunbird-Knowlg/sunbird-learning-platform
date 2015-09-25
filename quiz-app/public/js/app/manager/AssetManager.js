AssetManager = {
    strategy: undefined,
    stageAudios: {},
    init: function(themeData, basePath) {
        AssetManager.strategy = new LoadByStageStrategy(themeData, basePath);
    },
    getAsset: function(stageId, assetId) {
        return AssetManager.strategy.getAsset(stageId, assetId);
    },
    initStage: function(stageId, nextStageId, prevStageId, cb) {
        if (nextStageId) {
            AssetManager.stopStageAudio(nextStageId);
        }
        if (prevStageId) {
            AssetManager.stopStageAudio(prevStageId);
        }
        AssetManager.strategy.initStage(stageId, nextStageId, prevStageId, cb);
    },
    destroy: function() {
        AssetManager.strategy.destroy();
        AssetManager.strategy = undefined;
        AssetManager.stageAudios = {};
    },
    stopStageAudio: function(stageId) {
        if(AssetManager.stageAudios[stageId] && AssetManager.stageAudios[stageId].length > 0) {
            AssetManager.stageAudios[stageId].forEach(function(audioAsset) {
                AudioManager.stop({asset:audioAsset});
            });
        }
    },
    addStageAudio: function(stageId, audioId) {
        if(AssetManager.stageAudios[stageId]) {
            AssetManager.stageAudios[stageId].push(audioId);
        }
    }
}