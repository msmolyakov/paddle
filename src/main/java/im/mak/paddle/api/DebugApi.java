package im.mak.paddle.api;

import retrofit2.Retrofit;

public class DebugApi {

    private Retrofit client;

    public DebugApi(Retrofit client) {
        this.client = client;
    }

//    public Transaction stateChangesInfo(String txId) { //TODO stateChanges(Base58 txId); stateChanges(Address address)
//
//    }

}
