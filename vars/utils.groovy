def getCloudCI() {
  return "CASH-CI"
}

def getContainer(def imageName, def imageUrl, def cpu, def memory) {
  return """
    - name: ${imageName}
      image: ${imageUrl}
      args: ['cat']
      tty: true
      resources:
        limits: {}
        requests: 
          cpu: ${cpu}
          memory: ${memory}
   """
  }

  def getImagePullSecret() {
    return "docker-global-prosegur"  
  }
