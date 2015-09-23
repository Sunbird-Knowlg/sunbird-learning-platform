describe('Asset manager test cases', function() {

    // Img plugin is used for all the event manager specs. Make sure the specs of ImagePlugin are successfull
    beforeAll(function(done) {
        spyOn(AssetManager, 'init').and.callThrough();
        spyOn(AssetManager, 'getAsset').and.callThrough();
        spyOn(AssetManager, 'initStage').and.callThrough();
        spyOn(AssetManager, 'addStageAudio').and.callThrough();
        var themeData = {
            manifest: {
                media: [
                    {id: 'sringeri', src: 'sringeri.png', type: 'image'},
                    {id: 'splash_audio', src: 'splash.ogg', type: 'audio'}
                ]
            },
            stage: [
                {id:"splash", audio: {asset: 'splash_audio'}, img: {asset: 'sringeri'}}
            ]
        }

        AssetManager.init(themeData, '/js/test/assets/');
        done();
    });

    it('Test Asset manager init', function() {
        expect(AssetManager.init).toHaveBeenCalled();
        expect(AssetManager.init.calls.count()).toEqual(1);
    });

    it('Test Asset manager initStage', function(done) {
        AssetManager.initStage('splash', null, null, function() {
            expect(AssetManager.initStage).toHaveBeenCalled();
            expect(AssetManager.initStage.calls.count()).toEqual(1);
            done();
        });
    });

    it('Test Asset manager getAsset', function() {
        expect(AssetManager.initStage).toHaveBeenCalled();
        var img = AssetManager.getAsset('splash', 'sringeri');
        expect(img.src.indexOf('/js/test/assets/sringeri.png') > -1).toEqual(true);
        var abuff = AssetManager.getAsset('splash', 'splash_audio')
        expect(abuff.length).toEqual(396901);
        expect(abuff.duration).toEqual(9.000022675736961);            
    });

});