package com.example;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.multipart.CompletedFileUpload;

public interface UploadInterface {

    @Post(uri = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA)
    HttpResponse upload(CompletedFileUpload file, String userId, HttpRequest<?> request);
}
