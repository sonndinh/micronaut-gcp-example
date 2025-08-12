package com.example;

import com.google.cloud.storage.Blob;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.http.server.util.HttpHostResolver;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.objectstorage.googlecloud.GoogleCloudStorageOperations;
import io.micronaut.objectstorage.request.UploadRequest;
import io.micronaut.objectstorage.response.UploadResponse;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;

import java.net.URI;

@Controller(UploadController.PREFIX)
@ExecuteOn(TaskExecutors.BLOCKING)
public class UploadController implements UploadInterface {

    static final String PREFIX = "/documents";

    private final GoogleCloudStorageOperations objectStorage;
    private final HttpHostResolver httpHostResolver;

    public UploadController(GoogleCloudStorageOperations objectStorage, HttpHostResolver httpHostResolver) {
        this.objectStorage = objectStorage;
        this.httpHostResolver = httpHostResolver;
    }

    @Override
    public HttpResponse<?> upload(CompletedFileUpload file, String userId, HttpRequest<?> request) {
        String key = buildKey(userId);
        UploadRequest objectStorageUpload = UploadRequest.fromCompletedFileUpload(file, key);
        UploadResponse<Blob> response = objectStorage.upload(objectStorageUpload);

        return HttpResponse.created(location(request, userId))
                .header(HttpHeaders.ETAG, response.getETag());
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
