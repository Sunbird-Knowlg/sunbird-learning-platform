var InputPlugin = Plugin.extend({
	_type: 'input',
	_isContainer: false,
    _render: true,
	initPlugin: function(data) {
		var dims = this.relativeDims();
		var input = document.getElementById(data.id);
		if(input) {
			$("#" + data.id).remove();
		}
        input = document.createElement('input');
        input.id = data.id;
        input.type = data.type;
        input.style.width = dims.w + 'px';
        input.style.height = dims.h + 'px';
        input.className = data.class;
        input.style.display = 'none';
        var instance = this;
        var val;
        if (data.model) {
            var model = this.resolveParams(data.model);
            input.onblur = function() {
                instance._stage.setModelValue(model, this.value);
            };
        }
        var div = document.getElementById('gameArea');
        div.insertBefore(input, div.childNodes[0]);
        this._self = new createjs.DOMElement(input);
        this._self.x = dims.x;
        this._self.y = dims.y;
        this._theme.inputs.push(data.id);
	}
});
PluginManager.registerPlugin('input', InputPlugin);