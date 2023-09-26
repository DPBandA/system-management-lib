package jm.com.dpbennett.sm.exception;

import java.io.IOException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.ViewExpiredException;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;
import javax.servlet.http.HttpServletRequest;

public class CustomExceptionHandler extends ExceptionHandlerWrapper {

    private final ExceptionHandler wrapped;

    public CustomExceptionHandler(ExceptionHandler wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public ExceptionHandler getWrapped() {
        return wrapped;
    }

    @Override
    public void handle() {
        Iterator<ExceptionQueuedEvent> iterator = getUnhandledExceptionQueuedEvents().iterator();

        while (iterator.hasNext()) {
            ExceptionQueuedEvent event = iterator.next();
            ExceptionQueuedEventContext context = (ExceptionQueuedEventContext) event.getSource();
            Throwable throwable = context.getException();

            if (throwable instanceof ViewExpiredException) {
                try {
                    
                    System.out.println("View expired...redirecting...");
                    ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
                    HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
                    String currentURL = request.getRequestURI();                   
                    externalContext.redirect(currentURL);

                } catch (IOException ex) {
                    Logger.getLogger(CustomExceptionHandler.class.getName()).log(Level.SEVERE, null, ex);
                }

                iterator.remove(); 
            }
        }

        // Let the parent ExceptionHandler handle other exceptions.
        getWrapped().handle();
    }
}
