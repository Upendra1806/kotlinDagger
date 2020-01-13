pipeline {
    agent any
    tools {
        gradle "Gradle 5.5"
    }
    environment {
        ANDROID_HOME='/usr/local/share/android-sdk'
        APP_ID=''
        GRADLE_TASK=''
        FILE_PATH=''
        BUILD_CODE=''
    }
    stages {
        stage('Fetch Sources') {
            steps{
                git branch: 'develop', url: 'https://buildbotadviscent@bitbucket.org/adviscent1/jpm-android.git'
            }
        }

        stage('Prepare Environment') {
            steps {

                sh "sed -i.bak \"s/\\(^[ ]*versionCode \\)\\(.*\\)/\\1${BUILD_NUMBER}/;s/\\(^[ ]*versionName \\)\\(.*\\)/\\1\\\"${VERSION}\\\"/\" app/build.gradle"

                script {
//                    BUILD_CODE = sh returnStdout: true, script:"printf `date +%Y%m%d`${BUILD_NUMBER}"

                    switch (TYPE){
                        case 'DEV':
                            APP_ID = '774dec4db1384a79a1f425638b7ad2ca'//edited
                            GRADLE_TASK= 'assembleDevDebug'
                            FILE_PATH= "app/build/outputs/apk/dev/debug/*.apk"
                            break
                        case 'TEST':
                            APP_ID = 'e5083418b572424abbd21beb1e125dc2'
                            GRADLE_TASK= 'assembleDevTestDebug'
                            FILE_PATH= "app/build/outputs/apk/devTest/debug/*.apk"
                            break
                        case 'PROD_TEST':
                            APP_ID = 'f10f93a6a962477a98714e44879cca89'
                            GRADLE_TASK= 'assembleProdTestDebug'
                            FILE_PATH= "app/build/outputs/apk/prodTest/debug/*.apk"
                            break
                        case 'PROD':
                            APP_ID = '54a11015bdb14c74a48d2fe6063afa6d'
                            GRADLE_TASK= 'assembleProdDebug'
                            FILE_PATH= "app/build/outputs/apk/prod/debug/*.apk"
                            break
                    }
                }
            }
        }

        stage('Build') {
            steps {

                sh "gradle clean :app:${GRADLE_TASK}"

            }
        }

        stage('Deploy') {
            steps {

                hockeyApp applications: [
                        [
                                apiToken: 'ff85c8a801c04d178a0228ff50c330d7',
                                downloadAllowed: true,
                                filePath: FILE_PATH,
                                releaseNotesMethod: none(),
                                uploadMethod: versionCreation(APP_ID)
                        ]
                ], baseUrlHolder:null, debugMode: false, failGracefully: false

            }
        }

        stage('Notify') {
            steps {
                office365ConnectorSend color: "ee9a49",
                        message: "JPM Android ${TYPE} ${VERSION} (${BUILD_NUMBER}) <${HOCKEYAPP_INSTALL_URL}>",
                        status: 'Succes',
                        webhookUrl: 'https://outlook.office.com/webhook/f907094f-a61a-4582-a2e3-7ef08262f32f@b2af40a2-dc4b-480c-96d0-7ec93fd443a3/JenkinsCI/db1f636a604840bb8428d99028ae8c5a/4a810d64-c510-4007-807a-0cd88152e54b'
            }
        }
    }
}