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
		GlobalContext.user.sid = "de305d54-75b4-431b-adb2-eb6b9e546013";
		GlobalContext.user.uid = "123e4567-e89b-12d3-a456-426655440000";
		GlobalContext.user.did = "ff305d54-85b4-341b-da2f-eb6b9e5460fa";

		GlobalContext.game.id = gid;
		GlobalContext.game.ver = ver;

		return TelemetryService.init(GlobalContext.user, GlobalContext.game);
	}
};