package im.mak.paddle.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import im.mak.paddle.api.deser.AssetDetails;
import im.mak.paddle.api.deser.NodeVersion;
import im.mak.paddle.api.deser.transactions.IssueTx;
import im.mak.paddle.api.deser.ScriptInfo;
import im.mak.paddle.api.deser.StateChangesInfo;
import im.mak.paddle.api.exceptions.ApiError;
import im.mak.paddle.exceptions.NodeError;
import okhttp3.HttpUrl;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.List;
import java.util.Objects;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.DeserializationFeature.USE_LONG_FOR_INTS;

public class Api {

    public Retrofit client;

    private NodeApi nodeApi;

    public Api(URI nodeUri) {
        client = new Retrofit.Builder()
                .baseUrl(HttpUrl.get(nodeUri))
                .addConverterFactory(JacksonConverterFactory.create(new ObjectMapper()
                        .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
                        .configure(USE_LONG_FOR_INTS, true)
                )).build();

        nodeApi = client.create(NodeApi.class);
    }

    public ScriptInfo scriptInfo(String address) {
        try {
            Response<ScriptInfo> r = nodeApi.scriptInfo(address).execute();
            if (!r.isSuccessful()) throw parseError(r);
            return r.body();
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public AssetDetails assetDetails(String assetId) {
        try {
            Response<AssetDetails> r = nodeApi.assetDetails(assetId, false).execute();
            if (!r.isSuccessful()) throw parseError(r);
            return r.body();
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public List<IssueTx> nft(String address, int limit, String after) {
        try {
            Response<List<IssueTx>> r = nodeApi.nft(address, limit, after).execute();
            if (!r.isSuccessful()) throw parseError(r);
            return r.body();
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public List<IssueTx> nft(String address, int limit) {
        return nft(address, limit, null);
    }

    public List<IssueTx> nft(String address, String after) {
        return nft(address, 10000, after);
    }

    public List<IssueTx> nft(String address) {
        return nft(address, null);
    }

    public StateChanges stateChanges(String txId) {
        try {
            Response<StateChangesInfo> r = nodeApi.stateChanges(txId).execute();
            if (!r.isSuccessful()) throw parseError(r);
            return Objects.requireNonNull(r.body()).stateChanges;
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public String version() {
        try {
            Response<NodeVersion> r = nodeApi.version().execute();
            if (!r.isSuccessful()) throw parseError(r);
            return Objects.requireNonNull(r.body()).version;
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    private ApiError parseError(Response<?> response) {
        Converter<ResponseBody, ApiError> converter =
                client.responseBodyConverter(ApiError.class, new Annotation[0]);
        try {
            return converter.convert(Objects.requireNonNull(response.errorBody()));
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

}
