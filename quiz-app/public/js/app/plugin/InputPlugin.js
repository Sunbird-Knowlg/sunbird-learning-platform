var InputPlugin = Plugin.extend({
	_type: 'input',
	_isContainer: false,
	initPlugin: function(data) {
		var point = this._parent.localToGlobal(0,0);
		var dims = this.relativeDims();
		var input = document.getElementById(data.id);
		if(!) {
	        console.log('Creating input...');
	        input = document.createElement('input');
	        input.style.position = 'absolute';
	        input.id = data.id;
	        input.type = data.type;
	        input.style.width = dims.w + 'px';
	        input.style.height = dims.h + 'px';
	        input.style.className = data.class;
	        document.body.appendChild(input);
	    } else {
	        input.style.width=dims.w + 'px';
	        input.style.height=dims.h + 'px';
	    }
	    input.style.marginLeft = ($('#' + this._theme._canvasId).offset().left + point.x + dims.x) + 'px';
	    input.style.marginTop = (point.y + dims.y) + 'px';
	}
});
pluginManager.registerPlugin('input', InputPlugin);