function clickOnObject(id) {
	var stage = PluginManager.getPluginObject(id);
	stage._self.dispatchEvent('click');
}