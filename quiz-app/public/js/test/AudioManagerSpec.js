describe('Audio manager test cases', function() {

    // Img plugin is used for all the event manager specs. Make sure the specs of ImagePlugin are successfull
    beforeAll(function(done) {
        spyOn(EventManager, 'registerEvents').and.callThrough();
        var data = {
            "event": {
                "action": {
                    "type": "command",
                    "command": "show",
                    "asset": "testShape"
                },
                "type": "click"
            },
            "type": "rect",
            "x": 87,
            "y": 82,
            "w": 13,
            "h": 18,
            "hitArea": true,
            "id": "testShape"
        }
        ShapePluginExt = ShapePlugin.extend({
            render: function() {}
        });
        this.plugin = new ShapePluginExt(data, {
            dimensions: function() {
                return {
                    x: 0,
                    y: 0,
                    w: 500,
                    h: 500
                }
            }
        });
        done();
    });

    it('Events registered and registered only once', function() {
        expect(EventManager.registerEvents).toHaveBeenCalled();
        expect(EventManager.registerEvents.calls.count()).toEqual(1);
    });

});