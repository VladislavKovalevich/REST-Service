package vlad.bsuir.network.lab3;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.grizzly.http.server.HttpServer;

import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.assertEquals;

public class FileStorageResourceTest {

    private static final String CLIENT_DIR = "d:\\Учёба\\Лабораторные\\Семестр 4\\КСиС\\RestHttp\\client";

    private HttpServer server;
    private WebTarget target;

    @Before
    public void setUp() throws Exception {
        // start the server
        server = Main.startServer();

        // create the client
        Client client = ClientBuilder.newClient().
                property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true);

        target = client.target(Main.BASE_URI);
    }

    @After
    public void tearDown() throws Exception {
        server.shutdownNow();
    }

    private String getList() {
        WebTarget tr = target.path("file").path("list");
        return tr.request().get(String.class);
    }

    @Test
    public void testGetFilesList() {
        String listStr = getList();
        System.out.println("response: " + listStr);

        String[] list = listStr.split("\\|");
        assertEquals(list.length > 0, true);
    }


    @Test
    public void testFileRead() {
        String listStr = getList();
        String[] list = (listStr != null) ? listStr.split("\\|") : null;

        String fileName = "";
        if (list != null) {
            fileName = list[0];
        } else {
            fileName = "test";
        }

        WebTarget tr = target.path("file").path("read").queryParam("name", fileName);
        Response response = tr.request().get();

        //String contentDesc = (String)response.getHeaders().getFirst("content-disposition");
        //String fileName = (contentDesc.indexOf("filename = ") > 0) ?
        //        contentDesc.substring(contentDesc.indexOf("filename = ") + 11).trim() : null;

        //System.out.println(fileName);

        InputStream is = response.readEntity(InputStream.class);

        try {
            BufferedOutputStream bos = new BufferedOutputStream(
                    new FileOutputStream(CLIENT_DIR + File.separatorChar +
                            ((fileName != null) ? fileName : "test")));

            int bytesRead = 0;
            byte[] buffer = new byte[10240];

            while((bytesRead = is.read(buffer, 0, 10240)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }

            bos.flush();
            bos.close();
            is.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        File file = new File(CLIENT_DIR + File.separatorChar +
                ((fileName != null) ? fileName : "test"));

        assertEquals(file.exists(), true);
        assertEquals(file.length() > 0, true);
    }


    @Test
    public void testFileWrite() {
        try {
            File file = new File(CLIENT_DIR);
            String fileName = file.list()[0];

            final Client client = ClientBuilder.newBuilder().register(MultiPartFeature.class).
                    register(FileStorageResource.class).build();

            //System.out.println(CLIENT_DIR + File.separatorChar + fileName);

            final StreamDataBodyPart filePart = new StreamDataBodyPart(
                    "part",
                    new BufferedInputStream(new FileInputStream(CLIENT_DIR + File.separatorChar + fileName)));

            FormDataMultiPart multiPart = new FormDataMultiPart();
            multiPart.field("name", fileName + "-copy");
            multiPart.bodyPart(filePart);

            final WebTarget target = client.target(Main.BASE_URI + "file/write");
            final Response response = target.request().put(Entity.entity(multiPart, MediaType.MULTIPART_FORM_DATA_TYPE));

            System.out.println(response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    @Test
    public void testFileAppend() {
        try {
            String listStr = getList();
            String[] list = (listStr != null) ? listStr.split("\\|") : null;

            String fileName = "";
            if (list != null) {
                fileName = list[0];
            } else {
                fileName = "test";
            }

            File file = new File(CLIENT_DIR);
            String fileNameFrom = file.list()[0];

            final Client client = ClientBuilder.newBuilder().register(MultiPartFeature.class).
                    register(FileStorageResource.class).build();

            //System.out.println(CLIENT_DIR + File.separatorChar + fileName);

            final StreamDataBodyPart filePart = new StreamDataBodyPart(
                    "part",
                    new BufferedInputStream(new FileInputStream(CLIENT_DIR + File.separatorChar + fileNameFrom)));

            FormDataMultiPart multiPart = new FormDataMultiPart();
            multiPart.field("name", fileName);
            multiPart.bodyPart(filePart);

            final WebTarget target = client.target(Main.BASE_URI + "file/append");
            final Response response = target.request().post(Entity.entity(multiPart, MediaType.MULTIPART_FORM_DATA_TYPE));

            System.out.println(response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void testFileDelete() {
        String listStr = getList();
        String[] list = (listStr != null) ? listStr.split("\\|") : null;

        String fileName = "";
        if (list != null) {
            fileName = list[0];
        } else {
            fileName = "test";
        }

        WebTarget tr = target.path("file").path("delete").queryParam("name", fileName);
        Response response = tr.request().delete();

        System.out.println(response);
    }

    @Test
    public void testFileCopy() {
        String listStr = getList();
        String[] list = (listStr != null) ? listStr.split("\\|") : null;

        String fileName = "";
        if (list != null) {
            fileName = list[0];
        } else {
            fileName = "test";
        }

        WebTarget tr = target.path("file").path("copy").
                queryParam("from", fileName).
                queryParam("to", fileName + "-copy");
        Response response = tr.request().method("COPY");

        System.out.println(response);
    }

    @Test
    public void testFileMove() {
        String listStr = getList();
        String[] list = (listStr != null) ? listStr.split("\\|") : null;

        String fileName = "";
        if (list != null) {
            fileName = list[0];
        } else {
            fileName = "test";
        }

        WebTarget tr = target.path("file").path("move").
                queryParam("from", fileName).
                queryParam("to", fileName + "-move");
        Response response = tr.request().method("MOVE");

        System.out.println(response);
    }
}
