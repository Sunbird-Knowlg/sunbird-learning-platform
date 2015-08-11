var InputPlugin = Plugin.extend({
	_type: 'input',
	_isContainer: false,
	initPlugin: function(data) {
		var dims = this.relativeDims();
		var input = document.getElementById(data.id);
		if(!input) {
	        input = document.createElement('input');
	        input.style.position = 'absolute';
	        input.id = data.id;
	        input.type = data.type;
	        input.style.width = dims.w + 'px';
	        input.style.height = dims.h + 'px';
	        input.className = data.class;
	        document.body.appendChild(input);
	    } else {
	        input.style.width=dims.w + 'px';
	        input.style.height=dims.h + 'px';
	    }
	    input.style.marginLeft = ($('#' + this._theme._canvasId).offset().left + dims.x) + 'px';
	    input.style.marginTop = (dims.y) + 'px';
	    input.style.display = 'none';
	    this._theme.inputs.push(data.id);
	}
});
pluginManager.registerPlugin('input', InputPlugin);