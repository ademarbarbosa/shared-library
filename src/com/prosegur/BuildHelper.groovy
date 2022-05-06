package com.prosegur

class BuildHelper {
  def steps
	
	BuildHelper(steps) {
		this.steps = steps
	} 
  
  def getMavenProfile(def branchName) {
		def mavenProfile_name = "azure-nilo-uat-qa"
    if (branchName.startsWith('development') || branchName.startsWith('development-v')) {
        mavenProfile_name = "azure-nilo-uat-qa"
    } else if (branchName.startsWith('release') || branchName.startsWith('release-v')) {
        mavenProfile_name = "azure-nilo-uat-qa"
    } else if (branchName.startsWith('master') || branchName.name.startsWith('master-v')) {
        mavenProfile_name = "azure-nilo-pro"
    }
    return mavenProfile_name;
  }
	
  def getHelmEnv(def branchName) {
    def helmEnv = "dev"
    if (branchName.startsWith('development') || branchName.startsWith('development-v')) {
	helmEnv = "dev"
    } else if (branchName.startsWith('release') || branchName.startsWith('release-v')) {
	helmEnv = "uat"
    } else if (branchName.startsWith('master') || branchName.startsWith('master-v')) {
	helmEnv = "pro"
    }
    return helmEnv
  }
	
  def getCloudServicePrincipal(def branchName) {
    def azureServicePrincipal_name = "azure-nilo-non-prod"
    if (branchName.startsWith('development') || branchName.startsWith('development-v')) {
	azureServicePrincipal_name = "azure-nilo-non-prod"
    } else if (branchName.startsWith('release') || branchName.startsWith('release-v')) {
	azureServicePrincipal_name = "azure-nilo-non-prod"
    } else if (branchName.startsWith('master') || branchName.startsWith('master-v')) {
	azureServicePrincipal_name = "azure-nilo-prod"
    }
    return azureServicePrincipal_name
  }
	
  def getContainerRegistryName(def branchName) {
    def acrNiloRepository_name = "emaznilopcregistry01" 
    if (branchName.startsWith('development') || branchName.startsWith('release') || branchName.startsWith('development-v') || branchName.startsWith('release-v')) {
      acrNiloRepository_name = "nilocontainerregistry" 
    } else if (branchName.startsWith('master') || branchName.startsWith('master-v')) {
      acrNiloRepository_name = "emaznilopcregistry01" 
    }
    return acrNiloRepository_name;
  }
	
def getContainerRegistryUrl(def branchName) {
    def acrNilorepository_url = "emaznilopcregistry01.azurecr.io"
    if (branchName.startsWith('development') || branchName.startsWith('release') || branchName.startsWith('development-v') || branchName.startsWith('release-v')) {
      acrNilorepository_url = "nilocontainerregistry.azurecr.io"
    } else if (branchName.startsWith('master') || branchName.startsWith('master-v')) {
      acrNilorepository_url = "emaznilopcregistry01.azurecr.io"
    }
    return acrNilorepository_url
  }	
	
	
def getContainerRegistryCredentialFile(def branchName) {
    def acrNilorepositoryCredential_file = "FILE_ACR_NILO_EMAZNILOPCREGISTRY01_CREDENTIAL"
    if (branchName.startsWith('development') || branchName.startsWith('release') || branchName.startsWith('development-v') || branchName.startsWith('release-v')) {
      acrNilorepositoryCredential_file = "NILO-AZURE-DOCKER"
    } else if (branchName.startsWith('master') || branchName.startsWith('master-v')) {
      acrNilorepositoryCredential_file = "FILE_ACR_NILO_EMAZNILOPCREGISTRY01_CREDENTIAL"
    }
    return acrNilorepositoryCredential_file
  }
	
  def getApmEnv(def branchName) {
    def env
    if (branchName.startsWith('development') || branchName.startsWith('development-v')) {
	env = 'DEV'
    } else if (branchName.startsWith('release') || branchName.startsWith('release-v')) {
	env = 'UAT'
    } else if (branchName.startsWith('master') || branchName.startsWith('master-v')) {
	env = 'PRO'
    }
    return env
  }
	
  def connectToArtifactory(def serverId, def artifactoryUrl, def username, def password) {
    rtServer(
                  id: 'Artifactory-1',
                  url: "https://procde.prosegur.com/artifactory",
                  username: "${username}",
                  password: "${password}",
                  bypassProxy: true,
                  timeout: 300
                )
  }
	
  def downloadJarFromArtifactory(def serverId, def jarUrl, def targetDir) {
    rtDownload(
      serverId: serverId,
      spec: """{
      "files": [
	  {
	  "pattern": jarUrl,
	  "target": targetDir
	  }
      ]
      }"""
    )
  }
	
  def getMavenArgs(def profileName, def skipTests, def mavenUserSettings, def mavenGlobalSettings) {
    def SET_MAVEN_LOCALREPO = "-Dmaven.repo.local=/root/.m2/repository"
    def HIDE_DOWNLOADS = "-Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn"
    def MAVEN_SETTINGS = "-s mavenUserSettings -gs mavenGlobalSettings"
    def MAVEN_PROFILE = "-P$profileName"
    def MAVEN_GOAL = 'install deploy:deploy '
    def MAVEN_SKIP_TESTS = '-Dmaven.test.skip=$skipTests'
    def MAVEN_ARGUMENTS = "$SET_MAVEN_LOCALREPO $MAVEN_PROFILE $MAVEN_GOAL $MAVEN_SKIP_TESTS $MAVEN_SETTINGS -B $HIDE_DOWNLOADS "
    return MAVEN_ARGUMENTS;
  }
	
  def movePerfino(def targetDirName) {
    steps.sh """
      mv ./com/perfino/agent/zip/agent.zip ./
      unzip ./agent.zip

      cp -R ./perfino ./$targetDirName/src/main/
      ls -al ./$targetDirName/src/main/perfino
     """
  }
	
  def moveApm(def targetDirName) {
    steps.sh """
      mv ./com/microsoft/azure/applicationinsights-agent/3.2.11 ./applicationinsights-agent-3.2.11.jar
      cp ./applicationinsights-agent-3.2.11.jar ./$targetDirName/src/main/perfino
      cp ./applicationinsights.json ./$targetDirName/src/main/perfino
     """
  }
	
  def loginToCloud(def clientId, def clientSecret, def tenantId, def subscriptionId, def containerRegistryName) {
    steps.sh """
      az login --service-principal -u clientId -p clientSecret -t tenantId
      az account set -s subscriptionId
      az configure --defaults acr=containerRegistryName
      cat $credential_azure > ./config.json
      export DOCKER_CONFIG=\$(pwd)
    """
  }
	
}
