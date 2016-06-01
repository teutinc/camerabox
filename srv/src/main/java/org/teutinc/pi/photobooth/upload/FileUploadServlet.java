package org.teutinc.pi.photobooth.upload;

import com.google.common.io.ByteStreams;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Servlet to upload contents from administration to a temporary folder
 *
 * @author apeyrard
 */
public class FileUploadServlet extends HttpServlet {
    private static final Path tempBasePath = Paths.get("/tmp/camerabox/upload");

    @Override
    public void init() throws ServletException {
        try {
            Files.createDirectories(tempBasePath);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Part filePart = request.getPart("file");
        String fileName = filePart.getName();

        try (InputStream fileContent = filePart.getInputStream();
             FileOutputStream outputStream = new FileOutputStream(tempBasePath.resolve(fileName).toFile())) {
            ByteStreams.copy(fileContent, outputStream);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        throw new IOException("not authorized!");
    }
}
