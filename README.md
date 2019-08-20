[![Maven Central](https://img.shields.io/maven-central/v/im.mak/paddle.svg?label=Maven%20Central)](https://search.maven.org/artifact/im.mak/paddle)

# Paddle for Waves

Paddle is a Java library to write tests for your dApps and other smart contracts on Waves blockchain.

Read this documentation in [Russian](README_ru.md).

## Table of Contents

- [Getting started](#getting-started)
  - [Requirements](#requirements)
  - [Installation](#installation)
  - [Simple usage](#simple-usage)
  - [Example with JUnit 5](#example-with-junit-5)
- [Test lifecycle](#test-lifecycle)
- [Test environment](#test-environment)
  - [Run node in Docker](#run-node-in-docker)
  - [Connect to existing Waves node](#connect-to-existing-waves-node)
  - [Methods of Node instance](#methods-of-node-instance)
- [Account](#account)
  - [Retrieving information about account](#retrieving-information-about-account)
  - [Signing data and sending transactions](#signing-data-and-sending-transactions)
  - [Creating dApp and smart account](#creating-dapp-and-smart-account)
- [Assertions](#assertions)
  - [Transaction](#transaction)
  - [State changes of InvokeScript transaction](#state-changes-of-invokescript-transaction)
  - [Transactions rejection](#transactions-rejection)
- [Other features](#other-features)
  - [Waitings](#waitings)
  - [Asynchronous actions](#asynchronous-actions)
- [What next?](#what-next)

## Getting started

### Requirements

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
    <version>0.2.1</version>
</dependency>
```

#### Gradle

Groovy DSL:
```groovy
implementation 'im.mak:paddle:0.2.1'
```

Kotlin DSL:
```kotlin
compile("im.mak:paddle:0.2.1")
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
        Account bob = new Account(node);                // account with no Waves

        // Send 3 Waves to Bob and wait until the Transfer transaction appears in blockchain
        alice.transfers(t -> t.to(bob).amount(3_00000000));

        System.out.println( bob.balance() ); // 300000000
        
        node.shutdown();
    }

}
```

### Example with JUnit 5

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

But in general, any test consists of the following steps:
1. run or connect to node;
2. create test accounts;
3. send some transactions;
4. perform assertions;
5. shutdown docker node, if it's been used in step 1.

## Test environment

Paddle needs some Waves node to execute test scenarios.
It can run node in Docker automatically or connect to any other already running node.

### Run node in Docker

#### Default official image

By default, Paddle proposes to run tests locally in automatically configured environment.

To run docker node:

```java
DockerNode node = new DockerNode();
```

That's it! It uses [the official image of private Waves node](https://hub.docker.com/r/wavesplatform/waves-private-node) with clean blockchain and most frequent blocks generation. It allows to run your tests faster than in the Testnet or Mainnet.

If you don't have the docker image, Paddle will download it automatically!

At start, all Waves tokens are distributed to the special account named "rich".\
This account is available as `node.rich` field and it's used as a bank for initial balances of other test accounts.\
When creating any account, Waves tokens for its initial balance are transferred from the rich account.

**Note!** Don't forget shutdown node after test run:

```java
node.shutdown();
```

#### Custom Docker image

If you wish to use some custom docker image of Waves node, Paddle allows to do it:

```java
DockerNode node = new DockerNode(image, tag, apiPort, chainId, richSeedPhrase);
```

**Note!** For now custom image must expose port `6869` for REST API.

### Connect to existing Waves node

Paddle also can connect to running nodes of any Waves blockchain: mainnet, testnet or any custom.

To connect to node just provide node address, chain Id and seed phrase of rich account:

```java
Node node = new Node("testnodes.wavesnodes.com", 'T', "your rich seed phrase");
```

**Note!** Be aware in production networks like Mainnet! Your tests will spend your real money.

### Methods of Node instance

- `node.chainId()` - chain id of used blockchain;
- `node.height()` - current height of blockchain;
- `node.compileScript()` - compile RIDE script;
- `node.isSmart(assetOrAddress)` - returns true if asset or account is scripted;
- `node.send(...)` - send transaction;
- `node.api.assetDetails(assetId)` - information about issued asset;
- `node.api.nft(address)` - list of NFT for specified account;
- `node.api.stateChanges(invokeTxId)` - result of specified InvokeScript transaction.

## Account

In Paddle, Account is an actor of your test. It has information about Waves account and can send transactions.

To create new account:

```java
Account alice = new Account(node, 10_00000000L);
```

Definition of test account requires Node instance, where it will send all transactions and other requests.

Optionally, you can specify seed phrase, otherwise it will be randomly generated.

Also optionally, you can set initial Waves balance, otherwise account will not have Waves tokens at start.

Technically, when you specify initial balance, "rich" account sends Transfer transaction to the created account.

### Retrieving information about account

Account can provide seed phrase, private and public keys, address. Account can check if it's scripted:

```java
alice.seed();
alice.privateKey();
alice.publicKey();
alice.address();

alice.isSmart();
```

Account can get Waves or asset balance and read entries from its data storage:

```java
alice.balance();        // balance in Waves
alice.balance(assetId); // balance in some asset
alice.nft();            // list of non-fungible tokens on this account

alice.data();           // collection of all entries in account data storage
alice.dataByKey(key);   // entry of unknown type by specified key

alice.dataBin(key);     // binary entry by specified key
alice.dataBool(key);    // boolean entry by specified key
alice.dataInt(key);     // integer entry by specified key
alice.dataStr(key);     // string entry by specified key
```

### Signing data and sending transactions

Account can sign any bytes and send transactions:

```java
alice.sign(tx.getBodyBytes());

alice.issues(...);
alice.setsScript(...);
alice.invokes(...);
// and etc...
```

Sending a transaction, you can specify only the fields important for your scenario - in most cases all other fields will be set by default or calculated automatically.\
For example, you don't have to specify asset name and description for Issue transaction:

```java
alice.issues(a -> a.quantity(1000).decimals(0));
// only the number of tokens and decimals are specified here
```

Also, _transaction fee will be calculated automatically_ too! Exceptions at now: sponsored fee in tokens and InvokeScript transactions with smart assets in ScriptTransfers of dApp contract.

You don't need sign transactions explicitly - Paddle does it automatically.\
You don't need wait when transactions will be added to blockchain - Paddle does it automatically.

### Creating dApp and smart account

To create dApp or smart asset, you can provide script source from file:

```java
alice.setsScript(s -> s.script(Script.fromFile("wallet.ride")));
alice.issues(a -> a.script(Script.fromFile("fixed-price.ride")));
```

Or set script code directly as string:

```java
alice.setsScript(s -> s.script("sigVerify(tx.bodyBytes, tx.proofs[0], tx.senderPublicKey)"));
alice.issues(a -> a.script("true"));
```

In both cases Paddle will compile your script automatically.

## Assertions

### Transaction

Each account action returns an instance of the resulting transaction from blockchain.

```java
String assetId = alice.issues(a -> a.quantity(1000)).getId().toString();

assertEquals(1000, alice.balance(assetId));
```

### State changes of InvokeScript transaction

If connected node stores state changes of InvokeScript transactions (depends of the node config), you can check it:

```java
String txId = bob.invokes(i -> i.dApp(alice)).getId().toString();

StateChanges changes = node.api.stateChanges(txId);

assertAll(
    () -> assertEquals(1, changes.data.size()),
    () -> assertEquals(bob.address(), changes.data.get(0).key),
    () -> assertEquals(100500, changes.data.get(0).asInteger())
);
```

### Transactions rejection

If you expect that some transaction will return error and will not be in blockchain, you can check it by catching the NodeError exception.

For example, how it can look with JUnit 5:

```java
NodeError error = assertThrows(NodeError.class, () ->
        bob.invokes(i -> i.dApp(alice).function("deposit").payment(500, assetId))
);

assertTrue(error.getMessage().contains("can accept payment in waves tokens only!"));
```

## Other features

### Waitings

#### Height

Paddle allows to wait for the growth of the height of N blocks
```java
node.waitNBlocks(2);
```
or until the height of the blockchain reaches the specified
```java
node.waitForHeight(100);
```

The both methods have "soft" timeouts. This means that they continue to wait until the height rises with the expected frequency. The expected frequency is value of `node.blockWaitingInSeconds` but can be redefined by optional argument:
```java
node.waitNBlocks(2, waitingInSeconds);
node.waitForHeight(100, waitingInSeconds);
```

#### Transaction

Also Paddle can wait until transaction with specified id is in blockchain:
```java
node.waitForTransaction(txId);
```
It has default timeout in `node.transactionWaitingInSeconds` and can be redefined by optional argument.

### Asynchronous actions

To save execution time (or for other reasons in specific cases) it would be great to be able to perform some actions asynchronously.

For example, we want create some test account and each of them will issue asset:
```java
Account alice = new Account(node, 1_00000000);
Account bob = new Account(node, 1_00000000);
Account carol = new Account(node, 1_00000000);
alice.issues(a -> a.name("Asset 1"));
bob.issues(a -> a.name("Asset 2"));
carol.issues(a -> a.name("Asset 3"));
```
These 6 transactions will be sent consecutively. But with `Async.async()` their asynchronous execution will be three times faster:
```java
async(
    () -> {
        Account alice = new Account(node, 1_00000000);
        alice.issues(a -> a.name("Asset 1"));
    }, () -> {
        Account bob = new Account(node, 1_00000000);
        bob.issues(a -> a.name("Asset 2"));
    }, () -> {
        Account carol = new Account(node, 1_00000000);
        carol.issues(a -> a.name("Asset 3"));
    }
);
```

## What next?

See [tests](https://github.com/msmolyakov/paddle/tree/master/src/test/java/im/mak/paddle) in the repository for examples how the Paddle can be used.

[paddle-example](https://github.com/msmolyakov/paddle-example) - project boilerplate with example of Waves dApp and tests with Paddle.
