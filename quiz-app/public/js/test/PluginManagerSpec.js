describe('Plugin manager test cases', function() {

    // Img plugin is used for all the event manager specs. Make sure the specs of ImagePlugin are successfull
    beforeAll(function(done) {
        spyOn(PluginManager, 'registerPlugin').and.callThrough();
        spyOn(PluginManager, 'isPlugin').and.callThrough();
        spyOn(PluginManager, 'invoke').and.callThrough();
        spyOn(PluginManager, 'registerPluginObject').and.callThrough();
        spyOn(PluginManager, 'getPluginObject').and.callThrough();
        this.plugin = newPlugin()
        this.isPlugin = PluginManager.isPlugin('shapeext');
        this.pluginInstance = invokePlugin();
        done();
    });

    it('Plugin Registered', function() {
        expect(PluginManager.registerPlugin).toHaveBeenCalled();
        expect(PluginManager.registerPlugin.calls.count()).toEqual(1);
        expect(PluginManager.pluginMap['shapeext']).toEqual(this.plugin);
    });

    it('Test isPlugin', function() {
        expect(PluginManager.isPlugin).toHaveBeenCalled();
        expect(PluginManager.isPlugin.calls.count()).toEqual(1);
        expect(this.isPlugin).toEqual(true);
    });

    it('Plugin invoked and invoked only once', function() {
        expect(PluginManager.invoke).toHaveBeenCalled();
        expect(PluginManager.invoke.calls.count()).toEqual(1);
        expect(this.pluginInstance).not.toEqual(null);
    });

    it('Plugin object registered and is available in cache', function() {
        expect(PluginManager.registerPluginObject).toHaveBeenCalled();
        expect(PluginManager.registerPluginObject.calls.count()).toEqual(1);
    });

    it('Plugin object fetched successfully', function() {
        expect(Object.keys(PluginManager.pluginObjMap).length).toEqual(1);
        expect(PluginManager.getPluginObject.calls.count()).toEqual(0);
        var pluginObj = PluginManager.getPluginObject('testShape');
        expect(PluginManager.getPluginObject).toHaveBeenCalled();
        expect(PluginManager.getPluginObject.calls.count()).toEqual(1);
        expect(this.pluginInstance).toEqual(pluginObj);
    });

    xit('Plugin manager add error');
    xit('Plugin manager get errors');
    xit('Plugin manager cleanup');

});