package br.com.chronac.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice 
public class ApplicationExceptionHandler {
    



    @ExceptionHandler(NotImplementedException.class)
    public ResponseEntity<String> handleNotImplemented(NotImplementedException e){
        return ResponseEntity
        .status(HttpStatus.NOT_IMPLEMENTED)
        .body(e.getMessage())
        ;

    }

    @ExceptionHandler(DuplicateGenerationException.class)
    public ResponseEntity<String> handleDuplicateGeneration(DuplicateGenerationException e){
        return ResponseEntity
        .status(HttpStatus.CONFLICT)
        .body(e.getMessage())
        ;
    }

    @ExceptionHandler(UnknownDemoException.class)
    public ResponseEntity<String> handleUnknownDemo(UnknownDemoException e){
        return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(e.getMessage())
        ;
    }

    @ExceptionHandler(UnknownJobException.class)
    public ResponseEntity<String> handleUnknownJob(UnknownJobException e){
        return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(e.getMessage())
        ;
    }
}
