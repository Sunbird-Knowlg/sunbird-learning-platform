Renderer = {
    loader: undefined,
    theme: undefined,
    update: false,
    gdata: undefined,
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
        Renderer.theme.updateCanvas(newWidth, newHeight);
        if(!disableDraw) Renderer.theme.reRender();
    },
    init: function(data, canvasId) {
        console.log('Initializing....');
        Renderer.gdata = data;
        data.theme.canvasId = canvasId;
        Renderer.theme = new ThemePlugin(null, null, data.theme);
        Renderer.resizeGame(true);
        Renderer.theme.loader = new createjs.LoadQueue(true, '/assets/');
        Renderer.theme.loader.addEventListener("complete", Renderer.startCanvas);
        Renderer.theme.loader.loadManifest(data.theme.manifest.media, true);
        //createjs.Ticker.addEventListener("tick", Renderer.theme.tick);
    },
    startCanvas: function() {
        console.log('Rendering the canvas');
        Renderer.theme.render();
    }
}
