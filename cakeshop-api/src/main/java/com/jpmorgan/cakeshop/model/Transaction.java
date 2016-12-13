package com.jpmorgan.cakeshop.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpmorgan.cakeshop.model.ContractABI.Function;
import com.jpmorgan.cakeshop.util.StringUtils;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.bouncycastle.util.encoders.Hex;

@Entity
@Table(
        name = "TRANSACTIONS",
        indexes = {
            @Index(name = "tr_block_number_idx", columnList = "blockNumber"),
            @Index(name = "tr_contract_addr_idx", columnList = "contractAddress"),
            @Index(name = "tr_to_addr_idx", columnList = "to_address")
        }
)

public class Transaction implements Serializable {

    public static final String PRIVATE_TXN_V1 = "0x25";
    public static final String PRIVATE_TXN_V2 = "0x26";

    public class Input {
        private String method;
        private Object[] args;

        public Input(String method, Object[] args) {
            this.method = method;
            this.args = args;
        }

        public String getMethod() {
            return method;

        }
        public void setMethod(String method) {
            this.method = method;
        }

        public Object[] getArgs() {
            return args;
        }

        public void setArgs(Object[] args) {
            this.args = args;
        }
    }

    public static final String API_DATA_TYPE = "transaction";

	public static enum Status {
		pending,
		committed
	}

	@Id
	private String id;

	private Status status;

	private String nonce;

	private String blockId;

	private BigInteger blockNumber;

	private BigInteger transactionIndex;

    @Column(name = "from_address")
	private String from;
    @Column(name = "to_address")
	private String to;
    @Column(name = "trans_value")
	private BigInteger value;
	private BigInteger gas;
	private BigInteger gasPrice;

	@Lob
	@Column(name = "input_data", length=Integer.MAX_VALUE)
	private String input;

	@Transient
	private Input decodedInput;

	private BigInteger cumulativeGasUsed;
	private BigInteger gasUsed;

	private String contractAddress;

	@ElementCollection(fetch=FetchType.EAGER)
	private List<Event> logs;

	// signature fields (avail in quorum)
    private String r;
    private String s;
    private String v;


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNonce() {
		return nonce;
	}

	public void setNonce(String nonce) {
		this.nonce = nonce;
	}

	public String getBlockId() {
		return blockId;
	}

	public void setBlockId(String blockId) {
		this.blockId = blockId;
	}

	public BigInteger getBlockNumber() {
		return blockNumber;
	}

	public void setBlockNumber(BigInteger blockNumber) {
		this.blockNumber = blockNumber;
	}

	public BigInteger getTransactionIndex() {
		return transactionIndex;
	}

	public void setTransactionIndex(BigInteger transactionIndex) {
		this.transactionIndex = transactionIndex;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public BigInteger getValue() {
		return value;
	}

	public void setValue(BigInteger value) {
		this.value = value;
	}

	public BigInteger getGas() {
		return gas;
	}

	public void setGas(BigInteger gas) {
		this.gas = gas;
	}

	public BigInteger getGasPrice() {
		return gasPrice;
	}

	public void setGasPrice(BigInteger gasPrice) {
		this.gasPrice = gasPrice;
	}

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public BigInteger getCumulativeGasUsed() {
		return cumulativeGasUsed;
	}

	public void setCumulativeGasUsed(BigInteger cumulativeGasUsed) {
		this.cumulativeGasUsed = cumulativeGasUsed;
	}

	public BigInteger getGasUsed() {
		return gasUsed;
	}

	public void setGasUsed(BigInteger gasUsed) {
		this.gasUsed = gasUsed;
	}

	public String getContractAddress() {
		return contractAddress;
	}

	public void setContractAddress(String contractAddress) {
		this.contractAddress = contractAddress;
	}

	public List<Event> getLogs() {
		return logs;
	}

	public void setLogs(List<Event> logs) {
		this.logs = logs;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getR() {
        return r;
    }

    public void setR(String r) {
        this.r = r;
    }

    public String getS() {
        return s;
    }

    public void setS(String s) {
        this.s = s;
    }

    public String getV() {
        return v;
    }

    public void setV(String v) {
        this.v = v;
    }

    @Override
	public String toString() {
	    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

    public APIData toAPIData() {
        APIData data = new APIData();
        data.setId(getId());
        data.setType(API_DATA_TYPE);
        data.setAttributes(this);
        return data;
    }

    public void decodeContractInput(ContractABI abi) {
        if (getContractAddress() != null || getTo() == null
                || getInput() == null || getInput().isEmpty()) {

            return;
        }

        final String inputStr = getInput();

        Function func = abi.findFunction(new Predicate<ContractABI.Function>() {
            @Override
            public boolean evaluate(Function f) {
                return inputStr.startsWith("0x" + Hex.toHexString(f.encodeSignature()));
            }
        });

        if (func != null) {
            decodedInput = new Input(func.name, func.decodeHex(inputStr).toArray());
        }
    }

    public void decodeDirectTxnInput(String method) {
        final String directInput = getInput();
        ObjectMapper mapper = new ObjectMapper();
        Object[] data;
        try {
            data = mapper.readValue(new String(Hex.decode(directInput.replaceFirst("0x", ""))), Object[].class);
            decodedInput = new Input(method, data);
        } catch (IOException ex) {
            String decoded = new String(Hex.decode(directInput.replaceFirst("0x", "")));
            data = new Object[1];
            data[0] = decoded;
            decodedInput = new Input(method, data);
        }
    }

    public Input getDecodedInput() {
        return decodedInput;
    }

    public void setDecodedInput(Input decodedInput) {
        this.decodedInput = decodedInput;
    }

    /**
     * Whether or not the transaction is a public one
     *
     * @return
     */
    @JsonIgnore
    public boolean isPublic() {
        return !isPrivate();
    }

    public boolean isPrivate() {
        return (StringUtils.isNotBlank(v) && (v.equals(PRIVATE_TXN_V1) || v.equals(PRIVATE_TXN_V2)));
    }

}
