package aico.backend.global.exception;

import aico.backend.global.exception.curriculum.CurriNotFoundException;
import aico.backend.global.exception.user.ConfirmPasswordMisException;
import aico.backend.global.exception.user.DuplicatedUserException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(DuplicatedUserException.class)
    public ResponseEntity<?> handleDuplicatedUserException(DuplicatedUserException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }

    @ExceptionHandler(ConfirmPasswordMisException.class)
    public ResponseEntity<?> handleConfirmPasswordMisException(ConfirmPasswordMisException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    @ExceptionHandler(CurriNotFoundException.class)
    public ResponseEntity<?> handleCurriNotFoundException(CurriNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
}
