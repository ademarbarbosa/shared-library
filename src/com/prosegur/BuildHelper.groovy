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
  
}
