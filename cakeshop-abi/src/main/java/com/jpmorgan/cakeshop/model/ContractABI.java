package com.jpmorgan.cakeshop.model;

import static com.jpmorgan.cakeshop.model.SolidityType.IntType.*;
import static java.lang.String.*;
import static org.apache.commons.collections4.ListUtils.*;
import static org.apache.commons.lang3.ArrayUtils.*;
import static org.apache.commons.lang3.StringUtils.*;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpmorgan.cakeshop.error.ABIException;
import com.jpmorgan.cakeshop.model.ContractABI.Entry.Type;
import com.jpmorgan.cakeshop.util.AbiUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.bouncycastle.util.encoders.Hex;

public class ContractABI extends ArrayList<ContractABI.Entry> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static ContractABI fromJson(String json) {
        try {
            return objectMapper.readValue(json, ContractABI.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String toJson() {
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends ContractABI.Entry> T find(Class<T> resultClass, final ContractABI.Entry.Type type, final Predicate<T> searchPredicate) {
        return (T) CollectionUtils.find(this, new Predicate<ContractABI.Entry>() {
            @Override
            public boolean evaluate(ContractABI.Entry entry) {
                return entry.type == type && searchPredicate.evaluate((T) entry);
            }
        });
    }

    public Function getFunction(final String name) {
        return findFunction(new Predicate<ContractABI.Function>() {
            @Override
            public boolean evaluate(Function f) {
                return f.name.equalsIgnoreCase(name);
            }
        });
    }

    public Event findEventBySignature(final String sig) {
        return findEvent(new Predicate<ContractABI.Event>() {
            @Override
            public boolean evaluate(Event event) {
                String fSig = "0x" + Hex.toHexString(AbiUtils.sha3(event.formatSignature()));
                return fSig.contentEquals(sig);
            }
        });
    }

    public Constructor getConstructor() {
        return find(Constructor.class, Type.constructor, new Predicate<Constructor>() {
            @Override
            public boolean evaluate(Constructor obj) {
                return true;
            }
        ;
    }

    );
    }

    public Function findFunction(Predicate<Function> searchPredicate) {
        return find(Function.class, ContractABI.Entry.Type.function, searchPredicate);
    }

    public Event findEvent(Predicate<Event> searchPredicate) {
        return find(Event.class, ContractABI.Entry.Type.event, searchPredicate);
    }

    public ContractABI.Constructor findConstructor() {
        return find(Constructor.class, Entry.Type.constructor, new Predicate<Constructor>() {
            @Override
            public boolean evaluate(Constructor object) {
                return true;
            }
        });
    }

    @Override
    public String toString() {
        return toJson();
    }

    @JsonInclude(Include.NON_NULL)
    public static abstract class Entry {

        public enum Type {
            constructor,
            function,
            event
        }

        @JsonInclude(Include.NON_NULL)
        public static class Param {

            public Boolean indexed;
            public String name;
            public SolidityType type;

            public static List<?> decodeList(List<Param> params, byte[] encoded) {
                List<Object> result = new ArrayList<>(params.size());

                int offset = 0;
                for (Param param : params) {
                    Object decoded = param.type.isDynamicType()
                            ? param.type.decode(encoded, decodeInt(encoded, offset).intValue())
                            : param.type.decode(encoded, offset);
                    result.add(decoded);

                    offset += param.type.getFixedSize();
                }

                return result;
            }

            public static byte[] encodeList(List<Param> params, Object... args) {

                if (args == null || args.length == 0) {
                    if (params.size() > 0) {
                        throw new ABIException("Too few arguments: 0 < " + params.size());
                    }
                    return new byte[0];
                }

                if (args.length > params.size()) {
                    throw new ABIException("Too many arguments: " + args.length + " > " + params.size());
                } else if (args.length < params.size()) {
                    throw new ABIException("Too few arguments: " + args.length + " < " + params.size());
                }

                int staticSize = 0;
                int dynamicCnt = 0;
                // calculating static size and number of dynamic params
                for (int i = 0; i < args.length; i++) {
                    SolidityType type = params.get(i).type;
                    if (type.isDynamicType()) {
                        dynamicCnt++;
                    }
                    staticSize += type.getFixedSize();
                }

                byte[][] bb = new byte[args.length + dynamicCnt][];
                for (int curDynamicPtr = staticSize, curDynamicCnt = 0, i = 0; i < args.length; i++) {
                    SolidityType type = params.get(i).type;
                    if (type.isDynamicType()) {
                        byte[] dynBB = type.encode(args[i]);
                        bb[i] = encodeInt(curDynamicPtr);
                        bb[args.length + curDynamicCnt] = dynBB;
                        curDynamicCnt++;
                        curDynamicPtr += dynBB.length;
                    } else {
                        bb[i] = type.encode(args[i]);
                    }
                }

                return AbiUtils.merge(bb);
            }

            @Override
            public String toString() {
                return format("%s%s%s", type.getCanonicalName(), (indexed != null && indexed) ? " indexed " : " ", name);
            }

            public Boolean isIndexed() {
                return indexed;
            }

            public String getName() {
                return name;
            }

            public SolidityType getType() {
                return type;
            }
        }

        public final Boolean anonymous;
        public final Boolean constant;
        public final String name;
        public final List<Param> inputs;
        public final List<Param> outputs;
        public final Type type;
        public final Boolean payable;

        public Entry(Boolean anonymous, Boolean constant, String name, List<Param> inputs, List<Param> outputs, Type type, Boolean payable) {
            this.anonymous = anonymous;
            this.constant = constant;
            this.name = name;
            this.inputs = inputs;
            this.outputs = outputs;
            this.type = type;
            this.payable = payable == null ? Boolean.FALSE : payable;
        }

        /**
         * Signature of this entry, before hashing, e.g., "myfunc(uint,bytes32)"
         *
         * @return
         */
        public String formatSignature() {
            StringBuilder paramsTypes = new StringBuilder();
            for (Entry.Param param : inputs) {
                paramsTypes.append(param.type.getCanonicalName()).append(",");
            }

            return format("%s(%s)", name, stripEnd(paramsTypes.toString(), ","));
        }

        /**
         * The SHA3 hash of this entry's signature
         *
         * @return
         */
        public byte[] fingerprintSignature() {
            return AbiUtils.sha3(formatSignature().getBytes());
        }

        /**
         * The SHA3 hash of this entry's signature
         *
         * @return
         */
        public byte[] encodeSignature() {
            return fingerprintSignature();
        }

        @JsonCreator
        public static Entry create(@JsonProperty("anonymous") boolean anonymous,
                @JsonProperty("constant") boolean constant,
                @JsonProperty("name") String name,
                @JsonProperty("inputs") List<Param> inputs,
                @JsonProperty("outputs") List<Param> outputs,
                @JsonProperty("type") Type type) {
            Entry result = null;
            switch (type) {
                case constructor:
                    result = new Constructor(inputs, outputs);
                    break;
                case function:
                    result = new Function(constant, name, inputs, outputs);
                    break;
                case event:
                    result = new Event(anonymous, name, inputs, outputs);
                    break;
            }

            return result;
        }

        public Boolean isAnonymous() {
            return anonymous;
        }

        public Boolean isConstant() {
            return constant;
        }

        public String getName() {
            return name;
        }

        public List<Param> getInputs() {
            return inputs;
        }

        public List<Param> getOutputs() {
            return outputs;
        }

        public Type getType() {
            return type;
        }
    }

    public static class Constructor extends Entry {

        public Constructor(List<Param> inputs, List<Param> outputs) {
            super(null, null, "", inputs, outputs, Type.constructor, false);
        }

        public List<?> decode(byte[] encoded) {
            return Param.decodeList(inputs, encoded);
        }

        public byte[] encode(Object... args) {
            return Param.encodeList(inputs, args);
        }

        public String formatSignature(String contractName) {
            return format("function %s(%s)", contractName, join(inputs, ", "));
        }
    }

    public static class Function extends Entry {

        private static final int ENCODED_SIGN_LENGTH = 4;

        public Function(boolean constant, String name, List<Param> inputs, List<Param> outputs) {
            super(null, constant, name, inputs, outputs, Type.function, false);
        }

        public String encodeAsHex(Object... args) {
            return Hex.toHexString(encode(args));
        }

        public byte[] encode(Object... args) {
            return AbiUtils.merge(encodeSignature(), Param.encodeList(inputs, args));
        }

        public List<?> decode(byte[] encoded) {
            return Param.decodeList(inputs, subarray(encoded, ENCODED_SIGN_LENGTH, encoded.length));
        }

        public List<?> decodeHex(String encoded) {
            return decode(Hex.decode(encoded.substring(2))); // chop off the leading 0x
        }

        public List<?> decodeResult(byte[] encoded) {
            return Param.decodeList(outputs, encoded);
        }

        public List<?> decodeHexResult(String encoded) {
            return decodeResult(Hex.decode(encoded.substring(2))); // chop off the leading 0x
        }

        @Override
        public byte[] encodeSignature() {
            return extractSignature(super.encodeSignature());
        }

        public static byte[] extractSignature(byte[] data) {
            return subarray(data, 0, ENCODED_SIGN_LENGTH);
        }

        @Override
        public String toString() {
            String returnTail = "";
            if (constant) {
                returnTail += " constant";
            }
            if (!outputs.isEmpty()) {
                List<String> types = new ArrayList<>();
                for (Param output : outputs) {
                    types.add(output.type.getCanonicalName());
                }
                returnTail += format(" returns(%s)", join(types, ", "));
            }

            return format("function %s(%s)%s;", name, join(inputs, ", "), returnTail);
        }
    }

    public static class Event extends Entry {

        public Event(boolean anonymous, String name, List<Param> inputs, List<Param> outputs) {
            super(anonymous, null, name, inputs, outputs, Type.event, false);
        }

        public List<?> decode(byte[] data, byte[][] topics) {
            List<Object> result = new ArrayList<>(inputs.size());

            byte[][] argTopics = anonymous ? topics : subarray(topics, 1, topics.length);
            List<?> indexed = Param.decodeList(filteredInputs(true), AbiUtils.merge(argTopics));
            List<?> notIndexed = Param.decodeList(filteredInputs(false), data);

            for (Param input : inputs) {
                result.add(input.indexed ? indexed.remove(0) : notIndexed.remove(0));
            }

            return result;
        }

        private List<Param> filteredInputs(final boolean indexed) {
            return select(inputs, new Predicate<Param>() {
                @Override
                public boolean evaluate(Param param) {
                    return param.indexed == indexed;
                }
            });
        }

        @Override
        public String toString() {
            return format("event %s(%s);", name, join(inputs, ", "));
        }

    }

}
