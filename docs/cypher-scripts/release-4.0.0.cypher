// Scripts to set visibility=Defualt for all ObjectCategory objects.
MATCH (n:domain{IL_FUNC_OBJECT_TYPE:"ObjectCategory"}) SET n.visibility="Default";

// Migration Script for discussionForum
// Get the count
MATCH(n:domain) where exists(n.discussionForum) return count(n),n.discussionForum;
// Set the discussionForum
MATCH(n:domain) where exists(n.discussionForum) set n.discussionForum="{\"enabled\":\"No\"}";
