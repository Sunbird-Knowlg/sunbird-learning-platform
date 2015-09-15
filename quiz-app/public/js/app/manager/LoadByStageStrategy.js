LoadByStageStrategy = Class.extend({
    assetMap: {},
    commonAssets: [],
    templateAssets: [],
    loaders: {},
    commonLoader: undefined,
    templateLoader: undefined,
    stageManifests: {},
    init: function(themeData, basePath) {
        //console.info('createjs.CordovaAudioPlugin.isSupported()', createjs.CordovaAudioPlugin.isSupported());
        var instance = this;
        createjs.Sound.registerPlugins([createjs.CordovaAudioPlugin, createjs.WebAudioPlugin, createjs.HTMLAudioPlugin]);
        createjs.Sound.alternateExtensions = ["mp3"];
        this.destroy();
        if (themeData.manifest.media) {
            if (!_.isArray(themeData.manifest.media)) themeData.manifest.media = [themeData.manifest.media];
        }
        themeData.manifest.media.forEach(function(media) {
            media.src = basePath + media.src;
            if(createjs.CordovaAudioPlugin.isSupported()) { // Only supported in mobile
                if(media.type !== 'sound' && media.type !== 'audiosprite') {
                    media.src = 'file:///' + media.src;
                }
            }

            if (media.type == 'json') {
                instance.commonAssets.push(_.clone(media));
            } else {
                if(media.type == 'audiosprite') {
                    if(!_.isArray(media.data.audioSprite)) media.data.audioSprite = [media.data.audioSprite];
                }
                instance.assetMap[media.id] = media;
            }
        });
        var stages = themeData.stage;
        if (!_.isArray(stages)) stages = [stages];
        stages.forEach(function(stage) {
            instance.stageManifests[stage.id] = [];
            AssetManager.stageAudios[stage.id] = [];
            instance.populateAssets(stage, stage.id);
        });
        console.info("Instance", instance);
        if (instance.stageManifests.baseStage && instance.stageManifests.baseStage.length > 0) {
            instance.commonAssets.push.apply(instance.commonAssets, instance.stageManifests.baseStage);
        }
        instance.loadCommonAssets();

        var templates = themeData.template;
        if (!_.isArray(templates)) templates = [templates];
        templates.forEach(function(template) {
            instance.populateTemplateAssets(template);
        });
        instance.loadTemplateAssets();
    },
    populateAssets: function(data, stageId) {
        var instance = this;
        for (k in data) {
            var plugins = data[k];
            if (!_.isArray(plugins)) plugins = [plugins];
            if (PluginManager.isPlugin(k) && k == 'g') {
                plugins.forEach(function(plugin) {
                    instance.populateAssets(plugin, stageId);
                });
            } else {
                plugins.forEach(function(plugin) {
                    var asset = instance.assetMap[plugin.asset];
                    if (asset) {
                        instance.stageManifests[stageId].push(_.clone(asset));
                    }
                });
            }
        }
    },
    populateTemplateAssets: function(data) {
        var instance = this;
        for (k in data) {
            var plugins = data[k];
            if (!_.isArray(plugins)) plugins = [plugins];
            if (PluginManager.isPlugin(k) && k == 'g') {
                plugins.forEach(function(plugin) {
                    instance.populateTemplateAssets(plugin);
                });
            } else {
                plugins.forEach(function(plugin) {
                    var asset = instance.assetMap[plugin.asset];
                    if (asset) {
                        instance.templateAssets.push(_.clone(asset));
                    }
                });
            }
        }
    },
    getAsset: function(stageId, assetId) {
        var asset = undefined;
        if (this.loaders[stageId]) asset = this.loaders[stageId].getResult(assetId);
        if (!asset) asset = this.commonLoader.getResult(assetId);
        if (!asset) asset = this.templateLoader.getResult(assetId);
        if (!asset) {
            console.error('Asset not found. Returning - ', (this.assetMap[assetId].src));
            return this.assetMap[assetId].src;
        };
        return asset;
    },
    initStage: function(stageId, nextStageId, prevStageId, cb) {
        var instance = this;
        this.loadStage(stageId, cb);
        var deleteStages = _.difference(_.keys(instance.loaders), [stageId, nextStageId, prevStageId]);
        if (deleteStages.length > 0) {
            deleteStages.forEach(function(stageId) {
                instance.destroyStage(stageId);
            })
        }
        if (nextStageId) {
            instance.loadStage(nextStageId, function() {
                var plugin = PluginManager.getPluginObject('next');
                plugin.show();
                var nextContainer = PluginManager.getPluginObject('nextContainer');
                if (nextContainer) {
                    nextContainer.show();
                }
            });
        }
        if (prevStageId) {
            instance.loadStage(prevStageId, function() {
                var plugin = PluginManager.getPluginObject('previous');
                plugin.show();
                var previousContainer = PluginManager.getPluginObject('previousContainer');
                if (previousContainer) {
                    previousContainer.show();
                }
            });
        }
        instance.loaders = _.pick(instance.loaders, stageId, nextStageId, prevStageId);
    },
    loadStage: function(stageId, cb) {
        var instance = this;
        if (!instance.loaders[stageId]) {
            var loader = new createjs.LoadQueue(false);
            loader.setMaxConnections(instance.stageManifests[stageId].length);
            if (cb) {
                loader.addEventListener("complete", cb);
            }
            loader.on('error', function(evt) {
                console.error('Asset preload error', evt);
            });
            loader.installPlugin(createjs.Sound);
            var manifest = JSON.parse(JSON.stringify(instance.stageManifests[stageId]));
            loader.loadManifest(manifest, true);
            instance.loaders[stageId] = loader;
        } else {
            if (cb) {
                cb();
            }
        }
    },
    loadCommonAssets: function() {
        var loader = new createjs.LoadQueue(false);
        loader.setMaxConnections(this.commonAssets.length);
        loader.installPlugin(createjs.Sound);
        loader.loadManifest(this.commonAssets, true);
        this.commonLoader = loader;
    },
    loadTemplateAssets: function() {
        var loader = new createjs.LoadQueue(false);
        loader.setMaxConnections(this.templateAssets.length);
        loader.installPlugin(createjs.Sound);
        loader.loadManifest(this.templateAssets, true);
        this.templateLoader = loader;
    },
    destroy: function() {
        var instance = this;
        for (k in instance.loaders) {
            instance.destroyStage(k);
        }
        instance.assetMap = {};
        instance.loaders = {};
        instance.stageManifests = {};
    },
    destroyStage: function(stageId) {
        if (this.loaders[stageId]) {
            this.loaders[stageId].destroy();
            AssetManager.stageAudios[stageId].forEach(function(audioAsset) {
                AudioManager.destroy(audioAsset);
            });
        }
    }
});