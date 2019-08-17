# Paddle for Waves

## What is Paddle?

Paddle is a Java library to write tests for your dApps and other smart contracts on Waves blockchain.

## Getting started

### Installation

Add Paddle as dependency to your project.

#### Maven

In your project add into `pom.xml`:

```xml
<dependency>
    <groupId>im.mak</groupId>
    <artifactId>paddle</artifactId>
    <version>0.1</version>
</dependency>
```

### First test with Paddle

If you created project from the [paddle-example](https://github.com/msmolyakov/paddle-example) boilerplate or you use JUnit in existed project (for TestNG it's almost the same), add new Java class in `test` directory:

```java
import im.mak.paddle.Account;
import im.mak.paddle.DockerNode;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FirstTest {

    private DockerNode node;
    private Account alice, bob;
    private String assetId;
    
    @BeforeEach
    void before() {
        node = new DockerNode();

        alice = new Account(node, 10_00000000L);
        bob = new Account(node);
        
        assetId = alice.issues(a -> a
                .name("My Asset")
                .quantity(100)
                .script("2 * 2 == 4")
        ).getId().toString();
    }
    
    @Test
    void canSendSmartAsset() {
        alice.transfers(t -> t
                .to(bob)
                .amount(42)
                .asset(assetId)
        );
        
        assertEquals(42, bob.balance(assetId));
    }
    
    @AfterEach
    void after() {
        node.shutdown();
    }
}
```

### What next?

See [tests](https://github.com/msmolyakov/paddle/tree/master/src/test/java/im/mak/paddle) in the repository for examples how the Paddle can be used.

[paddle-example](https://github.com/msmolyakov/paddle-example) - project boilerplate with example of Waves dApp and tests with Paddle.

## Test lifecycle

Paddle is framework agnostic library, i.e. Paddle can be used with JUnit, TestNG or any test framework familiar to you.

In generally, test looks like:
1. Run or connect node
2. Define test accounts
3. Send some transactions
4. Assert specific conditions

## Test environment

### Docker

By default, Paddle proposes to run tests locally in automatically configured environment.

How to start node before test:

`DockerNode node = new DockerNode();`

That's it! It uses [docker image of Waves node]() with clean blockchain and most frequent block generation. It allows your tests to run more faster, than in the Testnet or Mainnet.

At start, all waves tokens are distributed to the special single miner account named "rich".

If you didn't download the docker image, Paddle will do it automatically for you!

But on official Waves releases you should update or delete you image manually:

`TODO docker rmi and pull example`

*TODO: auto update if checksum differs*

You can run any other node docker image:

`DockerNode node = new DockerNode(TODO ARGS);`

### Custom Waves node

Paddle also can run tests in any Waves network: mainnet, testnet or any custom network.

To connect to node, you just should provide node address, chainId and seed phrase of "rich" account:

`Node node = new Node("127.0.0.1:6869", 'T', "your rich seed phrase");`

Be aware in production networks like Mainnet! Your tests will spend your real money.

## Account setup

In Paddle, Account is an actor of your test. It contains info about Waves account and can send transactions.\
Definition of test account requires node instance, where it will send all requests.\
Optionally, you can specify seed phrase, otherwise it will be set randomly.\
Also optionally, you can set initial balance in Waves, otherwise account will not have Waves tokens at start.\
Technically, when you specify account balance, node creates transfer transaction from "rich" account to the created account.

Account can get seed phrase, private and public key, address. You can check is it smart.\
Account can return data entries from its data storage.\
Account can send transactions.

Sending transaction, you can specify only fields important to your scenario - all other fields will be set to default or calculated automatically.\
For example, you don't need to specify name or description for issue asset.\
Also, in most cases, transaction fee will be calculated automatically too. Exceptions at now: sponsored fee in tokens and InvokeScript transactions with smart assets in ScriptTransfers.

You don't need sign transactions explicitly - Paddle does it automatically.\
You don't need wait explicitly when transactions will be added to blockchain - Paddle does it automatically.

If you want to make sure that some unwanted transaction returns error, you can add assertion with your test framework and catch NodeError exception.\
For example, how it can look with JUnit 5:\
`TODO ... assertThrows`

If you want to set script to account or asset, you can provide its code from file.\
`TODO .script(file(relative path))`\
Or set script code as string directly.\
In both cases Paddle will compile your script automatically.

Examples:
- How to set script
- How to issue smart asset
- How to send data tx
- How to read data
- How to invoke dApp

Todo:
- Async
