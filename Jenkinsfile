node('build-slave') {
    try {
        ansiColor('xterm') {
            stage('Checkout') {
//                cleanWs()
//                def scmVars = checkout scm
//                checkout scm: [$class: 'GitSCM', branches: [[name: scmVars.GIT_BRANCH]], extensions: [[$class: 'SubmoduleOption', parentCredentials: true, recursiveSubmodules: true]], userRemoteConfigs: [[url: scmVars.GIT_URL]]]
                println 'OK'

            }

         

            stage('Build') {
//                    sh 'mvn clean install -DskipTests'
                println 'OK'

            }

            stage('Post-Build') {
//                    sh """
//                        cd searchIndex-platform/module/search-api/search-manager
//                        mvn play2:dist
//                     """
                println 'OK'
            }

            stage('Post_Build-Action') {
//                jacoco exclusionPattern: '**/common/**,**/dto/**,**/enums/**,**/pipeline/**,**/servlet/**,**/interceptor/**,**/batch/**,**/models/**,**/model/**,**/EnrichActor*.class,**/language/controller/**,**/wordchain/**,**/importer/**,**/Base**,**/ControllerUtil**,**/Indowordnet**,**/Import**'
                println 'OK'
            }

            stage('Archive artifacts'){
                commit_hash = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                branch_name = sh(script: 'git name-rev --name-only HEAD | rev | cut -d "/" -f1| rev', returnStdout: true).trim()
                artifact_version = branch_name + "_" + commit_hash
                sh """
                        mkdir lp_artifacts
                        cp platform-modules/service/target/learning-service.war lp_artifacts
                        cp searchIndex-platform/module/search-api/search-manager/target/search-manager*.zip lp_artifacts
                        zip -r lp_artifacts_$artifact_version lp_artifacts
                        """
            }
        }
    }

    catch (err) {
        currentBuild.result = "FAILURE"
        throw err
    }

}
