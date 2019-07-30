# Paddle for Waves

## What is Paddle?

Paddle is a Java library to write tests for your dApps and other smart contracts on Waves blockchain.

## Getting started

### Installation

Add Paddle as dependency to your project.

#### Maven

Create new project from Maven archetype:

```bash
mvn archetype:generate -DarchetypeGroupId=im.mak -DarchetypeArtifactId=paddle -DarchetypeVersion=0.1
```

Or in your current project add into `pom.xml`:

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
    private String assetId;
    
    @BeforeAll
    void before() {
        node = runDockerNode();

        alice = new Account(node, 10_00000000L);
        bob = new Account(node);
        
        assetId = alice.issues(a -> a.name("My Asset").script("2 * 2 == 4")).getId().toString();
    }
    
    @AfterAll
    void after() {
        node.stopDockerNode();
    }
    
    @Test
    void canSendSmartAsset() {
        alice.transfers(t -> t.to(bob).amount(10).asset(assetId));
        
        assertThat(bob.balance(assetId)).isEqualTo(10);
    }
}
```

### What next?

See tests in `e2e` package in the Paddle repository for examples how to use Paddle.
