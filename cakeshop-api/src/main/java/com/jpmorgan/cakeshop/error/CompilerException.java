package com.jpmorgan.cakeshop.error;

import java.util.List;
import java.util.Map;

public class CompilerException extends APIException {

    private static final long serialVersionUID = -7089507784862161620L;

    private final List<Map<String, Object>> errors;

    public CompilerException(List<Map<String, Object>> errors) {
        this.errors = errors;
    }

    public List<Map<String, Object>> getErrors() {
        return errors;
    }

}
