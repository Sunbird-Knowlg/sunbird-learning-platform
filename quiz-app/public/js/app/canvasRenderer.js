Renderer = {
    loader: undefined,
    stage: undefined,
    update: true,
    gdata: undefined,
    placeholderCache: {},
    gameAreaLeft: 0,
    resizeGame: function(disableDraw) {
        console.log('Resizing game...', 'disableDraw', disableDraw);
        var gameArea = document.getElementById('gameArea');
        var widthToHeight = 16 / 9;
        var newWidth = window.innerWidth;
        var newHeight = window.innerHeight;
        var newWidthToHeight = newWidth / newHeight;
        if (newWidthToHeight > widthToHeight) {
            newWidth = newHeight * widthToHeight;
            gameArea.style.height = newHeight + 'px';
            gameArea.style.width = newWidth + 'px';
        } else {
            newHeight = newWidth / widthToHeight;
            gameArea.style.width = newWidth + 'px';
            gameArea.style.height = newHeight + 'px';
        }

        gameArea.style.marginTop = (-newHeight / 2) + 'px';
        gameArea.style.marginLeft = (-newWidth / 2) + 'px';
        Renderer.gameAreaLeft = newWidth / 2;
        Renderer.stage.canvas.width = newWidth;
        Renderer.stage.canvas.height = newHeight;
        if(!disableDraw) Renderer.updateCanvas();
    },
    init: function(data, canvasId) {
        console.log('Initializing....');
        Renderer.gdata = data;
        Renderer.stage = new createjs.Stage(canvasId);
        Renderer.resizeGame(true);
        createjs.Touch.enable(Renderer.stage);
        Renderer.stage.enableMouseOver(10);
        Renderer.stage.mouseMoveOutside = true;
        Renderer.loader = new createjs.LoadQueue(true);
        Renderer.loader.addEventListener("complete", Renderer.startCanvas);
        Renderer.loader.loadManifest(data.manifest, true);
    },
    tick: function(event) {
            // this set makes it so the stage only re-renders when an event handler indicates a change has happened.
        if (Renderer.update) {
            Renderer.update = false; // only update once
            Renderer.stage.update(event);
        }
    },
    getAsset: function(aid) {
        return Renderer.loader.getResult(aid);
    },
    startCanvas: function() {
        console.log('Rendering the canvas');
        Renderer.renderTheme(Renderer.stage, Renderer.gdata.theme);
        Renderer.renderPlaceHolders(Renderer.stage, Renderer.gdata.placeholders);
        createjs.Ticker.addEventListener("tick", Renderer.tick);
    },
    updateCanvas: function() {
        console.log('Re-rendering canvas');
        Renderer.placeholderCache = {};
        Renderer.stage.clear();
        Renderer.stage.removeAllChildren();
        Renderer.renderTheme(Renderer.stage, Renderer.gdata.theme);
        Renderer.renderPlaceHolders(Renderer.stage, Renderer.gdata.placeholders);
        Renderer.stage.update();
    },
    getRelativeDimensions: function(pw, ph, dim) {
        return {
            x: parseFloat(pw * (dim.x || 0)/100),
            y: parseFloat(ph * (dim.y || 0)/100),
            w: parseFloat(pw * (dim.w || 0)/100),
            h: parseFloat(ph * (dim.h || 0)/100)
        }
    },
    renderTheme: function(parent, theme) {
        if (theme.displayObjects && theme.displayObjects.length > 0) {
            theme.displayObjects.forEach(function(disObj) {
                Renderer.renderDisplayObject(parent, parent.canvas.width, parent.canvas.height, disObj);
            });
        }
    },
    renderPlaceHolders: function(parent, placeholders) {
        if (placeholders) {
            for(k in placeholders) {
                var ph = placeholders[k];
                var phCache = Renderer.placeholderCache[k];
                if(!ph.dims) {
                    ph.dims = phCache.dims;
                }
                ph.relDims = Renderer.getRelativeDimensions(phCache.parentW, phCache.parentH, phCache.dims);
                Renderer.renderDisplayObject(phCache.parent, phCache.parentW, phCache.parentH, ph);
            }
        }
    },
    renderDisplayObject: function(parent, parentW, parentH, disObj) {
        disObj.relDims = Renderer.getRelativeDimensions(parentW, parentH, disObj.dims);
        if (disObj.type == 'container') {
            Renderer.renderContainer(parent, disObj);
        } else if (disObj.type == 'asset') {
            Renderer.renderAsset(parent, disObj);
        } else if (disObj.type == 'gridLayout') {
            renderGridLayout(parent, disObj);
        } else if (disObj.type == 'placeholder') {
            Renderer.cachePlaceholder(parent, parentW, parentH, disObj);
        }
    },
    renderContainer: function(parent, disObj) {
        var container = new createjs.Container();
        container.x = disObj.relDims.x;
        container.y = disObj.relDims.y;
        if(disObj.assetId) {
            disObj.relDims.x = 0;
            disObj.relDims.y = 0;
            Renderer.renderAsset(container, disObj);
        }
        if (disObj.displayObjects && disObj.displayObjects.length > 0) {
            disObj.displayObjects.forEach(function(obj) {
                Renderer.renderDisplayObject(container, disObj.relDims.w, disObj.relDims.h, obj);
            })
        }
        parent.addChild(container);
    },
    renderAsset: function(parent, asset) {
        if (asset.assetType == 'rect') {
            parent.addChild(createRect(asset.strokeColor, asset.relDims));
        } else if (asset.assetType == 'image') {
            parent.addChild(createImage(asset.assetId, asset.relDims));
        } else if (asset.assetType == 'text') {
            parent.addChild(createText(asset.assetValue, asset.relDims, asset.font, asset.color));
        } else if(asset.assetType == 'input') {
            var point = parent.localToGlobal(0,0);
            console.log('Point', point);
            var i = createInput(asset, point);
            //parent.addChild(i);
        }
    },
    cachePlaceholder: function(parent, parentW, parentH, disObj) {
        Renderer.placeholderCache[disObj.placeholderId] = disObj;
        Renderer.placeholderCache[disObj.placeholderId].parent = parent;
        Renderer.placeholderCache[disObj.placeholderId].parentW = parentW;
        Renderer.placeholderCache[disObj.placeholderId].parentH = parentH;
    }
}


function createRect(strokeColor, dims) {
    var s = new createjs.Shape();
    s.graphics.setStrokeStyle(2.5, 0, 1);
    s.graphics.beginStroke(strokeColor).drawRoundRect(dims.x, dims.y, dims.w, dims.h, 5);
    return s;
}

function createImage(assetId, dims) {
    var s = new createjs.Bitmap(Renderer.getAsset(assetId));
    var sb = s.getBounds();
    s.x = dims.x;
    s.y = dims.y;
    if (dims.h && dims.h > 0) {
        s.scaleY = dims.h / sb.height;
    }
    if (dims.w && dims.w > 0) {
        s.scaleX = dims.w / sb.width;
    }
    return s;
}

function createText(val, dims, font, color) {
    var text = new createjs.Text(val || '', font || 'bold 20px Arial', color);
    text.x = dims.x;
    text.y = dims.y;
    text.lineWidth = dims.w;
    text.textAlign = 'left';
    text.textBaseline = 'middle'
    return text;
}

function createInput(asset, point) {
    if(!document.getElementById('canvasInput')) {
        console.log('Creating input...');
        var input = document.createElement('input');
        input.style.position = 'absolute';
        input.id = 'canvasInput';
        input.type = 'number';
        input.style.width=asset.relDims.w + 'px';
        input.style.height=asset.relDims.h + 'px';
        input.style.fontSize=asset.fontSize;
        input.style.fontFamily=asset.fontFamily;
        input.style.fontWeight=asset.fontWeight;
        input.style.color=asset.fontColor;
        input.style.padding='10px';
        document.body.appendChild(input);
    } else {
        var input = document.getElementById('canvasInput');
        input.style.width=asset.relDims.w + 'px';
        input.style.height=asset.relDims.h + 'px';
    }
    input.style.marginLeft = ($('#gameCanvas').offset().left + point.x + asset.relDims.x) + 'px';
    input.style.marginTop = (point.y + asset.relDims.y) + 'px';
    /*var content = new createjs.DOMElement("canvasInput");
    console.log('asset.relDims.x', asset.relDims.x, 'asset.relDims.y', asset.relDims.y)
    content.x = (Renderer.gameAreaLeft + asset.relDims.x);
    content.y = asset.relDims.y;
    return content;*/
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

    var x = disObj.relDims.x,
        y = disObj.relDims.y,
        area = disObj.relDims.w * disObj.relDims.h,
        pad = disObj.relDims.pad || 0,
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
	        if ((x + pixelPerImg) > disObj.relDims.w) {
	            x = disObj.relDims.x || 0;
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
