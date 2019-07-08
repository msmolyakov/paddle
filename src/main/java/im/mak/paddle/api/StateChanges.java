package im.mak.paddle.api;

import im.mak.paddle.api.deser.DataEntry;
import im.mak.paddle.api.deser.ScriptTransfer;

import java.util.List;

public class StateChanges {

    //TODO deser to HashMap or custom object with filter methods
    public List<DataEntry> data;
    public List<ScriptTransfer> transfers;

}
