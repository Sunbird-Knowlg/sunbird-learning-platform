GlobalContext = {
    user: {},
    game: {
        id: "",
        ver: ""
    },
    init: function(gid, ver) {
        return new Promise(function(resolve, reject) {
            GlobalContext.game.id = gid;
            GlobalContext.game.ver = ver;
            GlobalContext._setGlobalContext(resolve, reject);
        });
    },
    _setGlobalContext: function(resolve, reject) {
        new Promise(function(resolve, reject) {
            if(window.plugins && window.plugins.webintent) {
                resolve(GlobalContext._getIntentExtra('uid'));  // change it to 'origin' after getting new build.
            } else {
                resolve('Genie');
            }
        })
        .then(function(origin) {
            if(origin) {
                return GenieService.getCurrentUser();
            } else {
                reject(false);
            }
        })
        .then(function(result) {
            console.log("Getting user session.......");
            if (result && result.status == 'success') {
                if (result.data.uid) {
                    GlobalContext.user = result.data;
                    resolve(true);
                } else {
                    reject(false);
                }
            } else {
                reject(false);
            }
        });
    },
    _getIntentExtra: function(param) {
        return new Promise(function(resolve, reject) {
            window.plugins.webintent.getExtra(param,
                function(url) {
                    console.log(param + ' intent value: ' + url);
                    resolve(url);
                },
                function() {
                    console.log('intent value not set for: ' + param);
                    resolve();
                }
            );
        });
    }

};