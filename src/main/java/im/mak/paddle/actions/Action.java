package im.mak.paddle.actions;

public interface Action {

    //TODO static methods-constructors in each implementation: setScript(), invoke(), ...

    //TODO timestamp
    //TODO feeAssetId
    long calcFee();
}
