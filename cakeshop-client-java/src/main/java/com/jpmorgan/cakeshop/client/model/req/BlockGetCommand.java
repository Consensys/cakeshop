package com.jpmorgan.cakeshop.client.model.req;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

import io.swagger.annotations.ApiModelProperty;

public class BlockGetCommand   {

    private String id = null;
    private Long number = null;
    private String tag = null;


    /**
     * Block ID (hash) to retrieve
     **/
    public BlockGetCommand id(String id) {
        this.id = id;
        return this;
    }

    @ApiModelProperty(example = "null", value = "Block ID (hash) to retrieve")
    @JsonProperty("id")
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }


    /**
     * Block number to retrieve
     **/
    public BlockGetCommand number(Long number) {
        this.number = number;
        return this;
    }

    @ApiModelProperty(example = "null", value = "Block number to retrieve")
    @JsonProperty("number")
    public Long getNumber() {
        return number;
    }
    public void setNumber(Long number) {
        this.number = number;
    }


    /**
     * One of \"earliest\", \"latest\" or \"pending\"
     **/
    public BlockGetCommand tag(String tag) {
        this.tag = tag;
        return this;
    }

    @ApiModelProperty(example = "null", value = "One of \"earliest\", \"latest\" or \"pending\"")
    @JsonProperty("tag")
    public String getTag() {
        return tag;
    }
    public void setTag(String tag) {
        this.tag = tag;
    }



    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BlockGetCommand command = (BlockGetCommand) o;
        return Objects.equals(this.id, command.id) &&
                Objects.equals(this.number, command.number) &&
                Objects.equals(this.tag, command.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, number, tag);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Command {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    number: ").append(toIndentedString(number)).append("\n");
        sb.append("    tag: ").append(toIndentedString(tag)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

