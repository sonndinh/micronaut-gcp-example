package com.example;

import com.google.cloud.storage.Blob;
import io.micronaut.http.*;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.http.server.types.files.StreamedFile;
import io.micronaut.http.server.util.HttpHostResolver;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.objectstorage.googlecloud.GoogleCloudStorageEntry;
import io.micronaut.objectstorage.googlecloud.GoogleCloudStorageOperations;
import io.micronaut.objectstorage.request.UploadRequest;
import io.micronaut.objectstorage.response.UploadResponse;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

@Controller(UploadController.PREFIX)
@ExecuteOn(TaskExecutors.BLOCKING)
public class UploadController {

    static final String PREFIX = "/documents";

    private final GoogleCloudStorageOperations objectStorage;
    private final HttpHostResolver httpHostResolver;

    public UploadController(GoogleCloudStorageOperations objectStorage, HttpHostResolver httpHostResolver) {
        this.objectStorage = objectStorage;
        this.httpHostResolver = httpHostResolver;
    }

    @Post(uri = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA)
    public HttpResponse<?> upload(CompletedFileUpload file, String userId, HttpRequest<?> request) {
        String key = buildKey(userId);
        UploadRequest objectStorageUpload = UploadRequest.fromCompletedFileUpload(file, key);
        UploadResponse<Blob> response = objectStorage.upload(objectStorageUpload);

        return HttpResponse.created(location(request, userId))
                .header(HttpHeaders.ETAG, response.getETag());
    }

    @Get("/{userId}")
    Optional<HttpResponse<StreamedFile>> download(String userId) {
        String key = buildKey(userId);
        return objectStorage.retrieve(key)
                .map(UploadController::buildStreamedFile);
    }

    private static HttpResponse<StreamedFile> buildStreamedFile(GoogleCloudStorageEntry entry) {
        Blob nativeEntry = entry.getNativeEntry();
        MediaType mediaType = MediaType.of(nativeEntry.getContentType());
        StreamedFile file = new StreamedFile(entry.getInputStream(), mediaType).attach(entry.getKey());
        MutableHttpResponse<Object> httpResponse = HttpResponse.ok()
                .header(HttpHeaders.ETAG, nativeEntry.getEtag());
        file.process(httpResponse);
        return httpResponse.body(file);
    }

    private static String buildKey(String userId) {
        return userId + ".jpg";
    }

    private URI location(HttpRequest<?> request, String userId) {
        return UriBuilder.of(httpHostResolver.resolve(request))
                .path(PREFIX)
                .path(userId)
                .build();
    }
}
