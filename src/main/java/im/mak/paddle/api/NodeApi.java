package im.mak.paddle.api;

import im.mak.paddle.api.deser.AssetDetails;
import im.mak.paddle.api.deser.NodeVersion;
import im.mak.paddle.api.deser.transactions.IssueTx;
import im.mak.paddle.api.deser.ScriptInfo;
import im.mak.paddle.api.deser.StateChangesInfo;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.util.List;

public interface NodeApi {

    @GET("addresses/scriptInfo/{address}")
    Call<ScriptInfo> scriptInfo(@Path("address") String address);

    @GET("assets/details/{assetId}")
    Call<AssetDetails> assetDetails(@Path("assetId") String assetId, @Query("full") boolean full);

    @GET("assets/nft/{address}/limit/{limit}")
    Call<List<IssueTx>> nft(
            @Path("address") String address,
            @Path("limit") int limit,
            @Query("after") String after
    );

    @GET("debug/stateChanges/info/{id}")
    Call<StateChangesInfo> stateChanges(@Path("id") String id);

    @GET("node/version")
    Call<NodeVersion> version();

}
