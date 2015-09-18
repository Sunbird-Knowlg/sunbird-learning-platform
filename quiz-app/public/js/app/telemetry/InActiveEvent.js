InActiveEvent = Class.extend({
    init: function() {},
    ext: function() {},
    start: function() {},
    end: function() {},
    flush: function() {},
    __noSuchMethod__: function() {
        console.log('TelemetryService is inActive');
        return this;
    }
})