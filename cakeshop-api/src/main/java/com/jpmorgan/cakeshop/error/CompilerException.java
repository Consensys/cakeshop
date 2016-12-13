package com.jpmorgan.cakeshop.error;

import java.util.List;

public class CompilerException extends APIException {

    private static final long serialVersionUID = -7089507784862161620L;

    private final List<String> errors;

    public CompilerException(List<String> errors) {
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }

}
