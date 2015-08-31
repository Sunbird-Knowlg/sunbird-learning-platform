describe('Telemetry Service API', function() {
    jasmine.DEFAULT_TIMEOUT_INTERVAL = 20000;
    
    beforeEach(function(done) {
        // done is called after getting the data. done() - is used to handle asynchronous operations...
        var user = {
            "sid": "de305d54-75b4-431b-adb2-eb6b9e546013",
            "uid": "123e4567-e89b-12d3-a456-426655440000",
            "did": "ff305d54-85b4-341b-da2f-eb6b9e5460fa"
        };
        var game = {
                "id": "com.ilimi.quiz.app",
                "ver": "1.0"
            };
        /*
        *** Important: Assuming that game, user data will be read and passed to TelemetryService.
        *** Will change this if needed.
        */
        TelemetryService.init(user, game);
        setTimeout(function() {
            TelemetryService.start();
            TelemetryService.interact("TOUCH", "id", "TOUCH");
            TelemetryService.startAssess("NUM", "qid", "MEDIUM");
            TelemetryService.endAssess("qid", "yes", 1);
            TelemetryService.startAssess("NUM", "qid", "MEDIUM");
            TelemetryService.endAssess("qid", "no", 1);
            TelemetryService.startAssess("NUM", "qid", "MEDIUM");
            TelemetryService.endAssess("qid", "yes", 1);
            TelemetryService.end();
            setTimeout(function() {
                done();
                console.log('beforeEach called...');    
            }, 3000);
        }, 3000);
    });

    it('Service is Active.', function() {
        var expected = TelemetryService.isActive;
        expect(true).toEqual(expected);
    });
});

function pausecomp(millis)
 {
  var date = new Date();
  var curDate = null;
  do { curDate = new Date(); }
  while(curDate-date < millis);
}