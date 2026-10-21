package pe.cibertec.gym.controller;

import java.util.NoSuchElementException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class FormErrorAdvice {
    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String invalid(Exception exception, Model model) {
        model.addAttribute("message", exception instanceof MethodArgumentTypeMismatchException
                ? "Revise las fechas y los datos enviados." : exception.getMessage());
        model.addAttribute("status", 400); return "error";
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String conflict(Model model) {
        model.addAttribute("message", "Los datos están duplicados o relacionados con otro registro. Revise el formulario.");
        model.addAttribute("status", 409); return "error";
    }
    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String missing(Model model) {
        model.addAttribute("message", "El registro solicitado no existe.");
        model.addAttribute("status", 404); return "error";
    }
}
