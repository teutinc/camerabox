package org.teutinc.pi.camerabox.upload;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.teutinc.pi.camerabox.AppSettings;
import restx.*;
import restx.factory.Component;
import restx.http.HttpStatus;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * from https://groups.google.com/forum/#!topic/restx/VzrX1-ka1wU adapted for current restx version
 */
@Component
public class FileUploadRoute extends StdRoute {
    private static final Logger logger = getLogger(FileUploadRoute.class);


    private final Path tempBasePath;
    private final ImmutableSet<String> allowedContentType;

    public FileUploadRoute(AppSettings settings) {
        super("upload", new StdRestxRequestMatcher("POST", "/upload"));
        try {
            tempBasePath = Files.createDirectories(Paths.get(settings.uploadTempPath()));
        } catch (IOException e) {
            throw new UncheckedIOException("unable to create a temporary directory for uploads", e);
        }
        allowedContentType = ImmutableSet.copyOf(
                Splitter.on(",")
                        .trimResults()
                        .splitToList(settings.uploadAllowedTypes())
                        .stream()
                        .map(String::toLowerCase)
                        .collect(Collectors.toList())
        );
    }

    @Override
    public void handle(RestxRequestMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
        new PartsReader(req).readParts(part -> {
            if (part instanceof PartsReader.FilePart) {
                PartsReader.FilePart filePart = (PartsReader.FilePart) part;
                if (allowedContentType.contains(filePart.getContentType().toLowerCase())) {
                    try (FileOutputStream outputStream = new FileOutputStream(tempBasePath.resolve(filePart.getFilename()).toFile())) {
                        logger.debug("upload file: {}", filePart.getFilename());
                        filePart.readStreamTo(outputStream);
                    }
                } else {
                    throw new IllegalStateException(format("file %s will be skipped, as content type %s is not allowed",
                            filePart.getFilename(), filePart.getContentType()));
                }
            } else if (part instanceof PartsReader.TextPart) {
                throw new IllegalStateException(format("skip upload of TextPart: %s", part));
            }
        });

        resp.setStatus(HttpStatus.OK);
        resp.setContentType("plain/text");
        resp.getWriter().write("ok");
    }
}