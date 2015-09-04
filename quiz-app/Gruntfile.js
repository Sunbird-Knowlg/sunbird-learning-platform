module.exports = function(grunt) {
    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),
        uglify: {
            js: {
                files: {
                    'www/js/app/quizapp-0.1.min.js': [
                        'www/js/thirdparty/xml2json.js',
                        'www/js/thirdparty/createjs-2015.05.21.min.js',
                        'www/js/thirdparty/cordovaaudioplugin-0.6.1',
                        'www/js/thirdparty/creatine-1.0.0.min.js',
                        'www/js/thirdparty/Class.js',
                        'www/js/app/manager/*.js',
                        'www/js/app/controller/*.js',
                        'www/js/app/evaluator/*.js',
                        'www/js/app/plugin/Plugin.js',
                        'www/js/app/plugin/*Plugin.js',
                        'www/js/app/renderer/*.js'
                    ],
                    'www/js/app/telemetry-lib-0.1.min.js': [
                        'www/js/thirdparty/date-format.js',
                        'www/js/app/telemetry/*.js'
                    ]
                }
            }
        },
        copy: {
            main: {
                files: [
                    {
                        expand: true,
                        cwd: 'www/',
                        src: ['**', '!**/controller/**', '!**/evaluator/**', '!**/manager/**', '!**/plugin/**', '!**/renderer/**', '!**/telemetry/**', '!**/test/**', '!**/libs/**', '!**/jasmine-2.3.4/**', '!**/exclude/**'],
                        dest: 'public/'
                    }
                ]
            }
        },
        remove: {
            fileList: ['public/index.html', 'public/TelemetrySpecRunner.html', 'public/WorksheetSpecRunner.html']
        },
        rename: {
            main: {
                src: 'public/index_min.html',
                dest: 'public/index.html'
            }
        },
        compress: {
            story: {
                options: {
                    archive: 'samples/haircut_story.zip'
                },
                filter: 'isFile',
                expand: true,
                cwd: 'www/stories/haircut_story/',
                src: ['**/*'],
                dest: '/'
            },
            worksheet: {
                options: {
                    archive: 'samples/addition_by_grouping.zip'
                },
                filter: 'isFile',
                expand: true,
                cwd: 'www/worksheets/addition_by_grouping/',
                src: ['**/*'],
                dest: '/'
            }
        },
        aws_s3: {
            aws: grunt.file.readJSON('aws-keys.json'),
            options: {
                accessKeyId: '<%= aws.AWSAccessKeyId %>', // Use the variables
                secretAccessKey: '<%= aws.AWSSecretKey %>', // You can also use env variables
                region: 'ap-southeast-1',
                uploadConcurrency: 5, // 5 simultaneous uploads
                downloadConcurrency: 5 // 5 simultaneous downloads
            },
            uploadJS: {
                options: {
                    bucket: 'ekstep-public',
                    mime: {
                        'www/js/app/quizapp-0.1.min.js': 'application/javascript'
                    }
                },
                files: [{
                    expand: true,
                    cwd: 'www/js/app',
                    src: ['quizapp-0.1.min.js'],
                    dest: 'js/'
                }]
            },
            uploadSamples: {
                options: {
                    bucket: 'ekstep-public',
                    mime: {
                        'samples/haircut_story.zip': 'application/zip',
                        'samples/addition_by_grouping.zip': 'application/zip'
                    }
                },
                files: [{
                    expand: true,
                    cwd: 'samples/',
                    src: ['**'],
                    dest: 'samples/'
                }]
            }
        },
        cordovacli: {
            options: {
                path: 'www',
                cli: 'cordova'  // cca or cordova
            },
            add_plugins: {
                options: {
                    command: 'plugin',
                    action: 'add',
                    plugins: [
                        'device',
                        'file',
                        'media',
                        'splashscreen',
                        'com.ionic.keyboard',
                        'console',
                        'cordova-plugin-whitelist',
                        'cordova-plugin-crosswalk-webview'
                    ]
                }
            },
            add_xwalk: {
                options: {
                    command: 'plugin',
                    action: 'add',
                    plugins: [
                        'cordova-plugin-crosswalk-webview'
                    ]
                }
            },
            rm_xwalk: {
                options: {
                    command: 'plugin',
                    action: 'rm',
                    plugins: [
                        'cordova-plugin-crosswalk-webview'
                    ]
                }
            },
            build_android: {
                options: {
                    command: 'build',
                    platforms: ['android']
                }
            },
            run_android: {
                options: {
                    command: 'run',
                    platforms: ['android']
                }
            }
        }
    });

    grunt.loadNpmTasks('grunt-cordovacli');
    grunt.loadNpmTasks('grunt-contrib-uglify');
    grunt.loadNpmTasks('grunt-contrib-copy');
    grunt.loadNpmTasks('grunt-contrib-compress');
    grunt.loadNpmTasks('grunt-aws-s3');
    grunt.loadNpmTasks('grunt-remove');
    grunt.loadNpmTasks('grunt-rename');

    grunt.registerTask('default', ['uglify:js']);
    grunt.registerTask('build-all', ['uglify:js', 'compress:story', 'compress:worksheet', 'aws_s3:uploadJS', 'aws_s3:uploadSamples']);
    grunt.registerTask('build-js', ['uglify:js', 'aws_s3:uploadJS']);
    grunt.registerTask('build-samples', ['compress:story', 'compress:worksheet', 'aws_s3:uploadSamples']);
};
