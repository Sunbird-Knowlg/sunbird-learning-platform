describe('Worksheet Validations', function() {
    var jsonData;
    // done is called after getting the data. done() - is used to handle asynchronous operations...
    beforeEach(function(done) {
        $.get('worksheets/worksheet2/markup.xml', function(data) {
                if (!$.isPlainObject(data)) {
                    var x2js = new X2JS({
                        attributePrefix: 'none'
                    });
                    jsonData = x2js.xml2json(data);
                    done();
                }
            })
            .fail(function(err) {
                alert("Unable to render - ", err);
            });
    });

    it('validate markup has all the asserts.', function() {
        expect(jsonData).not.toEqual(null);
        var assetIds = _.pluck(jsonData.theme.manifest.media, 'id');
        console.log('assetIds:', assetIds);
        if (_.isArray(jsonData.theme.stage)) {
            for (key in jsonData.theme.stage) {
                var num = key;
                var stage = jsonData.theme.stage[key];
                var stageAssetIds = getStageAssetIds(stage);
                for (key in stageAssetIds) {
                    var id = stageAssetIds[key];
                    console.log('indexof ' + id + ': ' + _.indexOf(assetIds, id) + ' : ' + (_.indexOf(assetIds, id) >= 0));
                    var expected = (_.indexOf(assetIds, id) >= 0);
                    expect(true).toEqual(expected);
                }
                // console.log('Stage: ' + num + ' -- AssetIds:', stageAssetIds);
            }
        } else if (_.isObject(jsonData.theme.stage)) {
            var stage = jsonData.theme.stage;
            var stageAssetIds = getStageAssetIds(stage);
            for (key in stageAssetIds) {
                var id = stageAssetIds[key];
                console.log('indexof ' + id + ': ' + _.indexOf(assetIds, id) + ' : ' + (_.indexOf(assetIds, id) >= 0));
                var expected = (_.indexOf(assetIds, id) >= 0);
                expect(true).toEqual(expected);
            }
            // console.log('Object Stage:', stage);
        }
    });
});

function getStageAssetIds(stage) {
    var assetIds = [];
    if (stage) {
        for (key in stage) {
            var val = stage[key];
            if (_.isArray(val)) {
                var ids = getStageAssetIds(val);
                if (ids && ids.length > 0) {
                    for (key in ids)
                        assetIds.push(ids[key]);
                }
            } else if (_.isObject(val) && val.asset) {
                assetIds.push(val.asset);
            }
        }
    }
    return assetIds;
}