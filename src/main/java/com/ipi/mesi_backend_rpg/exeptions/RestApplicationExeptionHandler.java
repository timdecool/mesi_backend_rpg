package com.ipi.mesi_backend_rpg.exeptions;

import com.google.firebase.auth.FirebaseAuthException;
import com.ipi.mesi_backend_rpg.model.ErrorValidation;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ControllerAdvice
@ResponseBody
public class RestApplicationExeptionHandler extends ResponseEntityExceptionHandler {
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request
    ) {
        List<ErrorValidation> errors = new ArrayList<>();
        BindingResult bindingResult = ex.getBindingResult();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();

        for (FieldError fieldError : fieldErrors) {
            errors.add(new ErrorValidation(fieldError.getField(), fieldError.getDefaultMessage()));
        }

        return this.handleExceptionInternal(ex, errors, headers, status, request);

    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Object> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex, WebRequest request) {
        ErrorValidation error = new ErrorValidation("email", ex.getMessage());
        return handleExceptionInternal(ex, Collections.singletonList(error), new HttpHeaders(), HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(FirebaseAuthException.class)
    public ResponseEntity<Object> handleFirebaseAuthException(FirebaseAuthException ex, WebRequest request) {
        String errorMessage = "Erreur d'authentification Firebase.";
        if (ex.getErrorCode() != null) {
            errorMessage = "Erreur Firebase: " + ex.getErrorCode().name() + " - " + ex.getMessage();
            // Vous pouvez affiner les messages ici en fonction des codes d'erreur Firebase
            if (ex.getErrorCode().equals(com.google.firebase.ErrorCode.UNAUTHENTICATED)) {
                errorMessage = "Token d'authentification invalide ou expiré.";
            } else if (ex.getErrorCode().equals(com.google.firebase.ErrorCode.NOT_FOUND)) {
                errorMessage = "Utilisateur non trouvé.";
            } else if (ex.getErrorCode().equals(com.google.firebase.ErrorCode.INVALID_ARGUMENT)) {
                errorMessage = "Mot de passe incorrect.";
            }
        }
        ErrorValidation error = new ErrorValidation("firebase_auth", errorMessage);
        return handleExceptionInternal(ex, Collections.singletonList(error), new HttpHeaders(), HttpStatus.UNAUTHORIZED, request);
    }

}
