AssetManager = {
    basePath: undefined,
    assetMap: {},
    commonAssets: [],
    loaders: {},
    commonLoader: undefined,
    stageManifests: {},
    init: function(themeData, basePath) {
        createjs.Sound.initializeDefaultPlugins();
        createjs.Sound.alternateExtensions = ["mp3"];
        AssetManager.destroy();
        AssetManager.basePath = basePath;
        if (themeData.manifest.media) {
            if (!_.isArray(themeData.manifest.media)) themeData.manifest.media = [themeData.manifest.media];
        }
        themeData.manifest.media.forEach(function(media) {
            if (media.type == 'json') {
                AssetManager.commonAssets.push(media);
            } else {
                if(media.type == 'audiosprite') {
                    if(!_.isArray(media.data.audioSprite)) media.data.audioSprite = [media.data.audioSprite];
                }
                AssetManager.assetMap[media.id] = media;
            }
        });
        var stages = themeData.stage;
        if (!_.isArray(stages)) stages = [stages];
        stages.forEach(function(stage) {
            AssetManager.stageManifests[stage.id] = [];
            AssetManager.populateAssets(stage, stage.id);
        });
        if (AssetManager.stageManifests.baseStage && AssetManager.stageManifests.baseStage.length > 0) {
            AssetManager.commonAssets.push.apply(AssetManager.commonAssets, AssetManager.stageManifests.baseStage);
        }
        AssetManager.loadCommonAssets();
    },
    populateAssets: function(data, stageId) {
        for (k in data) {
            var plugins = data[k];
            if (!_.isArray(plugins)) plugins = [plugins];
            if (PluginManager.isPlugin(k) && k == 'g') {
                plugins.forEach(function(plugin) {
                    AssetManager.populateAssets(plugin, stageId);
                });
            } else {
                plugins.forEach(function(plugin) {
                    if (AssetManager.assetMap[plugin.asset]) {
                        AssetManager.stageManifests[stageId].push(_.clone(AssetManager.assetMap[plugin.asset]));
                    }
                })
            }
        }
    },
    getAsset: function(stageId, assetId) {
        var asset = undefined;
        if (AssetManager.loaders[stageId]) asset = AssetManager.loaders[stageId].getResult(assetId);
        if (!asset) asset = AssetManager.commonLoader.getResult(assetId);
        if (!asset) {
            console.error('Asset not found. Returning - ', (AssetManager.basePath + AssetManager.assetMap[assetId].src));
            return AssetManager.basePath + AssetManager.assetMap[assetId].src;
        };
        return asset;
    },
    initStage: function(stageId, nextStageId, prevStageId, cb) {
        AssetManager.loadStage(stageId, cb);
        if (nextStageId) {
            AssetManager.loadStage(nextStageId, function() {
                var plugin = PluginManager.getPluginObject('next');
                plugin.show();
            });
        }
        if (prevStageId) {
            AssetManager.loadStage(prevStageId, function() {
                var plugin = PluginManager.getPluginObject('previous');
                plugin.show();
            });
        }
        var deleteStages = _.difference(_.keys(AssetManager.loaders), [stageId, nextStageId, prevStageId]);
        if (deleteStages.length > 0) {
            deleteStages.forEach(function(stageId) {
                AssetManager.clearStage(stageId);
            })
        }
        AssetManager.loaders = _.pick(AssetManager.loaders, stageId, nextStageId, prevStageId);
    },
    loadStage: function(stageId, cb) {
        if (!AssetManager.loaders[stageId]) {
            var loader = undefined;
            if (AssetManager.basePath) {
                loader = new createjs.LoadQueue(true, AssetManager.basePath);
            } else {
                loader = new createjs.LoadQueue(true);
            }
            loader.setMaxConnections(AssetManager.stageManifests[stageId].length);
            if (cb) {
                loader.addEventListener("complete", cb);
            }
            loader.on('error', function(evt) {
                console.error('Asset preload error', evt);
            });
            loader.installPlugin(createjs.Sound);
            var manifest = JSON.parse(JSON.stringify(AssetManager.stageManifests[stageId]));
            loader.loadManifest(manifest, true);
            AssetManager.loaders[stageId] = loader;
        } else {
            if (cb) {
                cb();
            }
        }
    },
    loadCommonAssets: function() {
        var loader = undefined;
        if (AssetManager.basePath) {
            loader = new createjs.LoadQueue(true, AssetManager.basePath);
        } else {
            loader = new createjs.LoadQueue(true);
        }
        loader.setMaxConnections(AssetManager.commonAssets.length);
        loader.installPlugin(createjs.Sound);
        loader.loadManifest(AssetManager.commonAssets, true);
        AssetManager.commonLoader = loader;
    },
    destroy: function() {
        for (k in AssetManager.loaders) {
            AssetManager.clearStage(k);
        }
        AssetManager.assetMap = {};
        AssetManager.loaders = {};
        AssetManager.stageManifests = {};
    },
    clearStage: function(stageId) {
        if (AssetManager.loaders[stageId]) {
            AssetManager.loaders[stageId].destroy();
        }
    }
}
