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
        AssetManager.strategy.initStage(stageId, nextStageId, prevStageId, cb);
    },
    destroy: function() {
        AssetManager.strategy.destroy();
        AssetManager.strategy = undefined;
        AssetManager.stageAudios = {};
    },
    pauseStageAudio: function(stageId) {
        if(AssetManager.stageAudios[stageId] && AssetManager.stageAudios[stageId].length > 0) {
            console.info('Pausing stage audios', stageId);
            AssetManager.stageAudios[stageId].forEach(function(audioAsset) {
                AudioManager.pause({asset:audioAsset});
            });
        }
    },
    addStageAudio: function(stageId, audioId) {
        AssetManager.stageAudios[stageId].push(audioId);
    }
}
