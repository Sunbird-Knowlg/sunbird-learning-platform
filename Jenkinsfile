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
                withMaven(tempBinDir: '') {
                    sh 'mvn clean install -DskipTests'
                }
            }

            stage('Post-Build') {
                withMaven(tempBinDir: '') {
                    sh """
                        cd searchIndex-platform/module/search-api/search-manager
                        mvn play2:dist
                     """
                }
            }

            stage('Post_Build-Action') {
                jacoco exclusionPattern: '**/common/**,**/dto/**,**/enums/**,**/pipeline/**,**/servlet/**,**/interceptor/**,**/batch/**,**/models/**,**/model/**,**/EnrichActor*.class,**/language/controller/**,**/wordchain/**,**/importer/**,**/Base**,**/ControllerUtil**,**/Indowordnet**,**/Import**'
            }

            stage('Archive artifacts'){
                commit_hash = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                branch_name = sh(script: 'git name-rev --name-only HEAD | rev | cut -d "/" -f1| rev', returnStdout: true).trim()
                artifact_version = branch_name + "_" + commit_hash
                artifact_name = "learning-service.war:" +  artifact_version
                archiveArtifacts artifacts: 'platform-modules/service/target/learning-service.war', fingerprint: true, onlyIfSuccessful: true
                sh 'echo {\\"artifact_name\\" : \\"${artifact_name}\\", \\"artifact_version\\" : \\"${artifact_version}\\", \\"node_name\\" : \\"${env.NODE_NAME}\\"} > metadata.json'
                archiveArtifacts artifacts: 'metadata.json', onlyIfSuccessful: true
            }
        }
    }

    catch (err) {
        currentBuild.result = "FAILURE"
        throw err
    }

}
