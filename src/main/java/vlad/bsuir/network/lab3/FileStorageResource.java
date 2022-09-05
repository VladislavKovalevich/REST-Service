package vlad.bsuir.network.lab3;

import org.glassfish.jersey.media.multipart.FormDataParam;

import vlad.bsuir.network.lab3.http.COPY;
import vlad.bsuir.network.lab3.http.MOVE;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import java.io.*;

import java.nio.file.Files;
import java.nio.file.Paths;

@Path("file")
public class FileStorageResource {

    @GET
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("list")
    public String list(@QueryParam("subdir") String subdir) {
        //System.out.println("list subdir: " + subdir);
        try {
            File dir = new File(Main.BASE_DIR);

            String[] files = dir.list();

            String ret = "";
            for (String str : files) {
                ret += (ret.length() == 0 ? "" : "|") + str;
            }

            return ret;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return "";
    }

    @GET
    @Path("read")
    public Response read(@QueryParam("name") String fileName) {
        final String file = fileName;
        //System.out.println("read fileName: " + file);
        StreamingOutput fileStream =  new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws WebApplicationException {
                try {
                    java.nio.file.Path path = Paths.get(Main.BASE_DIR + File.separatorChar + file);
                    byte[] data = Files.readAllBytes(path);
                    output.write(data);
                    output.flush();
                } catch (Exception e) {
                    throw new WebApplicationException("File Not Found !!");
                }
            }
        };
        return Response.ok(fileStream, MediaType.APPLICATION_OCTET_STREAM)
                .header("content-disposition","attachment; filename = " + file)
                .build();
    }

    @PUT
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("write")
    public Response write(
            @FormDataParam("part") InputStream inputStream,
            @FormDataParam("name") String fileName) {

        try {
            BufferedOutputStream bos = new BufferedOutputStream(
                    new FileOutputStream(Main.BASE_DIR + File.separatorChar + fileName));

            int bytesRead = 0;
            byte[] buffer = new byte[10240];

            while((bytesRead = inputStream.read(buffer, 0, 10240)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }

            bos.flush();
            bos.close();

            inputStream.close();

            return Response.ok("ok").build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.ok("error: " + ex.getMessage()).build();
        }

    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("append")
    public Response append(@FormDataParam("part") InputStream inputStream,
                           @FormDataParam("name") String fileName) {
        try {
            BufferedOutputStream bos = new BufferedOutputStream(
                    new FileOutputStream(Main.BASE_DIR + File.separatorChar + fileName, true));

            int bytesRead = 0;
            byte[] buffer = new byte[10240];

            while((bytesRead = inputStream.read(buffer, 0, 10240)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }

            bos.flush();
            bos.close();

            inputStream.close();

            return Response.ok("ok").build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.ok("error: " + ex.getMessage()).build();
        }
    }

    @DELETE
    @Path("delete")
    public Response delete(@QueryParam("name") String fileName) {
        try {
            File file = new File(Main.BASE_DIR + File.separatorChar + fileName);

            if (file.delete()) {
                return Response.ok("ok").build();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.ok("error: " + ex.getMessage()).build();
        }
        return Response.ok("file not found").build();
    }

    @COPY
    @Path("copy")
    public Response copy(@QueryParam("from") String fileNameFrom,
                         @QueryParam("to")   String fileNameTo) {
        try {
            java.nio.file.Path pathFrom = Paths.get(Main.BASE_DIR + File.separatorChar + fileNameFrom);
            java.nio.file.Path pathTo   = Paths.get(Main.BASE_DIR + File.separatorChar + fileNameTo);

            Files.copy(pathFrom, pathTo);

            return Response.ok("ok").build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.ok("error: " + ex.getMessage()).build();
        }
    }

    @MOVE
    @Path("move")
    public Response move(@QueryParam("from") String fileNameFrom,
                         @QueryParam("to")   String fileNameTo) {
        try {
            java.nio.file.Path pathFrom = Paths.get(Main.BASE_DIR + File.separatorChar + fileNameFrom);
            java.nio.file.Path pathTo   = Paths.get(Main.BASE_DIR + File.separatorChar + fileNameTo);

            Files.move(pathFrom, pathTo);

            return Response.ok("ok").build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.ok("error: " + ex.getMessage()).build();
        }
    }

}
