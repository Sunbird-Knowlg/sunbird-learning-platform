module.exports = function(grunt) {
    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),
        uglify: {
            js: {
                files: {
                    'public/js/app/quizapp-0.3.min.js': [
                        'public/js/thirdparty/exclude/xml2json.js',
                        'public/js/thirdparty/exclude/createjs-2015.05.21.min.js',
                        'public/js/thirdparty/exclude/cordovaaudioplugin-0.6.1.min.js',
                        'public/js/thirdparty/exclude/creatine-1.0.0.min.js',
                        'public/js/thirdparty/exclude/Class.js',
                        'public/js/app/controller/Controller.js',
                        'public/js/app/plugin/Plugin.js',
                        'public/js/app/manager/*.js',
                        'public/js/app/controller/*Controller.js',
                        'public/js/app/generator/*.js',
                        'public/js/app/evaluator/*.js',
                        'public/js/app/plugin/*Plugin.js',
                        'public/js/app/renderer/*.js',
                        'public/js/app/cordova-plugin/DownloaderService.js'
                    ],
                    'public/js/app/telemetry-lib-0.3.min.js': [
                        'public/js/thirdparty/exclude/date-format.js',
                        'public/js/app/telemetry/FilewriterService.js',
                        'public/js/app/telemetry/TelemetryEvent.js',
                        'public/js/app/telemetry/*.js'
                    ]
                }
            }
        },
        copy: {
            main: {
                files: [
                    {
                        expand: true,
                        cwd: 'public/',
                        src: ['**', '!**/controller/**', '!**/evaluator/**', '!**/manager/**', '!**/plugin/**', '!**/renderer/**', '!**/generator/**', '!**/telemetry/**', '!**/test/**', '!**/tests/**', '!**/libs/**', '!**/jasmine-2.3.4/**', '!**/exclude/**'],
                        dest: 'www/'
                    },
                    {
                        expand: true,
                        cwd: 'public/',
                        src: 'build-extras.gradle',
                        dest: 'platforms/android/'
                    }
                ]
            }
        },
        clean: {
            before: ["www", "platforms/android/assets/www", "platforms/android/build"],
            after: ["www/TelemetrySpecRunner.html", "www/WorksheetSpecRunner.html"],
            samples: ["www/stories", "www/worksheets"]
        },
        rename: {
            main: {
                src: 'www/index_min.html',
                dest: 'www/index.html'
            }
        },
        compress: {
            story: {
                options: {
                    archive: 'samples/haircut_story_0.2.zip'
                },
                filter: 'isFile',
                expand: true,
                cwd: 'public/stories/haircut_story/',
                src: ['**/*'],
                dest: '/'
            },
            worksheet: {
                options: {
                    archive: 'samples/addition_by_grouping_0.2.zip'
                },
                filter: 'isFile',
                expand: true,
                cwd: 'public/worksheets/addition_by_grouping/',
                src: ['**/*'],
                dest: '/'
            }
        },
        aws_s3: {
            options: {
                accessKeyId: process.env.AWSAccessKeyId, // Use the variables
                secretAccessKey: process.env.AWSSecretKey, // You can also use env variables
                region: 'ap-southeast-1',
                uploadConcurrency: 5, // 5 simultaneous uploads
                downloadConcurrency: 5 // 5 simultaneous downloads
            },
            uploadJS: {
                options: {
                    bucket: 'ekstep-public',
                    mime: {
                        'public/js/app/quizapp-0.3.min.js': 'application/javascript',
                        'public/js/app/telemetry-lib-0.3.min.js': 'application/javascript'
                    }
                },
                files: [{
                    expand: true,
                    cwd: 'public/js/app/',
                    src: ['*-0.3.min.js'],
                    dest: 'js/'
                }]
            },
            uploadSamples: {
                options: {
                    bucket: 'ekstep-public',
                    mime: {
                        'samples/haircut_story_0.2.zip': 'application/zip',
                        'samples/addition_by_grouping_0.2.zip': 'application/zip'
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
            rm_custom_plugins: {
                options: {
                    command: 'plugin',
                    action: 'rm',
                    plugins: [
                        'org.ekstep.platform.service.plugin',
                        'org.ekstep.downloader.service.plugin',
                        'org.ekstep.genie.service.plugin'
                    ]
                }
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
                        'cordova-plugin-crosswalk-webview',
                        'cordova-plugin-file-transfer',
                        'https://github.com/Initsogar/cordova-webintent.git',
                        'com.lampa.startapp'
                    ]
                }
            },
            add_custom_plugins: {
                options: {
                    command: 'plugin',
                    action: 'add',
                    plugins: [
                        'custom-plugins/PlatformService/',
                        'custom-plugins/DownloaderService/',
                        'custom-plugins/GenieService/'
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
    grunt.loadNpmTasks('grunt-contrib-clean');
    grunt.loadNpmTasks('grunt-rename');

    grunt.registerTask('default', ['uglify:js']);
    grunt.registerTask('build-all', ['uglify:js', 'compress:story', 'compress:worksheet', 'aws_s3:uploadJS', 'aws_s3:uploadSamples']);
    grunt.registerTask('build-js', ['uglify:js', 'aws_s3:uploadJS']);
    grunt.registerTask('build-samples', ['compress:story', 'compress:worksheet', 'aws_s3:uploadSamples']);
    grunt.registerTask('build-apk-xwalk', ['uglify:js', 'clean:before', 'copy', 'rename', 'clean:after', 'cordovacli:add_plugins', 'cordovacli:build_android']);
    grunt.registerTask('build-apk', ['uglify:js', 'clean:before', 'copy', 'rename', 'clean:after', 'clean:samples', 'cordovacli:add_plugins', 'cordovacli:rm_xwalk', 'cordovacli:build_android']);
    grunt.registerTask('build-apk-quick', ['uglify:js', 'clean:before', 'copy', 'rename', 'clean:after', 'clean:samples', 'cordovacli:build_android']);
    grunt.registerTask('install-apk-xwalk', ['uglify:js', 'clean:before', 'copy', 'rename', 'clean:after', 'clean:samples', 'cordovacli:add_plugins', 'cordovacli:run_android']);
    grunt.registerTask('install-apk', ['uglify:js', 'clean:before', 'copy', 'rename', 'clean:after', 'clean:samples', 'cordovacli:add_plugins', 'cordovacli:rm_xwalk', 'cordovacli:run_android']);
    grunt.registerTask('install-apk-quick', ['uglify:js', 'clean:before', 'copy', 'rename', 'clean:after', 'clean:samples', 'cordovacli:run_android']);
    grunt.registerTask('rm_custom_plugins', ['cordovacli:rm_custom_plugins']);
    grunt.registerTask('add_custom_plugins', ['cordovacli:add_custom_plugins']);
    grunt.registerTask('update_custom_plugins', ['cordovacli:rm_custom_plugins', 'cordovacli:add_custom_plugins']);
};
