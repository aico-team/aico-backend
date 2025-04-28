package aico.backend.global.exception.curriculum;

public class CurriNotFoundException extends RuntimeException {
    public CurriNotFoundException(String message) {
        super(message);
    }
}
