package rconnect

import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.rosuda.REngine.Rserve.RConnection
import org.rosuda.REngine.Rserve.RFileOutputStream
import org.rosuda.REngine.Rserve.RFileInputStream
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

class RconnectService {

    final static String host = ConfigurationHolder?.config?.rconnect?.host?:"127.0.0.1"
    final static int port = Integer.valueOf(ConfigurationHolder?.config?.rconnect?.port?:"6311")

    /**
     * Get a connection object with default connection params, which can be used to send commands
     */
    def getConnection = {
        log.info "trying to connect to Rserve on ${host}:${port}"
        return new RConnection(host, port)
    }

    /**
     * Open a specific connection
     */
    def getConnectionWithParams = { host, port ->
        return new RConnection(host, port)
    }

    /**
     *  Test if the connection is valid
     */
    def testConnection = { connection ->
        connection.eval("R.version.string").asString();
    }

    /**
     * Transfer a file to the server, needs a client file (webserver) and a remote (server) file (R)
     * @param r
     * @param client_file
     * @param server_file
     * @return
     */
    def transferToServer(RConnection r, String client_file, String server_file ){

        byte [] b = new byte[8192];
        try{
            /* the file on the client machine we read from */
            BufferedInputStream client_stream = new BufferedInputStream(
                    new FileInputStream( new File( client_file ) ) );

            /* the file on the server we write to */
            RFileOutputStream server_stream = r.createFile( server_file );

            /* typical java IO stuff */
            int c = client_stream.read(b) ;
            while( c >= 0 ){
                server_stream.write( b, 0, c ) ;
                c = client_stream.read(b) ;
            }
            server_stream.close();
            client_stream.close();

        } catch( IOException e){
            log.error e.getMessage()
        }

    }

    /**
     * Transfer a file server_file from R to the webserver and save as client_file
     * @param r
     * @param client_file
     * @param server_file
     */
    public void transferToClient(RConnection r,  String client_file, String server_file ){

        byte [] b = new byte[8192];
        try{

            /* the file on the client machine we write to */
            BufferedOutputStream client_stream = new BufferedOutputStream(
                    new FileOutputStream( new File( client_file ) ) );

            /* the file on the server machine we read from */
            RFileInputStream server_stream = r.openFile( server_file );

            /* typical java io stuff */
            int c = server_stream.read(b) ;
            while( c >= 0 ){
                client_stream.write( b, 0, c ) ;
                c = server_stream.read(b) ;
            }
            client_stream.close();
            server_stream.close();

        } catch( IOException e){
            log.error e.getMessage()
        }

    }

    def transferByteStream(RConnection r, String server_file, ByteArrayOutputStream baos){
        RFileInputStream server_stream = r.openFile(server_file)

        baos << server_stream
        server_stream.close()

        return (baos)
    }

    def transferPlot(RConnection r, String server_file, BufferedImage bufferedImage){
        RFileInputStream server_stream = r.openFile(server_file)

        bufferedImage = ImageIO.read(server_stream)

        return(bufferedImage)
    }
}
