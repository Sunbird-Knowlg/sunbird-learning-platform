AssetManager = {
    assetMap: {},
    commonAssets: [],
    templateAssets: [],
    loaders: {},
    commonLoader: undefined,
    templateLoader: undefined,
    stageManifests: {},
    stageAudios: {},
    init: function(themeData, basePath) {
        //console.info('createjs.CordovaAudioPlugin.isSupported()', createjs.CordovaAudioPlugin.isSupported());
        createjs.Sound.registerPlugins([createjs.CordovaAudioPlugin, createjs.WebAudioPlugin, createjs.HTMLAudioPlugin]);
        createjs.Sound.alternateExtensions = ["mp3"];
        AssetManager.destroy();
        if (themeData.manifest.media) {
            if (!_.isArray(themeData.manifest.media)) themeData.manifest.media = [themeData.manifest.media];
        }
        themeData.manifest.media.forEach(function(media) {
            media.src = basePath + media.src;
            if(createjs.CordovaAudioPlugin.isSupported()) { // Only supported in mobile
                if(media.type === 'sound' || media.type === 'audiosprite') {
                    media.src = '/android_asset/www/' + media.src;
                }
            }

            if (media.type == 'json') {
                AssetManager.commonAssets.push(_.clone(media));
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
            AssetManager.stageAudios[stage.id] = [];
            AssetManager.populateAssets(stage, stage.id);
        });
        if (AssetManager.stageManifests.baseStage && AssetManager.stageManifests.baseStage.length > 0) {
            AssetManager.commonAssets.push.apply(AssetManager.commonAssets, AssetManager.stageManifests.baseStage);
        }
        AssetManager.loadCommonAssets();

        var templates = themeData.template;
        if (!_.isArray(templates)) templates = [templates];
        templates.forEach(function(template) {
            AssetManager.populateTemplateAssets(template);
        });
        AssetManager.loadTemplateAssets();
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
                    var asset = AssetManager.assetMap[plugin.asset];
                    if (asset) {
                        AssetManager.stageManifests[stageId].push(_.clone(asset));
                    }
                });
            }
        }
    },
    populateTemplateAssets: function(data) {
        for (k in data) {
            var plugins = data[k];
            if (!_.isArray(plugins)) plugins = [plugins];
            if (PluginManager.isPlugin(k) && k == 'g') {
                plugins.forEach(function(plugin) {
                    AssetManager.populateTemplateAssets(plugin);
                });
            } else {
                plugins.forEach(function(plugin) {
                    var asset = AssetManager.assetMap[plugin.asset];
                    if (asset) {
                        AssetManager.templateAssets.push(_.clone(asset));
                    }
                });
            }
        }
    },
    getAsset: function(stageId, assetId) {
        var asset = undefined;
        if (AssetManager.loaders[stageId]) asset = AssetManager.loaders[stageId].getResult(assetId);
        if (!asset) asset = AssetManager.commonLoader.getResult(assetId);
        if (!asset) asset = AssetManager.templateLoader.getResult(assetId);
        if (!asset) {
            console.error('Asset not found. Returning - ', (AssetManager.assetMap[assetId].src));
            return AssetManager.assetMap[assetId].src;
        };
        return asset;
    },
    initStage: function(stageId, nextStageId, prevStageId, cb) {
        AssetManager.loadStage(stageId, cb);
        var deleteStages = _.difference(_.keys(AssetManager.loaders), [stageId, nextStageId, prevStageId]);
        if (deleteStages.length > 0) {
            deleteStages.forEach(function(stageId) {
                AssetManager.destroyStage(stageId);
            })
        }
        if (nextStageId) {
            AssetManager.pauseStageAudio(nextStageId);
            AssetManager.loadStage(nextStageId, function() {
                var plugin = PluginManager.getPluginObject('next');
                plugin.show();
                var nextContainer = PluginManager.getPluginObject('nextContainer');
                if (nextContainer) {
                    nextContainer.show();
                }
            });
        }
        if (prevStageId) {
            AssetManager.pauseStageAudio(prevStageId);
            AssetManager.loadStage(prevStageId, function() {
                var plugin = PluginManager.getPluginObject('previous');
                plugin.show();
                var previousContainer = PluginManager.getPluginObject('previousContainer');
                if (previousContainer) {
                    previousContainer.show();
                }
            });
        }
        AssetManager.loaders = _.pick(AssetManager.loaders, stageId, nextStageId, prevStageId);
    },
    loadStage: function(stageId, cb) {
        if (!AssetManager.loaders[stageId]) {
            var loader = new createjs.LoadQueue(true);
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
        var loader = new createjs.LoadQueue(true);
        loader.setMaxConnections(AssetManager.commonAssets.length);
        loader.installPlugin(createjs.Sound);
        loader.loadManifest(AssetManager.commonAssets, true);
        AssetManager.commonLoader = loader;
    },
    loadTemplateAssets: function() {
        var loader = new createjs.LoadQueue(true);
        loader.setMaxConnections(AssetManager.templateAssets.length);
        loader.installPlugin(createjs.Sound);
        loader.loadManifest(AssetManager.templateAssets, true);
        AssetManager.templateLoader = loader;
    },
    destroy: function() {
        for (k in AssetManager.loaders) {
            AssetManager.destroyStage(k);
        }
        AssetManager.assetMap = {};
        AssetManager.loaders = {};
        AssetManager.stageManifests = {};
        AssetManager.stageAudios = {};
    },
    destroyStage: function(stageId) {
        if (AssetManager.loaders[stageId]) {
            AssetManager.loaders[stageId].destroy();
            AssetManager.stageAudios[stageId].forEach(function(audioAsset) {
                AudioManager.destroy(audioAsset);
            });
        }
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
