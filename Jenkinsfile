pipeline {
    agent any
    tools {
        maven 'maven-3.9.9' // Maven version configured in Jenkins Global Tools
    }
    stages {
        stage('Build') {
            steps {
                checkout scmGit(branches: [[name: '*/main']], extensions: [], userRemoteConfigs: [[url: 'https://github.com/yashwanth200/devops-project-1']])
                sh 'mvn clean install'
            }
        }
        stage('Build docker image'){
            steps{
                script{
                    sh 'docker build -t yashwanthvee2001/springboot-jenkins .'
                }
            }
        }
        stage('Push image to docker hub'){
            steps{
                script{
                    withCredentials([string(credentialsId: 'dockerhubpwd', variable: 'dockerhubpwd')]) {
                        sh 'docker login -u yashwanthvee2001 -p ${dockerhubpwd}'
                        }
                        sh 'docker push yashwanthvee2001/springboot-jenkins'
                    }
                }
            }
            stage('Deploy to k8s'){
                        steps{
                            script{
                                kubernetesDeploy (configs: 'deploymentservice.yaml',kubeconfigId: 'k8sconfigpwd')
                            }
                        }
                    }
        }
    }
}
