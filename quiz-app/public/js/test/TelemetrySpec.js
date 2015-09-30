describe('Telemetry Service API - inActive', function() {
    beforeAll(function(done) {
        // done is called after getting the data. done() - is used to handle asynchronous operations...
        TelemetryService.start();
        TelemetryService.interact("TOUCH", "id", "TOUCH");
        TelemetryService.assess("qid", "NUM", "MEDIUM").start();
        TelemetryService.assess("qid").end(true, 1);
        TelemetryService.assess("qid", "NUM", "MEDIUM").start();
        TelemetryService.assess("qid").end(false, 0);
        TelemetryService.assess("qid", "NUM", "MEDIUM").start();
        TelemetryService.assess("qid").end(true, 1);
        TelemetryService.assess("qid", "NUM", "MEDIUM").start();
        TelemetryService.assess("qid").end(true, 1);
        TelemetryService.assess("qid1", "NUM", "MEDIUM").start();
        TelemetryService.assess("qid1").end(true, 1);
        TelemetryService.end();
        setTimeout(function() {
            done();
        }, 2000);
    });

    it('service is inactive.', function() {
        var expected = TelemetryService.isActive;
        expect(false).toEqual(expected);
    });

    it('events should not create and output data should be empty.', function() {
        expect('undefined').toEqual((typeof TelemetryService.ws));
    });

})

describe('Telemetry Service API', function() {

    beforeAll(function(done) {
        // done is called after getting the data. done() - is used to handle asynchronous operations...
        var packageName = "org.ekstep.quiz.app";
        var version = "1.0";
        GlobalContext.init(packageName, version)
        .then(function() {
            return TelemetryService.init(GlobalContext.game);
        })
        .then(function() {
            console.log("Init completed........");
            TelemetryService.start();
            TelemetryService.end();
            setTimeout(function() {
                done();
            }, 2000);
        });
    });

    describe('Telemetry Service initialization', function() {
        it('service is active.', function() {
            var expected = TelemetryService.isActive;
            expect(true).toEqual(expected);
        });
    });
});

function isValidJson(str) {
    try {
        var properties = ["eid", "ts", "ver", "gdata", "sid", "uid", "did", "edata"];
        var requiredPropAssess = ["subj", "qlevel", "qid"];
        var requiredPropInteract = ["type"];

        var dataObj = JSON.parse(str);
        var events = dataObj.events;
        // json properties validation  
        for(var i = 0 ; i < events.length; i++) {
            for(key in properties) {
                var property = properties[key];
                if(dataObj.events[i][property] == null || dataObj.events[i][property] == undefined){
                    return false;
                }
                // OE_ASSESS required properties validation
                if(property == "OE_ASSESS"){
                    for(key in requiredPropAssess){
                        var property = requiredPropAssess[key];
                        if(dataObj.events[i].edata.eks[property] == null || dataObj.events[i].edata.eks[property] == undefined)
                            return false;
                        }
                }
                // OE_INTERACT required properties validation
                if(property == "OE_INTERACT"){
                    for(key in requiredPropAssess){
                        var property = requiredPropInteract[key];
                        if(dataObj.events[i].edata.eks[property] == null || dataObj.events[i].edata.eks[property] == undefined)
                            return false;
                        }
                }
            }
        }
    } catch (e) {
        return false;
    }
    return true;
}