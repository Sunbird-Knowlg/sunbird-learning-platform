module.exports = function(grunt) {
    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),
        uglify: {
            js: {
                files: {
                    'www/js/quizapp.min.js': [
                        'www/js/thirdparty/xml2json.js',
                        'www/js/thirdparty/createjs-2015.05.21.min.js',
                        'www/js/thirdparty/creatine-1.0.0.min.js',
                        'www/js/thirdparty/Class.js',
                        'www/js/app/plugin/PluginManager.js',
                        'www/js/app/plugin/CommandManager.js',
                        'www/js/app/plugin/Plugin.js',
                        'www/js/app/plugin/*Plugin.js',
                        'www/js/app/renderer/*.js'
                    ]
                }
            }
        }
    });

    grunt.loadNpmTasks('grunt-contrib-uglify');
    grunt.registerTask('default', ['uglify:js']);
};
