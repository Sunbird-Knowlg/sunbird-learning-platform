Renderer = {
    loader: undefined,
    theme: undefined,
    update: false,
    gdata: undefined,
    resizeGame: function(disableDraw) {
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
        Renderer.theme.updateCanvas(newWidth, newHeight);
        if(!disableDraw) Renderer.theme.reRender();
    },
    start: function(gameRelPath, canvasId) {
        Renderer.initByJSON(gameRelPath, canvasId);
    },
    initByJSON: function(gameRelPath, canvasId) {
        $.get(gameRelPath + '/markup.json', function(data) {
            Renderer.init(data, canvasId, gameRelPath);
        })
        .fail(function() {
            Renderer.initByXML(gameRelPath, canvasId)
        });
    },
    initByXML: function(gameRelPath, canvasId) {
        $.get(gameRelPath + '/markup.xml', function(data) {
            Renderer.init(data, canvasId, gameRelPath);
        })
        .fail(function(err) {
            alert("Unable to render.", err);
        });
    },
    init: function(data, canvasId, gameRelPath) {
        if(!$.isPlainObject(data)) {
            var x2js = new X2JS({attributePrefix: 'none'});
            data = x2js.xml2json(data);
        }
        Renderer.gdata = data;
        data.theme.canvasId = canvasId;
        Renderer.theme = new ThemePlugin(data.theme);
        Renderer.resizeGame(true);
        // if(Renderer.theme.loader) Renderer.theme.loader.destroy();
        if(gameRelPath) {
            Renderer.theme.loader = new createjs.LoadQueue(true, gameRelPath + "/assets/");
        } else {
            Renderer.theme.loader = new createjs.LoadQueue(true);
        }
        Renderer.theme.loader.setMaxConnections(data.theme.manifest.media.length);
        Renderer.theme.loader.addEventListener("complete", Renderer.startCanvas);
        Renderer.theme.loader.installPlugin(createjs.Sound);
        Renderer.theme.loader.loadManifest(data.theme.manifest.media, true);
        createjs.Ticker.addEventListener("tick", function() {
            Renderer.theme.update();
        });
    },
    startCanvas: function() {
        Renderer.theme.render();
        $('#gameAreaLoad').hide();
    }
}
