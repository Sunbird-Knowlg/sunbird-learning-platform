db = db.getSiblingDB('percp_scope_1');
db.user.ensureIndex({email: 1, identifier: 1});
db.user.ensureIndex({googleId: 1});
db.user.ensureIndex({facebookId: 1});
db.user.ensureIndex({email: 1});