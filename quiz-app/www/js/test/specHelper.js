
function clickOnObject(id) {
	var obj = PluginManager.getPluginObject(id);
	obj._self.dispatchEvent('click');
}

function getText(id){
	var obj = PluginManager.getPluginObject(id);
	return obj._self.text;

}

function getOriginalXY(id){
	var obj = PluginManager.getPluginObject(id);
	origX = obj._self.getOrigX;
	origY = obj._self.getOrigY;
	//return origX, origY;
}

function isDisplayed(id){
	var obj = PluginManager.getPluginObject(id);
	return obj._self.isVisible();
}

function getAudioState(audioObjectId){
	var obj = PluginManager.getPluginObject(audioObjectId);
	return obj.state;
}


function isAudioPlaying(audioObjectId){
	if (getAudioState(audioObjectId) == "play"){
		return True;
	}
	else
		return False;
}

function isAudioStopped(audioObjectId){
	if(getAudioState(audioObjectId)=="stop"){
		return True;
	}
	else
		return False;
}

function getAudioPlayState(audioObjectId){
	var obj = PluginManager.getPluginObject(audioObjectId);
	return obj._self.playState;
}

function isPlayStateFinished(audioObjectId){
	if (getAudioPlayState(audioObjectId) == "playFinished"){
		return True;
	}
	else
		return False;
}

function isPlayStateSucceeded(audioObjectId){
	if (getAudioPlayState(audioObjectId) == "playSucceeded"){
		return True;
	}
	else
		return False;
}

