package rconnect

import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.rosuda.REngine.Rserve.RConnection

class RoperationsService {

    final static String fileDir = ConfigurationHolder?.config?.Rserve?.fileDir?:"/R/files"
    final static String imageDir = ConfigurationHolder?.config?.Rserve?.imageDir?:"/R/images"
    def RconnectService

    /**
     * Method to set the working directory in R to the one stored in the flow scope.
     * @param rConnection
     * @param flow
     * @return
     */
    def setWorkingDirectory(def rConnection, def rFolder) {
        rConnection.assign("folder", rFolder)
        rConnection.voidEval("setwd(folder)")
    }

    /**
     * Create and return a reusable temp folder on the R server.
     * @param rConnection
     * @return
     */
    def String createTempDir(def rConnection) {
        def timestamp = new Date().getTime().toString()
        rConnection.voidEval("setwd(tempdir())")
        rConnection.voidEval("dir.create(\"${timestamp}\")")
        rConnection.voidEval("setwd(\"${timestamp}\")")
        return rConnection.eval("getwd()").asString()
    }

    Rfile createRfile(RConnection r, File file){

        RconnectService.transferToServer(r, file, file.getName())

        new Rfile(fileName: file.getName(), filePath: fileDir+"file.getName()").save(flush: true)
    }

}
