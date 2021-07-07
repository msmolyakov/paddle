package im.mak.paddle.assertj;

import com.wavesplatform.transactions.account.Address;
import com.wavesplatform.transactions.common.Amount;
import com.wavesplatform.transactions.common.Base64String;
import com.wavesplatform.transactions.data.*;
import com.wavesplatform.wavesj.LeaseInfo;
import com.wavesplatform.wavesj.StateChanges;
import com.wavesplatform.wavesj.actions.*;
import im.mak.paddle.dapp.DAppCall;
import im.mak.paddle.util.RecipientResolver;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertAll;

public class StateChangesAssert extends AbstractAssert<StateChangesAssert, StateChanges> {

    public StateChangesAssert(StateChanges actual) {
        super(actual, StateChangesAssert.class);
    }

    public static StateChangesAssert assertThat(StateChanges actual) {
        return new StateChangesAssert(actual);
    }

    public StateChangesAssert containsExactly(Consumer<StateChangesFields> result) {
        isNotNull();

        var fields = new StateChangesFields();
        result.accept(fields);

        assertAll(
                () -> Assertions.assertThat(actual.error()).isNotPresent(),
                () -> Assertions.assertThat(actual.data()).containsExactlyInAnyOrder(fields.dataEntries.toArray(DataEntry[]::new)),
                () -> Assertions.assertThat(actual.transfers()).containsExactlyInAnyOrder(fields.transfers.toArray(ScriptTransfer[]::new)),
                () -> Assertions.assertThat(actual.issues()).containsExactlyInAnyOrder(fields.issues.toArray(IssueAction[]::new)),
                () -> Assertions.assertThat(actual.reissues()).containsExactlyInAnyOrder(fields.reissues.toArray(ReissueAction[]::new)),
                () -> Assertions.assertThat(actual.burns()).containsExactlyInAnyOrder(fields.burns.toArray(BurnAction[]::new)),
                () -> Assertions.assertThat(actual.sponsorFees()).containsExactlyInAnyOrder(fields.sponsorFees.toArray(SponsorFeeAction[]::new)),
                () -> Assertions.assertThat(actual.leases()).containsExactlyInAnyOrder(fields.leases.toArray(LeaseInfo[]::new)),
                () -> Assertions.assertThat(actual.leaseCancels()).containsExactlyInAnyOrder(fields.leaseCancels.toArray(LeaseInfo[]::new)),
                () -> Assertions.assertThat(actual.invokes()).containsExactlyInAnyOrder(fields.invokes.toArray(InvokeAction[]::new))
        );

        return this;
    }

    public static class StateChangesFields {

        protected List<DataEntry> dataEntries;
        protected List<ScriptTransfer> transfers;
        protected List<IssueAction> issues;
        protected List<ReissueAction> reissues;
        protected List<BurnAction> burns;
        protected List<SponsorFeeAction> sponsorFees;
        protected List<LeaseInfo> leases;
        protected List<LeaseInfo> leaseCancels;
        protected List<InvokeAction> invokes;

        protected StateChangesFields() {
            dataEntries = new ArrayList<>();
            transfers = new ArrayList<>();
            issues = new ArrayList<>();
            reissues = new ArrayList<>();
            burns = new ArrayList<>();
            sponsorFees = new ArrayList<>();
            leases = new ArrayList<>();
            leaseCancels = new ArrayList<>();
            invokes = new ArrayList<>();
        }

        public StateChangesFields entry(DataEntry dataEntry) {
            dataEntries.add(dataEntry);
            return this;
        }

        public StateChangesFields binaryEntry(String key, Base64String value) {
            dataEntries.add(BinaryEntry.as(key, value));
            return this;
        }

        public StateChangesFields binaryEntry(String key, byte[] value) {
            dataEntries.add(BinaryEntry.as(key, value));
            return this;
        }

        public StateChangesFields binaryEntry(String key, String value) {
            dataEntries.add(BinaryEntry.as(key, value));
            return this;
        }

        public StateChangesFields booleanEntry(String key, boolean value) {
            dataEntries.add(BooleanEntry.as(key, value));
            return this;
        }

        public StateChangesFields integerEntry(String key, long value) {
            dataEntries.add(IntegerEntry.as(key, value));
            return this;
        }

        public StateChangesFields stringEntry(String key, String value) {
            dataEntries.add(StringEntry.as(key, value));
            return this;
        }

        public StateChangesFields deleteEntry(String key) {
            dataEntries.add(DeleteEntry.as(key));
            return this;
        }

        public StateChangesFields transfer(Address to, Amount amount) {
            transfers.add(new ScriptTransfer(to, amount));
            return this;
        }

        //todo other actions, error

        public StateChangesFields invoke(DAppCall dAppCall, List<Amount> payments, StateChanges stateChanges) {
            invokes.add(new InvokeAction(
                    RecipientResolver.toAddress(dAppCall.getDApp()),
                    dAppCall.getFunction(),
                    payments,
                    stateChanges));
            return this;
        }

        public StateChangesFields invoke(DAppCall dAppCall, List<Amount> payments) {
            return invoke(dAppCall, payments, new StateChanges());
        }

        public StateChangesFields invoke(DAppCall dAppCall, Amount... payments) {
            return invoke(dAppCall, Arrays.asList(payments));
        }

    }

}
