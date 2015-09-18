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
        TelemetryService.assess("qid1").end(true, 1).mmc(["mmc1", "mmc2"]);
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
        var user = {
            "sid": "de305d54-75b4-431b-adb2-eb6b9e546013",
            "uid": "123e4567-e89b-12d3-a456-426655440000",
            "did": "ff305d54-85b4-341b-da2f-eb6b9e5460fa"
        };
        var game = {
                "id": "com.ekstep.quiz.app",
                "ver": "1.0"
            };
        TelemetryService.init(user, game)
        .then(function() {
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
            TelemetryService.assess("qid1").end(true, 1).mmc(["mmc1", "mmc2"]);
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
    

    describe('Testcases for events data validation', function() {
        it('events created and flushed.', function(done) {
            var expected = TelemetryService.isActive;
            expect(true).toEqual(expected);
            TelemetryService.ws.getData(TelemetryService._gameOutputFile)
            .then(function(data) {
                expect(data.length).not.toEqual(0);
                done();
            });
        });
        it('count of the events.', function(done) {
            var expected = TelemetryService.isActive;
            expect(true).toEqual(expected);
            TelemetryService.ws.getData(TelemetryService._gameOutputFile)
            .then(function(data) {
                var dataObj = JSON.parse( data );
                expect(5).toEqual(dataObj.events.length);
                done();
            });
        });
        it('validate structure of each event.', function(done) {
            var expected = TelemetryService.isActive;
            expect(true).toEqual(expected);
            TelemetryService.ws.getData(TelemetryService._gameOutputFile)
            .then(function(data) {
                expect(true).toEqual(isValidJson(data));
                done();
            });
        });

    });

    describe('Testcases for "Assess event"', function() {
        it('validate attempts.', function(done) {
            var expected = TelemetryService.isActive;
            expect(true).toEqual(expected);
            TelemetryService.ws.getData(TelemetryService._gameOutputFile)
            .then(function(data) {
            var dataObj = JSON.parse( data );
            expect(4).toEqual(dataObj.events[2].edata.eks.atmpts);
            done();
        });
        });
        
        it('validate failed attempts.', function(done) {
            var expected = TelemetryService.isActive;
            expect(true).toEqual(expected);
            TelemetryService.ws.getData(TelemetryService._gameOutputFile)
            .then(function(data) {
            var dataObj = JSON.parse( data );
            expect(1).toEqual(dataObj.events[2].edata.eks.failedatmpts);
            done();
        });
        });

        it('validate final pass and score values.', function(done) {
            var expected = TelemetryService.isActive;
            expect(true).toEqual(expected);
            TelemetryService.ws.getData(TelemetryService._gameOutputFile)
            .then(function(data) {
                var dataObj = JSON.parse( data );
                expect("Yes").toMatch(dataObj.events[2].edata.eks.pass);
                expect(1).toEqual(dataObj.events[2].edata.eks.score);
                done();
            });
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