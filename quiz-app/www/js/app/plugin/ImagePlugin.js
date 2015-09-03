var ImagePlugin = Plugin.extend({
    _type: 'image',
    _isContainer: false,
    _render: true,
    initPlugin: function(data) {
        var instance = this;
        var s = new createjs.Bitmap(this._theme.getAsset(data.asset));
        var dims = this.relativeDims();
        var sb = s.getBounds();
        s.x = dims.x;
        s.y = dims.y;
        if (dims.h && dims.h > 0) {
            s.scaleY = dims.h / sb.height;
        }
        if (dims.w && dims.w > 0) {
            s.scaleX = dims.w / sb.width;
        }
        this._self = s;
        this._self.origX = dims.x;
        this._self.origY = dims.y;
        this._self.width = dims.w;
        this._self.height = dims.h;
        if (data.type == 'choice') {
            this._stage._choices.push(this);
        }
        if (data.enableDrag) {
            this.enableDrag(this._self, data.snapTo);
        }
    },
    toggleShadow: function() {
        if (this._stage._choices) {
            this._stage._choices.forEach(function(choice) {
                choice.removeShadow();
            });
        }
        if (this._self.shadow) {
            this._self.shadow = undefined;
        } else {
            this._self.shadow = new createjs.Shadow(this._data.shadowColor, 0, 0, 30);
        }
        Renderer.update = true;
    },
    removeShadow: function() {
        this._self.shadow = undefined;
    },
    enableDrag: function(asset, snapTo) {
        asset.cursor = "pointer";
        asset.on("mousedown", function(evt) {
            this.parent.addChild(this);
            this.offset = {
                x: this.x - evt.stageX,
                y: this.y - evt.stageY
            };
        });
        asset.on("pressmove", function(evt) {
            this.x = evt.stageX + this.offset.x;
            this.y = evt.stageY + this.offset.y;
            Renderer.update = true;
        });
        if (snapTo) {
            asset.on("pressup", function(evt) {
                var plugin = PluginManager.getPluginObject(snapTo);
                var dims = plugin._dimensions;
                var x = dims.x,
                    y = dims.y,
                    maxX = dims.x + dims.w,
                    maxY = dims.y + dims.h;
                var snapSuccess = false;
                if (this.x >= x && (this.x + this.width) <= maxX) {
                    if (this.y >= y && (this.y + this.width) <= maxY) {
                        snapSuccess = true;
                    }
                }
                if (!snapSuccess) {
                    this.x = this.origX;
                    this.y = this.origY;
                } else {
                    if (plugin._data.snapX) {
                        this.x = dims.x + (dims.w * plugin._data.snapX / 100);
                    }
                    if (plugin._data.snapY) {
                        this.y = dims.y + (dims.w * plugin._data.snapY / 100);
                    }
                }
                Renderer.update = true;
            });
        }
    }
});
PluginManager.registerPlugin('image', ImagePlugin);