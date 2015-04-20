module.exports = function(grunt) {
    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),
        nodeunit: {
            all: ['tests/**/Test*.js', 'models/tests/**/*.js'],
            options: {
                reporter: 'junit',
                reporterOptions: {
                    output: 'outputdir'
                }
            }
        },
        copy: {
            dev: {
                files: [{src: ['conf/appConfig_dev.json'], dest: 'conf/appConfig.json'}]
            },
            qa: {
                files: [{src: ['conf/appConfig_qa.json'], dest: 'conf/appConfig.json'}]
            },
            prod: {
                files: [{src: ['conf/appConfig_prod.json'], dest: 'conf/appConfig.json'}]
            }
        },
        'string-replace': {
            dev: {
                files: {
                  'routes/routes.js': 'routes/routes.js'
                },
                options: {
                    replacements: [{
                        pattern: '/oauth2callback',
                        replacement: '/user/google/login/response'
                    }]
                }
            },
            qa: {
                files: {
                  'routes/routes.js': 'routes/routes.js'
                },
                options: {
                    replacements: [{
                        pattern: '/user/google/login/response',
                        replacement: '/oauth2callback'
                    }]
                }
            }
        }
    });

    grunt.loadNpmTasks('grunt-contrib-copy');
    grunt.loadNpmTasks('grunt-string-replace');
    grunt.registerTask('default', ['copy:qa','string-replace:qa']);
};
