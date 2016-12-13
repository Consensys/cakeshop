package com.jpmorgan.cakeshop.client;

import com.jpmorgan.cakeshop.model.ContractABI.Entry.Param;
import com.jpmorgan.cakeshop.model.SolidityType;
import com.jpmorgan.cakeshop.model.SolidityType.ArrayType;
import com.jpmorgan.cakeshop.model.SolidityType.Bytes32Type;
import com.jpmorgan.cakeshop.model.SolidityType.BytesType;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;

public class ContractUtil {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory
            .getLogger(ContractUtil.class);

    public static void processInputArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return;
        }
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];

            // base64 encode byte types
            if (arg instanceof Byte) {
                if (arg.getClass().isArray()) {
                    args[i] = Base64.encodeBase64String((byte[]) arg);
                } else {
                    args[i] = Base64.encodeBase64String(new byte[]{ (byte) arg });
                }
            }
        }
    }
        public static List<Object> processOutputArgs(List<Object> returnVals, List<Param> outputs) {
        return processOutputArgs(returnVals, outputs, false);
    }

    public static List<Object> processOutputArgs(List<Object> returnVals, List<Param> outputs, boolean treatBytesAsStrings) {

        if (outputs == null || outputs.isEmpty()) {
            return returnVals;
        }

        List<Object> results = new ArrayList<>();

        for (int i = 0; i < returnVals.size(); i++) {
            Object ret = returnVals.get(i);
            Param out = outputs.get(i);

            SolidityType type = out.type;
            if (type instanceof ArrayType) {
                type = ((ArrayType) out.type).elementType; // use inner type
            }

            if (type instanceof BytesType || type instanceof Bytes32Type) {
                if (ret instanceof Byte) {
                    // single value
                    results.add(decodeBytes((String) ret, treatBytesAsStrings));

                } else if (ret instanceof List) {
                    // convert array of bytes
                    List<Object> argList = new ArrayList<>();
                    for (Object arg : (List) ret) {
                        argList.add(decodeBytes((String) arg, treatBytesAsStrings));
                    }
                    results.add(argList);

                } else {
                    LOG.warn("Not sure how to handle " + ret.getClass());
                    results.add(ret);
                }

            } else {
                results.add(ret);
            }
        }

        return results;
    }

    private static Object decodeBytes(String str, boolean treatBytesAsStrings) {
        byte[] decoded = Base64.decodeBase64(str);
        if (treatBytesAsStrings) {
            return new String(decoded);
        } else {
            return decoded;
        }
    }




}
