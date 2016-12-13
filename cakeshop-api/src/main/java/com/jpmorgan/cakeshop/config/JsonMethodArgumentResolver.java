package com.jpmorgan.cakeshop.config;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.ValueConstants;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class JsonMethodArgumentResolver implements HandlerMethodArgumentResolver {

    private static final Logger LOG = LoggerFactory.getLogger(JsonMethodArgumentResolver.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface JsonBodyParam	{

        /**
         * The name of the request parameter to bind to.
         */
        String value() default "";

        /**
         * Whether the parameter is required.
         * <p>Default is {@code true}, leading to an exception thrown in case
         * of the parameter missing in the request. Switch this to {@code false}
         * if you prefer a {@code null} in case of the parameter missing.
         * <p>Alternatively, provide a {@link #defaultValue() defaultValue},
         * which implicitly sets this flag to {@code false}.
         */
        boolean required() default true;

        /**
         * The default value to use as a fallback when the request parameter value
         * is not provided or empty. Supplying a default value implicitly sets
         * {@link #required()} to false.
         */
        String defaultValue() default ValueConstants.DEFAULT_NONE;

    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return (parameter.getParameterAnnotation(JsonBodyParam.class) != null);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

        if (!mavContainer.getModel().containsAttribute("_json_data")) {
            BufferedReader postReader = ((HttpServletRequest)webRequest.getNativeRequest()).getReader();
            Map<String, Object> data = null;
            try {
                data = objectMapper.readValue(postReader, Map.class);
            } catch (JsonMappingException ex) {
            }
            mavContainer.addAttribute("_json_data", data);
        }


        Map<String, Object> data = (Map<String, Object>) mavContainer.getModel().get("_json_data");

        JsonBodyParam jsonParam = parameter.getParameterAnnotation(JsonBodyParam.class);
        String param = jsonParam.value();
        if (param == null || param.isEmpty() || param.equals(jsonParam.defaultValue())) {
            param = parameter.getParameterName(); // fallback to name of param itself
        }

        Class paramType = parameter.getParameterType();
        Object val = data == null ? null : data.get(param);
        if (val == null) {
            // handle null val
            if (!jsonParam.defaultValue().contentEquals(ValueConstants.DEFAULT_NONE)) {
                return jsonParam.defaultValue();
            }
            return val;
        }
        if (paramType == val.getClass() || paramType == Object.class) {
            return val; // val types match exactly or Object type was requested
        }

        if ((paramType == Long.class || paramType == Integer.class)
                && (val instanceof Long || val instanceof Integer)) {

            // flip type of val
            if (paramType == Long.class) {
                return new Integer((int) val).longValue();
            } else {
                return new Long((long) val).intValue();
            }
        }

        if (paramType.isArray()) {
            if (val.getClass().isArray()) {
                return val;
            }

            if (val instanceof List) {
                return ((List) val).toArray();
            }
        }

        if (paramType == List.class && val instanceof List) {
            return val;
        }

        LOG.warn("Param type mismatch for '" + parameter.getParameterName() + "'; got " + val.getClass().toString());
        return null;
    }

}
