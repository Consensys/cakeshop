package com.jpmorgan.cakeshop.manager.utils;

import com.jpmorgan.cakeshop.manager.db.entity.RemoteNode;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Utils {

    public static Function<List<RemoteNode>, List<RemoteNode>> otherNodes(String currentNodeUrl) {
        Function<List<RemoteNode>, List<RemoteNode>> otherNodes = (List<RemoteNode> nodes) -> {
            List<RemoteNode> nodesReturn = new ArrayList<>();
            nodes.stream().filter((node) -> (!node.getUrl().equals(currentNodeUrl))).forEachOrdered((node) -> {
                nodesReturn.add(node);
            });
            return nodesReturn;
        };
        return otherNodes;
    }
}
