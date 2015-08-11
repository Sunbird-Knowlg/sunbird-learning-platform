var Plugin = Class.extend({
	_theme: undefined,
	_parent: undefined,
	_data: undefined,
	_currIndex: 0,
	_index: 0,
	_self: undefined,
	_dimensions: undefined,
	init: function(theme, parent, data) {
		this._theme = theme;
		this._parent = parent;
		this._data = data;
		this.initPlugin(data);
		if(this._data.events) {
			this.handleEvents(data.events);
		}
	},
	setIndex: function(idx) {
		this._index = idx;
	},
	addChild: function(child, childPlugin) {
		var nextIdx = this._currIndex++;
		this._self.addChildAt(child, nextIdx);
		childPlugin.setIndex(nextIdx);
	},
	removeChild: function(idx) {
		this._self.removeChildAt(idx);
	},
	render: function() {
		this._parent.addChild(this._self, this);
	},
	update: function() {
		this._theme.update();
	},
	dimensions: function() {
		return this._dimensions;
	},
	relativeDims: function() {
		var parentDims = this._parent.dimensions();
		this._dimensions = {
            x: parseFloat(parentDims.w * (this._data.x || 0)/100),
            y: parseFloat(parentDims.h * (this._data.y || 0)/100),
            w: parseFloat(parentDims.w * (this._data.w || 0)/100),
            h: parseFloat(parentDims.h * (this._data.h || 0)/100)
        }
        return this._dimensions;
	},
	initPlugin: function(data) {
		throw "Subclasses of plugin should implement this function";
	},
	handleEvents: function(events) {

	}
})