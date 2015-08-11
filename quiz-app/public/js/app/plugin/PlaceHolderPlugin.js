var PlaceHolderPlugin = Plugin.extend({
	_type: 'placeholder',
	_isContainer: false,
	initPlugin: function(data) {
		var dims = this.relativeDims();
		var instance = this;
		if (this._stage._datasource) {
			var datasource = this._theme.getAsset(this._stage._datasource);
			var dataLength = datasource.items.length;
			var count = this._theme._stageRepeatCount[this._stage._data.id];
			if (count >= dataLength) {
				count = 0;
			}
			var dataItem = datasource.items[count];
			instance.param = dataItem.params[data.param];
			instance.renderGridLayout(instance._parent, instance);
		}
	},

	renderGridLayout: function(parent, instance) {
		var computePixel = function(area, repeat) {
        	return Math.floor(Math.sqrt(parseFloat(area / repeat)))
    	}

    	var paddedImageContainer = function(assetId, pad) {
	        var img = new createjs.Bitmap(instance._theme.getAsset(assetId));
	        var imgBounds = img.getBounds();
	        var imgW = imgBounds.width;
	        var imgH = imgBounds.height;
	        img.x = parseFloat(pad / 2);
	        img.y = parseFloat(pad / 2);
	        var imgCont = new createjs.Container();
	        imgCont.addChild(img);
	        imgCont.cache(0, 0, imgW + pad, imgH + pad);
	        return imgCont;
	    }

	    var x = instance._dimensions.x,
        	y = instance._dimensions.y,
        	area = instance._dimensions.w * instance._dimensions.h,
        	pad = instance._dimensions.pad || 0,
        	repeat = instance.param.count;

        // This code assumes that the img aspect ratio is 1. i.e. the image is a square
    	// Hardcoding the cell size adjusting factor to 1.5. Need to invent a new algorithm
    	var pixelPerImg = computePixel(area, repeat || 1) - parseFloat(pad / 1.5);
    	var param = instance.param;
    	param.paddedImg = paddedImageContainer(param.asset, pad);
    	var assetBounds = param.paddedImg.getBounds();
    	var assetW = assetBounds.width,
        assetH = assetBounds.height;
		param.paddedImg.scaleY = parseFloat(pixelPerImg / assetH);
    	param.paddedImg.scaleX = parseFloat(pixelPerImg / assetW);
    	param.paddedImg.x = x + pad;
    	param.paddedImg.y = y + pad;
    	
    	for (i = 0; i < param.count; i++) {
	    	var clonedAsset = param.paddedImg.clone(true);
	        //console.log(x + pixelPerImg, cw);
	        if ((x + pixelPerImg) > instance._dimensions.w) {
	            x = instance._dimensions.x || 0;
	            y += pixelPerImg + pad;
	        }
	        clonedAsset.x = x + pad;
	        clonedAsset.y = y + pad;
	        x += pixelPerImg;
	        parent.addChild(clonedAsset);
	    }
	}
});
pluginManager.registerPlugin('placeholder', PlaceHolderPlugin);