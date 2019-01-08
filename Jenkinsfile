node('build-slave') {
    try {
        ansiColor('xterm') {
            stage('Checkout') {
                cleanWs()
                def scmVars = checkout scm
                checkout scm: [$class: 'GitSCM', branches: [[name: scmVars.GIT_BRANCH]], extensions: [[$class: 'SubmoduleOption', parentCredentials: true, recursiveSubmodules: true]], userRemoteConfigs: [[url: scmVars.GIT_URL]]]

            }

            stage('Pre-Build') {
                sh """
                java -version
                rm -rf /data/logs/*
                rm -rf /data/graphDB/*
                rm -rf /data/testgraphDB/*
                rm -rf /data/testGraphDB/*
                vim -esnc '%s/dialcode.es_conn_info="localhost:9200"/dialcode.es_conn_info="10.6.0.11:9200"/g|:wq' platform-core/unit-tests/src/test/resources/application.conf
                vim -esnc '%s/search.es_conn_info="localhost:9200"/search.es_conn_info="10.6.0.11:9200"/g|:wq' platform-core/unit-tests/src/test/resources/application.conf
                vim -esnc '%s/search.es_conn_info="localhost:9200"/search.es_conn_info="10.6.0.11:9200"/g|:wq' searchIndex-platform/module/search-api/search-manager/conf/application.conf
                """
            }

            stage('Build') {
                sh 'mvn clean install -DskipTests'
                withMaven(options: [dependenciesFingerprintPublisher(includeReleaseVersions: true, includeScopeTest: true), artifactsPublisher(), junitPublisher(healthScaleFactor: 1.0, keepLongStdio: true), pipelineGraphPublisher(ignoreUpstreamTriggers: true, includeReleaseVersions: true, includeScopeTest: true, lifecycleThreshold: 'install', skipDownstreamTriggers: true), jacocoPublisher(), invokerPublisher(), jgivenPublisher(), concordionPublisher(), mavenLinkerPublisher(), spotbugsPublisher(), openTasksPublisher(), findbugsPublisher()], tempBinDir: '')
}
            }

            stage('Post-Build') {
                sh """
                cd searchIndex-platform/module/search-api/search-manager
                mvn play2:dist
            """
            }

            stage('Post_Build-Action') {
                jacoco exclusionPattern: '**/common/**,**/dto/**,**/enums/**,**/pipeline/**,**/servlet/**,**/interceptor/**,**/batch/**,**/models/**,**/model/**,**/EnrichActor*.class,**/language/controller/**,**/wordchain/**,**/importer/**,**/Base**,**/ControllerUtil**,**/Indowordnet**,**/Import**'
            }
        }
    }

    catch (err) {
        currentBuild.result = "FAILURE"
        throw err
    }

}
