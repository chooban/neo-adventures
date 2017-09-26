package com.chooban.neo;

import org.neo.smartcontract.framework.SmartContract;
import org.neo.smartcontract.framework.services.neo.Storage;

public class HelloWorld extends SmartContract {
	
    public static byte[] Main(String[] args) {
        Storage.put(Storage.currentContext(), "Greetings to the World", "Hello World!");
        return Storage.get(Storage.currentContext(),"Greetings to the World");
    }
}
