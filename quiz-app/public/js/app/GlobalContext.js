GlobalContext = {
	user: {
		sid: "",
		uid: "",
		did: "",
		uname: ""
	},
	game: {
		id: "",
		ver: ""
	},
	init: function(gid, ver) {
		return new Promise(function(resolve, reject) {
			GlobalContext.user.sid = _.uniqueId('sid_');
			GlobalContext.user.uid = _.uniqueId('uid_');
			GlobalContext.user.did = _.uniqueId('did_');
			GlobalContext.game.id = gid;
			GlobalContext.game.ver = ver;
			GlobalContext._setGlobalContext(resolve, reject);
        });
	},
	_setGlobalContext: function(resolve, reject) {
		if (window.plugins && window.plugins.webintent) {
			var promises = [];
            promises.push(GlobalContext._getIntentExtra('sid'));
            promises.push(GlobalContext._getIntentExtra('did'));
            promises.push(GlobalContext._getIntentExtra('uid'));
            promises.push(GlobalContext._getIntentExtra('ueksid'));
            Promise.all(promises)
            .then(function(result) {
            	resolve(true);    
            });
        } else {
            resolve(true);
        }
	},
	_getIntentExtra: function(param) {
		return new Promise(function(resolve, reject) {
			window.plugins.webintent.getExtra(param,
	            function(url) {
	                console.log(param + ' intent value: ' + url);
	                if (url) {
	                	GlobalContext.user[param] = url;
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