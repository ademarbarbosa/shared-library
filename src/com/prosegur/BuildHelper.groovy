package com.prosegur

import groovy.json.JsonOutput

class BuildHelper {
  def steps
	
  BuildHelper(steps) {
    this.steps = steps
  } 
  
  def isDevelopmentMode(branchName) {
      if (branchName.startsWith('development')) {
          return true;
      }
  }
  
  def isHomologationMode(branchName) {
      if (branchName.startsWith('release')) {
          return true;
      }
  }
  
  def isProductionMode(branchName) {
      if (branchName.startsWith('master')) {
          return true;
      }
  }
  
  def getMavenProfile(def branchName) {
	def mavenProfile_name = "azure-nilo-uat-qa"
    if (isDevelopmentMode(branchName)) {
        mavenProfile_name = "azure-nilo-uat-qa"
    } else if (isHomologationMode(branchName)) {
        mavenProfile_name = "azure-nilo-uat-qa"
    } else if (isProductionMode(branchName)) {
        mavenProfile_name = "azure-nilo-pro"
    }
    return mavenProfile_name;
  }
	
  def getHelmEnv(def branchName) {
    def helmEnv = "dev"
    if (isDevelopmentMode(branchName)) {
	  helmEnv = "dev"
    } else if (isHomologationMode(branchName)) {
	  helmEnv = "uat"
    } else if (isProductionMode(branchName)) {
	  helmEnv = "pro"
    }
    return helmEnv
  }
	
  def getCloudServicePrincipal(def branchName) {
    def azureServicePrincipal_name = "azure-nilo-non-prod"
    if (isDevelopmentMode(branchName)) {
	  azureServicePrincipal_name = "azure-nilo-non-prod"
    } else if (isHomologationMode(branchName)) {
	  azureServicePrincipal_name = "azure-nilo-non-prod"
    } else if (isProductionMode(branchName)) {
	  azureServicePrincipal_name = "azure-nilo-prod"
    }
    return azureServicePrincipal_name
  }
	
  def getContainerRegistryName(def branchName) {
    def acrNiloRepository_name = "emaznilopcregistry01" 
    if (isDevelopmentMode(branchName) || isHomologationMode(branchName)) {
      acrNiloRepository_name = "nilocontainerregistry" 
    } else if (isProductionMode(branchName)) {
      acrNiloRepository_name = "emaznilopcregistry01" 
    }
    return acrNiloRepository_name;
  }
	
  def getContainerRegistryUrl(def branchName) {
    def acrNilorepository_url = "emaznilopcregistry01.azurecr.io"
    if (isDevelopmentMode(branchName) || isHomologationMode(branchName)) {
      acrNilorepository_url = "nilocontainerregistry.azurecr.io"
    } else if (isProductionMode(branchName)) {
      acrNilorepository_url = "emaznilopcregistry01.azurecr.io"
    }
    return acrNilorepository_url
  }	
	
	
  def getContainerRegistryCredentialFile(def branchName) {
    def acrNilorepositoryCredential_file = "FILE_ACR_NILO_EMAZNILOPCREGISTRY01_CREDENTIAL"
    if (isDevelopmentMode(branchName) || isHomologationMode(branchName)) {
      acrNilorepositoryCredential_file = "NILO-AZURE-DOCKER"
    } else if (isProductionMode(branchName)) {
      acrNilorepositoryCredential_file = "FILE_ACR_NILO_EMAZNILOPCREGISTRY01_CREDENTIAL"
    }
    return acrNilorepositoryCredential_file
  }
	
  def getApmEnv(def branchName) {
    def env
    if (isDevelopmentMode(branchName)) {
	  env = 'DEV'
    } else if (isHomologationMode(branchName)) {
	  env = 'UAT'
    } else if (isProductionMode(branchName)) {
	  env = 'PRO'
    }
    return env
  }
	
  def connectToArtifactory(def serverId, def artifactoryUrl, def username, def password) {
    steps.rtServer(
      id: serverId,
      url: artifactoryUrl,
      username: username,
      password: password,
      bypassProxy: true,
      timeout: 300
    )
  }
	
  def downloadJarFromArtifactory(def serverId, def jarUrl, def targetDir) {
    def json = """{ "files": [ { "pattern": "${jarUrl}", "target": "${targetDir}" } ] }"""
    steps.rtDownload(
      serverId: serverId,
      spec: json
    )
  }
	
  def getMavenArgs(def profileName, def skipTests, def mavenUserSettings, def mavenGlobalSettings) {
    def SET_MAVEN_LOCALREPO = "-Dmaven.repo.local=/root/.m2/repository"
    def HIDE_DOWNLOADS = "-Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn"
    def MAVEN_SETTINGS = """-s ${mavenUserSettings} -gs ${mavenGlobalSettings}"""
    def MAVEN_PROFILE = """-P${profileName}"""
    def MAVEN_GOAL = 'install deploy:deploy '
    def MAVEN_SKIP_TESTS = """-Dmaven.test.skip=${skipTests}"""
    def MAVEN_ARGUMENTS = """${SET_MAVEN_LOCALREPO} ${MAVEN_PROFILE} ${MAVEN_GOAL} ${MAVEN_SKIP_TESTS} ${MAVEN_SETTINGS} -B ${HIDE_DOWNLOADS} """
    return MAVEN_ARGUMENTS;
  }
	
  def movePerfino(def targetDirName) {
    steps.sh """
      mv ./com/perfino/agent/zip/agent.zip ./
      unzip ./agent.zip

      cp -R ./perfino ./$targetDirName/src/main/
     """
  }
	
  def moveApm(def targetDirName) {
    steps.sh """
      mv ./com/microsoft/azure/applicationinsights-agent/3.2.11 ./applicationinsights-agent-3.2.11.jar
      cp ./applicationinsights-agent-3.2.11.jar ./$targetDirName/src/main/perfino
      cp ./applicationinsights.json ./$targetDirName/src/main/perfino
     """
  }
	
  def loginToCloud(def clientId, def clientSecret, def tenantId, def subscriptionId, def containerRegistryName, def credential_azure) {
    steps.sh """
      az login --service-principal -u ${clientId} -p ${clientSecret} -t ${tenantId}
      az account set -s ${subscriptionId}
      az configure --defaults acr=${containerRegistryName}

      cat ${credential_azure} > ./config.json
    """
  }
  
  def buildProject(def apmEnv, def helmEnv, def mavenArguments, def perfinoFlags, def apmFlags, def projectName) {
    steps.sh """
      export DOCKER_CONFIG=\$(pwd)
      mvn -Djib.console=plain -Denv=${apmEnv} -DhelmEnv=${helmEnv} ${mavenArguments} com.google.cloud.tools:jib-maven-plugin:build -Dperfino.jvm.flags=${perfinoFlags} -Dapp.insights.jvm.flags=${apmFlags} -f ${projectName}/pom.xml
    """
  }
	
}
