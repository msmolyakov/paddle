# Paddle for Waves

[![Maven Central](https://img.shields.io/maven-central/v/im.mak/paddle.svg?label=Maven%20Central)](https://search.maven.org/artifact/im.mak/paddle)

## What is Paddle?

Paddle is a Java library to write tests for your dApps and other smart contracts on Waves blockchain.

## Getting started

### Prerequisites

- Java 8 or higher;
- Docker 17.03.1 or newer if you want to use Waves Node in Docker. On Windows, install the latest "Docker for Windows".

### Installation

The easiest way to get started is to create a new project from the [paddle-example](https://github.com/msmolyakov/paddle-example) GitHub template.

Or add Paddle as dependency to your existing project.

#### Maven

```xml
<dependency>
    <groupId>im.mak</groupId>
    <artifactId>paddle</artifactId>
    <version>0.1</version>
</dependency>
```

#### Gradle

Groovy DSL:
```groovy
implementation 'im.mak:paddle:0.1'
```

Kotlin DSL:
```kotlin
compile("im.mak:paddle:0.1")
```

### Simple usage

```java
import im.mak.paddle.Account;
import im.mak.paddle.DockerNode;

public class Main {

    public static void main(String[] args) {

        // Download and run docker node
        DockerNode node = new DockerNode();

        // Create two accounts
        Account alice = new Account(node, 10_00000000); // account with 10 Waves
        Account bob = new Account(node);                // account with no waves

        // Send 3 Waves to Bob and wait ubtil the Transfer transaction appears in blockchain
        alice.transfers(t -> t.to(bob).amount(3_00000000));

        System.out.println( bob.balance() ); // 300000000
    }

}
```

### Example with JUnit

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

## Test lifecycle

Paddle is framework agnostic, i.e. Paddle could be used with JUnit, TestNG or any other test framework familiar to you.

In general, any test consists of the following steps:
1. run or connect to node;
2. create test accounts;
3. send some transactions;
4. assert specific conditions;
5. shutdown docker node, if used in step 1.

## Test environment

Paddle needs some Waves node to execute test scenarios.
It can run automatically node in Docker or connect to any other already running node.

### Docker

#### Default official image

By default, Paddle proposes to run tests locally in automatically configured environment.

How to run node before test:

```java
DockerNode node = new DockerNode();
```

That's it! It uses [the official Docker image of Waves node](https://hub.docker.com/r/wavesplatform/waves-private-node) with clean blockchain and most frequent blocks generation. It allows to run your tests faster than in the Testnet or Mainnet.

If you didn't have the docker image, Paddle will do it automatically for you!

At start, all Waves tokens are distributed to the special single miner account named "rich". This account is available as `node.rich` field of node instance.

When creating any account, Waves tokens for its initial balance are transferred from the rich account.

Don't forget shutdown node after test run:

```java
node.shutdown();
```

#### Custom Docker image

If you wish to use any custom docker image with Waves node, Paddle can do it:

```java
DockerNode node = new DockerNode(image, tag, apiPort, chainId, richSeedPhrase);
```

At now, custom image must expose port `6869` for REST API.

### Connect to existing Waves node

Paddle also can connect to nodes of any Waves blockchain: mainnet, testnet or any custom.

To connect to node, just provide node address, chainId and seed phrase of rich account:

```java
Node node = new Node("testnodes.wavesnodes.com", 'T', "your rich seed phrase");
```

Be aware in production networks like Mainnet! Your tests will spend your real money.

### Methods of Node instance

- `chainId()`
- `height()`
- `compileScript()`
- `isSmart()` for accounts and assets
- `send()`

## Account setup

In Paddle, Account is an actor of your test. It contains information about Waves account and can send transactions.

To create new account:

```java
Account alice = new Account(node, 10_00000000L);
```

Definition of test account requires node instance, where it will send all transactions and other requests.\
Optionally, you can specify seed phrase, otherwise it will be randomly generated.\
Also optionally, you can set initial Waves balance, otherwise account will not have Waves tokens at start.\
Technically, when you specify initial balance, node generates Transfer transaction from "rich" account to the created account.

### Retrieving info about account

Account can provide seed phrase, private and public keys, address. Account can check if it has a smart contract:

```java
alice.seed();
alice.privateKey();
alice.publicKey();
alice.address();

alice.isSmart();
```

Account can get Waves or asset balance and return data entries from its data storage:

```java
alice.balance();
alice.balance(assetId);

alice.data();
alice.dataByKey(key);

alice.dataBin(key);
alice.dataBool(key);
alice.dataInt(key);
alice.dataStr(key);
```

### Signing and sending transactions

Account can sign any bytes and send transactions:

```java
alice.sign(tx.getBodyBytes());

alice.issues(...);
alice.setsScript(...);
alice.invokes(...);
// and etc...
```

Sending a transaction, you can specify only the fields important for your scenario - all other fields will be set by default or calculated automatically.\
For example, you don't have to specify asset name and description for Issue transaction:

```java
alice.issues(a -> a.quantity(1000).decimals(0));
// only the number of tokens and decimals are indicated here
```

Also, in most cases, _transaction fee will be calculated automatically_ too. Exceptions at now: sponsored fee in tokens and InvokeScript transactions with smart assets in ScriptTransfers of dApp contract.

You don't need sign transactions explicitly - Paddle does it automatically.\
You don't need wait when transactions will be added to blockchain - Paddle does it automatically.

### Creating dApp and smart account

To create dApp or smart asset, you can provide script source from file:

```java
alice.setsScript(s -> s.script(file("wallet.ride")));
alice.issues(a -> a.script(file("fixed-price.ride")));
```

Or set script code directly as string:

```java
alice.setsScript(s -> s.script("sigVerify(tx.bodyBytes, tx.proofs[0], tx.senderPublicKey)"));
alice.issues(a -> a.script("true"));
```

In both cases Paddle will compile your script automatically.

## Assertions

### actions return transaction info

### state changes for InvokeScript transactions

### transactions rejection

If you expect that some transaction will return error and will not be in blockchain, you can add assertion with your test framework and catch the NodeError exception.\
For example, how it can look with JUnit 5:\

```java
NodeError error = assertThrows(NodeError.class, () ->
        bob.invokes(i -> i.dApp(alice).function("deposit").payment(500, assetId))
);
assertTrue(error.getMessage().contains("can accept payment in waves tokens only!"));
```

## Other features

### Waitings

### Asynchronous actions

## What next?

See [tests](https://github.com/msmolyakov/paddle/tree/master/src/test/java/im/mak/paddle) in the repository for examples how the Paddle can be used.

[paddle-example](https://github.com/msmolyakov/paddle-example) - project boilerplate with example of Waves dApp and tests with Paddle.
