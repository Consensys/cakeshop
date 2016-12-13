package com.foo.bar;

import com.foo.bar.contract.SimpleStorageContract;
import com.foo.bar.contract.SimpleStorageContractApi;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.jpmorgan.cakeshop.client.ClientManager;
import com.jpmorgan.cakeshop.client.api.ContractApi;
import com.jpmorgan.cakeshop.client.model.Transaction;
import com.jpmorgan.cakeshop.client.model.TransactionResult;
import com.jpmorgan.cakeshop.client.proxy.ContractProxyBuilder;
import com.jpmorgan.cakeshop.client.ws.TransactionEventHandler;
import com.jpmorgan.cakeshop.model.ContractABI;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class Main {

    public static void main(String[] args) {

        String address = null;
        if (args.length > 0) {
            address = args[0];
        }

        if (address == null) {
            // enter address here or via command line
            address = "0x881ba6a77748bf1d54b7cb653eabba9a1bc7a0a2";
        }

        // setup our manager
        final ClientManager manager = ClientManager.create("http://localhost:8080/cakeshop");
        ContractApi contractApi = manager.getClient(ContractApi.class);

        // get val using "full" interface
        final SimpleStorageContract ss1 = SimpleStorageContract.at(contractApi, address);
        System.out.println(ss1.get());

        // get val using "light" interface
        ContractABI abi = ContractABI.fromJson(SimpleStorageContract.jsonAbi);
        final SimpleStorageContractApi ss2 = new ContractProxyBuilder(contractApi).build(SimpleStorageContractApi.class, address, abi);
        List<Object> get2 = ss2.get();
        System.out.println(get2);

        // watch txn events
        manager.subscribe(new TransactionEventHandler() {
            @Override
            public void onData(Transaction data) {
                System.out.println("Got txn: " + data.getId());
            }
        });

        // incr val by 1000
        TransactionResult tr = ss2.set(((int) get2.get(0)) + 1000);

        // wait for txn to be committed and print info
        final ListenableFuture<Transaction> txFuture = manager.waitForTx(tr);
        txFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("tx committed:\n" + txFuture.get().toString());
                    System.out.println(ss2.get()); // read new value
                    System.out.print("bye!");
                    manager.shutdown();
                    System.exit(0);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }, MoreExecutors.directExecutor());
    }

}
