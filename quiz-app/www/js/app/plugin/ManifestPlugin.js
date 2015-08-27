var ManifestPlugin = Plugin.extend({
	_render: false,
	initPlugin: function(data) {
	}
});
PluginManager.registerPlugin('manifest', ManifestPlugin);