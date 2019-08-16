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
