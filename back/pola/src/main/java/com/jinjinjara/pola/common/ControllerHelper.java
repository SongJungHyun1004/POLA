package com.jinjinjara.pola.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface ControllerHelper {
    default ResponseEntity<?> handleSuccess(Object data){
        return handleSuccess(data, HttpStatus.OK);
    }

    default ResponseEntity<?> handleFail(Exception e){
        return handleFail(e, HttpStatus.FORBIDDEN);
    }

    default ResponseEntity<?> handleFail(CustomException e){
        Map<String, Object> map = Map.of("status","FAIL","code", e.errorCode.getCode(),"error", e.errorCode.getMessage());
        return ResponseEntity.status(e.errorCode.getStatus()).body(map);
    }

    default ResponseEntity<?> handleSuccess(Object data, HttpStatus status){
        Map<String, Object> map = Map.of("status","SUCCESS","data", data);
        return ResponseEntity.status(status).body(map);
    }

    default ResponseEntity<?> handleFail(Exception e, HttpStatus status){
        Map<String, Object> map = Map.of("status","FAIL","error", e.getMessage());
        return ResponseEntity.status(status).body(map);
    }

    default ResponseEntity<?> handleSuccess(Object data, HttpStatus status, String message){
        Map<String, Object> map = Map.of("status","SUCCESS","data", data, "message", message);
        return ResponseEntity.status(status).body(map);
    }
}
