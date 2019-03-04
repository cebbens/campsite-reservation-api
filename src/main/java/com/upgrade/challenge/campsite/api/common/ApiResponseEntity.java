package com.upgrade.challenge.campsite.api.common;

import lombok.Builder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Builder
public class ApiResponseEntity<T> {

    private HttpStatus status;
    private String message;
    private T data;

    public static class ApiResponseEntityBuilder<T> {
        private HttpStatus status;
        private String message;
        private T data;

        public ApiResponseEntityBuilder<T> ok(ApiResponse<T> apiResponse) {
            return status(HttpStatus.OK).body(apiResponse);
        }

        public ApiResponseEntityBuilder<T> created(ApiResponse<T> apiResponse) {
            return status(HttpStatus.CREATED).body(apiResponse);
        }

        public ApiResponseEntityBuilder<T> status(HttpStatus status) {
            this.status = status;
            this.message = this.status.getReasonPhrase();
            return this;
        }

        public ApiResponseEntityBuilder<T> body(ApiResponse<T> apiResponse) {
            this.data = apiResponse.getData();
            return this;
        }

        public ResponseEntity<ApiResponse<T>> build() {
            return ResponseEntity.status(status).body(ApiResponse.<T>builder().message(message).data(data).build());
        }
    }
}
