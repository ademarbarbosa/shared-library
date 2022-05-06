package com.prosegur

class BuildHelper {
  def steps
	
	BuildHelper(steps) {
		this.steps = steps
	} 
  
  def getMavenProfile(def branchName) {
		def mavenProfile_name = "azure-nilo-uat-qa"
    if (scm.branches[0].name.startsWith('development') || scm.branches[0].name.startsWith('development-v')) {
        mavenProfile_name = "azure-nilo-uat-qa"
    } else if (scm.branches[0].name.startsWith('release') || scm.branches[0].name.startsWith('release-v')) {
        mavenProfile_name = "azure-nilo-uat-qa"
    } else if (scm.branches[0].name.startsWith('master') || scm.branches[0].name.startsWith('master-v')) {
        mavenProfile_name = "azure-nilo-pro"
    }
		return mavenProfile_name;
	}
  
}
