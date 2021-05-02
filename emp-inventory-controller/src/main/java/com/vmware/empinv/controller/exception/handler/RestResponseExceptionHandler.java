package com.vmware.empinv.controller.exception.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.vmware.empinv.exceptions.DataNotFound;
import com.vmware.empinv.exceptions.FileUploadException;
import com.vmware.empinv.exceptions.InventoryException;
import com.vmware.empinv.exceptions.ValidationException;
import com.vmware.empinv.models.ErrorResponse;

@ControllerAdvice
public class RestResponseExceptionHandler 
  extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = DataNotFound.class)
    protected ResponseEntity<ErrorResponse> handleDataNotFound(
    		DataNotFound ex, WebRequest request) {
        ErrorResponse response = ErrorResponse.builder().errorCode(HttpStatus.NOT_FOUND.name())
        		.errorMessage(ex.getMessage()).exceptionType(ex.getClass().getName()).build();
        return new ResponseEntity<ErrorResponse>(response, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(value = FileUploadException.class)
    protected ResponseEntity<ErrorResponse> handleFileUploadException(
    		FileUploadException ex, WebRequest request) {
        ErrorResponse response = ErrorResponse.builder().errorCode(HttpStatus.SERVICE_UNAVAILABLE.name())
        		.errorMessage(ex.getMessage()).exceptionType(ex.getClass().getName()).build();
        return new ResponseEntity<ErrorResponse>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }
    
    @ExceptionHandler(value = InventoryException.class)
    protected ResponseEntity<ErrorResponse> handleInventoryException(
    		InventoryException ex, WebRequest request) {
        ErrorResponse response = ErrorResponse.builder().errorCode(HttpStatus.SERVICE_UNAVAILABLE.name())
        		.errorMessage(ex.getMessage()).exceptionType(ex.getClass().getName()).build();
        return new ResponseEntity<ErrorResponse>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }
    
    @ExceptionHandler(value = ValidationException.class)
    protected ResponseEntity<ErrorResponse> handleValidationException(
    		ValidationException ex, WebRequest request) {
        ErrorResponse response = ErrorResponse.builder().errorCode(HttpStatus.BAD_REQUEST.name())
        		.errorMessage(ex.getMessage()).exceptionType(ex.getClass().getName()).build();
        return new ResponseEntity<ErrorResponse>(response, HttpStatus.BAD_REQUEST);
    }
}