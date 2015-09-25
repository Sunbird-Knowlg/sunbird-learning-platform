describe('Command manager test cases', function() {

    beforeEach(function(done) {
        Renderer.theme = {_currentStage:''};
        spyOn(CommandManager, 'handle').and.callThrough();
        this.plugin = createAndInvokePlugin();
        this.action = {
            asset: 'testShape'
        }
        done();
    });

    it('Test command play', function() {
        this.action.command = 'play';
        CommandManager.handle(this.action);
        expect(CommandManager.handle).toHaveBeenCalled();
        expect(CommandManager.handle.calls.count()).toEqual(1);
    });

    it('Test command pause', function() {
        this.action.command = 'pause';
        CommandManager.handle(this.action);
        expect(CommandManager.handle).toHaveBeenCalled();
        expect(CommandManager.handle.calls.count()).toEqual(1);
    });

    it('Test command stop', function() {
        this.action.command = 'stop';
        CommandManager.handle(this.action);
        expect(CommandManager.handle).toHaveBeenCalled();
        expect(CommandManager.handle.calls.count()).toEqual(1);
    });

    it('Test command togglePlay', function() {
        this.action.command = 'togglePlay';
        CommandManager.handle(this.action);
        expect(CommandManager.handle).toHaveBeenCalled();
        expect(CommandManager.handle.calls.count()).toEqual(1);
    });

    it('Test command show', function() {
        this.action.command = 'show';
        CommandManager.handle(this.action);
        expect(CommandManager.handle).toHaveBeenCalled();
        expect(CommandManager.handle.calls.count()).toEqual(1);
        expect(this.plugin._self.visible).toEqual(true);
    });

    it('Throw error on invalid plugin', function() {
        this.action.command = 'show';
        this.action.asset = '123';
        CommandManager.handle(this.action);
        expect(CommandManager.handle).toHaveBeenCalled();
        expect(CommandManager.handle.calls.count()).toEqual(1);
        expect(_.contains(PluginManager.errors, 'Plugin not found for action - ' + JSON.stringify(this.action))).toEqual(true);
    });

    it('Test command hide', function() {
        this.action.command = 'hide';
        CommandManager.handle(this.action);
        expect(CommandManager.handle).toHaveBeenCalled();
        expect(CommandManager.handle.calls.count()).toEqual(1);
        expect(this.plugin._self.visible).toEqual(false);
    });

    it('Test command toggleShow', function() {
        this.action.command = 'toggleShow';
        CommandManager.handle(this.action);
        expect(CommandManager.handle).toHaveBeenCalled();
        expect(CommandManager.handle.calls.count()).toEqual(1);
        expect(this.plugin._self.visible).toEqual(false);

        CommandManager.handle(this.action);
        expect(CommandManager.handle).toHaveBeenCalled();
        expect(CommandManager.handle.calls.count()).toEqual(2);
        expect(this.plugin._self.visible).toEqual(true);
    });

    it('Test command transitionTo', function() {
        this.action.command = 'transitionTo';
        CommandManager.handle(this.action);
        expect(CommandManager.handle).toHaveBeenCalled();
        expect(CommandManager.handle.calls.count()).toEqual(1);
        expect(_.contains(PluginManager.errors, 'Subclasses of plugin should implement transitionTo()')).toEqual(true);
    });

    it('Test command toggleShadow', function() {
        this.action.command = 'toggleShadow';
        CommandManager.handle(this.action);
        expect(CommandManager.handle).toHaveBeenCalled();
        expect(CommandManager.handle.calls.count()).toEqual(1);
        expect(this.plugin._self.shadow).not.toEqual(null);

        CommandManager.handle(this.action);
        expect(CommandManager.handle).toHaveBeenCalled();
        expect(CommandManager.handle.calls.count()).toEqual(2);
        expect(this.plugin._self.shadow).toEqual(undefined);
    });

    it('Test command eval', function() {
        this.action.command = 'eval';
        CommandManager.handle(this.action);
        expect(CommandManager.handle).toHaveBeenCalled();
        expect(CommandManager.handle.calls.count()).toEqual(1);
        expect(_.contains(PluginManager.errors, 'Subclasses of plugin should implement transitionTo()')).toEqual(true);
    });

    it('Test command reload', function() {
        this.action.command = 'reload';
        CommandManager.handle(this.action);
        expect(CommandManager.handle).toHaveBeenCalled();
        expect(CommandManager.handle.calls.count()).toEqual(1);
        expect(_.contains(PluginManager.errors, 'Subclasses of plugin should implement transitionTo()')).toEqual(true);
    });

    it('Test command restart', function() {
        this.action.command = 'restart';
        CommandManager.handle(this.action);
        expect(CommandManager.handle).toHaveBeenCalled();
        expect(CommandManager.handle.calls.count()).toEqual(1);
        expect(_.contains(PluginManager.errors, 'Subclasses of plugin should implement transitionTo()')).toEqual(true);
    });

});
