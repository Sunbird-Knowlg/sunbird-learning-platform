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
  				GlobalContext.user = result.data;
  				resolve(true);
  			} else {
  				reject(false);
  			}
  		});
	}
};