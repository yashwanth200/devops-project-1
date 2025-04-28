package com.ecommerce.project.exceptions;

public class ResourceNotFoundException extends RuntimeException {
    String resorceName;
    String field;
    String fieldName;
    Long fieldId;

    public ResourceNotFoundException() {
    }

    public ResourceNotFoundException(String resorceName, String field, String fieldName) {
        super(String.format("%s not found with %s : %s",resorceName,field,fieldName));
        this.resorceName = resorceName;
        this.field = field;
        this.fieldName = fieldName;
    }

    public ResourceNotFoundException( String resorceName, String field,Long fieldId) {
        super(String.format("%s not found with %s : %d",resorceName,field,fieldId));
        this.fieldId = fieldId;
        this.field = field;
        this.resorceName = resorceName;
    }
}
