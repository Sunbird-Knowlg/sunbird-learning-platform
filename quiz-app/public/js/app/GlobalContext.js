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
        GenieService.getCurrentUser()
            .then(function(result) {
                if (result.status == 'success') {
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
    _getIntentExtra: function(param, contextObj) {
        return new Promise(function(resolve, reject) {
            window.plugins.webintent.getExtra(param,
                function(url) {
                    console.log(param + ' intent value: ' + url);
                    if (url) {
                        contextObj[param] = url;
                    }
                    resolve(true);
                },
                function() {
                    console.log('intent value not set for: ' + param);
                    resolve(true);
                }
            );
        });
    }

};