package aico.backend.global.exception.user;

public class DuplicatedUserException extends RuntimeException {
    public DuplicatedUserException(String message) {
        super(message);
    }
}
