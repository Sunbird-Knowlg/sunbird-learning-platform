var TweenPlugin = AnimationPlugin.extend({
    _animateFn: undefined,
    initPlugin: function(data, plugin) {
        var to = data.to;
        if(!_.isArray(to)) {
            to = [to];
        }
        var fn = '(function() {return function(plugin){';
        fn += 'createjs.Tween.get(plugin, {override:true})';
        to.forEach(function(to) {
            var data = JSON.parse(to.__cdata);
            var relDims = plugin.getRelativeDims(data);
            data.x = relDims.x;
            data.y = relDims.y;
            data.width = relDims.w;
            data.height = relDims.h;
            fn += '.to(' + JSON.stringify(data) + ',' + to.duration + ', createjs.Ease.' + to.ease + ')';
        });
        fn += '.addEventListener("change", function(event) {Renderer.update = true;';
        if(data.widthChangeEvent) {
            fn += 'AnimationManager.widthHandler(event, plugin);';
        }
        fn += '})}})()';
        this._animateFn = fn;
        this.animate(plugin);
    },
    animate: function(plugin) {
        var animationFn = eval(this._animateFn);
        animationFn.apply(null, [plugin._self]);
    }
});
AnimationManager.registerPlugin('tween', TweenPlugin);