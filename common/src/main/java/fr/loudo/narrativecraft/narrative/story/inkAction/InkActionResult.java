package fr.loudo.narrativecraft.narrative.story.inkAction;

public class InkActionResult {
    public enum Status {
        PASS, BLOCK, ERROR
    }
    
    private final Status status;
    private final String errorMessage;
    
    private InkActionResult(Status status, String errorMessage) {
        this.status = status;
        this.errorMessage = errorMessage;
    }
    
    public static InkActionResult pass() {
        return new InkActionResult(Status.PASS, null);
    }
    
    public static InkActionResult block() {
        return new InkActionResult(Status.BLOCK, null);
    }
    
    public static InkActionResult error(Class<?> clazz, String message) {
        return new InkActionResult(Status.ERROR, clazz.getSimpleName() + "\n" + message);
    }
    
    public Status getStatus() { return status; }
    public String getErrorMessage() { return errorMessage; }
    public boolean isError() { return status == Status.ERROR; }
}