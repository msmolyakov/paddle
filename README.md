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

If you created project from the Maven archetype or use JUnit, add new Java class:

```java
import im.mak.paddle.Account;
import im.mak.paddle.Node;
import org.junit.jupiter.api.*;

import static im.mak.paddle.Node.runDockerNode;
import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FirstTest {

    private Node node;
    private Account alice, bob;
    private String assetId;
    
    @BeforeAll
    void before() {
        node = runDockerNode();

        alice = new Account(node, 10_00000000L);
        bob = new Account(node);
        
        assetId = alice.issues(a -> a.name("My Asset").quantity(42).script("2 * 2 == 4")).getId().toString();
    }
    
    @AfterAll
    void after() {
        node.stopDockerNode();
    }
    
    @Test
    void canSendSmartAsset() {
        alice.transfers(t -> t.to(bob).amount(42).asset(assetId));
        
        assertThat(bob.balance(assetId)).isEqualTo(42);
    }
}
```

### What next?

See tests in [e2e](https://github.com/msmolyakov/paddle/tree/master/src/test/java/im/mak/paddle/e2e) package in the repository for examples how the Paddle can be used.

[paddle-example](https://github.com/msmolyakov/paddle-example) - project boilerplate with example of dApp and tests with Paddle.
