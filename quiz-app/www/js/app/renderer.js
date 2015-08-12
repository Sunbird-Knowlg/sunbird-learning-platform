Renderer = {
    loader: undefined,
    stage: undefined,
    update: true,
    gdata: undefined,
    cw: undefined,
    ch: undefined,
    init: function(data, canvasId) {
        Renderer.gdata = data;
        Renderer.stage = new createjs.Stage(canvasId);
        createjs.Touch.enable(Renderer.stage);
        Renderer.stage.enableMouseOver(10);
        Renderer.stage.mouseMoveOutside = true;
        Renderer.cw = Renderer.stage.canvas.width;
        Renderer.ch = Renderer.stage.canvas.height;
        Renderer.loader = new createjs.LoadQueue(true);
        Renderer.loader.addEventListener("complete", Renderer.start);
        Renderer.loader.loadManifest(data.manifest, true);
    },
    getAsset: function(aid) {
        return Renderer.loader.getResult(aid);
    },
    start: function() {
        renderTemplate(Renderer.stage, Renderer.gdata.template);
        createjs.Ticker.addEventListener("tick", tick);
        Renderer.stage.update();
    }
}

function renderBackground(parent, bgdata) {
    if (bgdata) {
        var bm = new createjs.Shape();
        bm.graphics.beginBitmapFill(Renderer.getAsset(bgdata.assetId)).drawRect(0, 0, Renderer.cw, Renderer.ch);
        parent.addChild(bm);
    }
}

function renderTemplate(parent, template) {
    renderBackground(parent, template.bg);
    if (template.displayObjects && template.displayObjects.length > 0) {
        template.displayObjects.forEach(function(disObj) {
            renderDisplayObject(parent, disObj);
        });
    }
}

function renderDisplayObject(parent, displayObject) {
    if (displayObject.type == 'container') {
        renderContainer(parent, displayObject);
    } else if (displayObject.type == 'asset') {
        renderAsset(parent, displayObject);
    } else if (displayObject.type == 'gridLayout') {
        renderGridLayout(parent, displayObject);
    }
}

function renderContainer(parent, disObj) {
    var container = new createjs.Container();
    container.x = disObj.layout.x || 0;
    container.y = disObj.layout.y || 0;
    disObj.layout.x = 0;
    disObj.layout.y = 0;
    renderAsset(container, disObj);
    if (disObj.displayObjects && disObj.displayObjects.length > 0) {
        disObj.displayObjects.forEach(function(obj) {
            renderDisplayObject(container, obj);
        })
    }
    parent.addChild(container);
}

function renderAsset(parent, asset) {
    if (!asset.layout) {
        return;
    }
    var layout = asset.layout;
    if (layout.type == 'rect') {
        parent.addChild(createRect(layout.strokeColor, layout.x || 0, layout.y || 0, layout.w, layout.h));
    } else if (layout.type == 'image') {
        parent.addChild(createImage(layout.assetId, layout.x || 0, layout.y || 0, layout.w, layout.h));
    } else if (layout.type == 'text') {
        parent.addChild(createText(layout.assetId, layout.x || 0, layout.y || 0, layout.w, layout.font, layout.color));
    }
}

function tick(event) {
    // this set makes it so the stage only re-renders when an event handler indicates a change has happened.
    if (Renderer.update) {
        Renderer.update = false; // only update once
        Renderer.stage.update(event);
    }
}


function createRect(strokeColor, x, y, w, h) {
    var s = new createjs.Shape();
    s.graphics.setStrokeStyle(2.5, 0, 1);
    s.graphics.beginStroke(strokeColor).drawRoundRect(x, y, w, h, 5);
    return s;
}

function createImage(assetId, x, y, w, h) {
    var s = new createjs.Bitmap(Renderer.getAsset(assetId));
    var sb = s.getBounds();
    s.x = x;
    s.y = y;
    if (h) {
        s.scaleY = h / sb.height;
    }
    if (h) {
        s.scaleX = w / sb.width;
    }
    return s;
}

function createText(val, x, y, w, font, color) {
    var text = new createjs.Text(val, font || 'bold 20px Arial', color);
    text.x = x;
    text.y = y;
    text.lineWidth = w;
    text.textAlign = 'left';
    text.textBaseline = 'middle'
    return text;
}

function renderGridLayout(container, disObj) {

    var computePixel = function(area, repeat) {
        return Math.floor(Math.sqrt(parseFloat(area / repeat)))
    }

    var paddedImageContainer = function(assetId, pad) {

        var img = new createjs.Bitmap(Renderer.getAsset(assetId));
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

    var enableDrag = function(asset) {
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
        asset.on("pressup", function(evt) {
        });
    }

    var x = disObj.x || 0,
        y = disObj.y || 0,
        area = disObj.w * disObj.h,
        pad = disObj.pad || 0,
        repeat = _.reduce(_.pluck(disObj.assets, 'repeat'), function(memo, num){ return memo + num; }, 0);

    // This code assumes that the img aspect ratio is 1. i.e. the image is a square
    // Hardcoding the cell size adjusting factor to 1.5. Need to invent a new algorithm
    var pixelPerImg = computePixel(area, repeat || 1) - parseFloat(pad / 1.5);

    disObj.assets.forEach(function(asset) {
    	asset.paddedImg = paddedImageContainer(asset.id, pad);
    	var assetBounds = asset.paddedImg.getBounds();
    	var assetW = assetBounds.width,
        assetH = assetBounds.height;
		asset.paddedImg.scaleY = parseFloat(pixelPerImg / assetH);
    	asset.paddedImg.scaleX = parseFloat(pixelPerImg / assetW);
    	asset.paddedImg.x = x + pad;
    	asset.paddedImg.y = y + pad;
    });

    disObj.assets.forEach(function(asset) {
    	for (i = 0; i < asset.repeat; i++) {
	    	var clonedAsset = asset.paddedImg.clone(true);
	        //console.log(x + pixelPerImg, cw);
	        if ((x + pixelPerImg) > disObj.w) {
	            x = disObj.x || 0;
	            y += pixelPerImg + pad;
	        }
	        clonedAsset.x = x + pad;
	        clonedAsset.y = y + pad;
	        x += pixelPerImg;
	        if (disObj.enableDrag) {
	            enableDrag(clonedAsset);
	        }
	        container.addChild(clonedAsset);
	    }
    });
}
