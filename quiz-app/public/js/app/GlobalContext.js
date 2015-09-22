GlobalContext = {
	user: {
		sid: undefined,
		uid: undefined,
		did: undefined,
		uname: ""
	},
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
		if (window.plugins && window.plugins.webintent) {
			var promises = [];
            promises.push(GlobalContext._getIntentExtra('sid', GlobalContext.user));
            promises.push(GlobalContext._getIntentExtra('did', GlobalContext.user));
            promises.push(GlobalContext._getIntentExtra('uid', GlobalContext.user));
            promises.push(GlobalContext._getIntentExtra('ueksid', GlobalContext.user));
            Promise.all(promises)
            .then(function(result) {
            	if (!GlobalContext.user.sid && !GlobalContext.user.uid && !GlobalContext.user.did) {
            		reject(false);
            	} else {
            		resolve(true);
            	}
            });
        } else {
        	GlobalContext.user.sid = _.uniqueId('sid_');
			GlobalContext.user.uid = _.uniqueId('uid_');
			GlobalContext.user.did = _.uniqueId('did_');
            resolve(true);
        }
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
	            }, function() {
	                console.log('intent value not set for: ' + param);
	                resolve(true);
	            }
	        );
		});
		
	}
};