[![Maven Central](https://img.shields.io/maven-central/v/im.mak/paddle.svg?label=Maven%20Central)](https://search.maven.org/artifact/im.mak/paddle)

# Paddle для Waves

Paddle - Java библиотека для тестирования смарт контрактов на [блокчейне Waves](https://wavesplatform.com/).

<p align="right">Read this documentation in <a href="README.md">English</a>.</p>

## Оглавление

- [С чего начать](#с-чего-начать)
  - [Требования](#требования)
  - [Установка](#установка)
  - [Пример использования](#пример-использования)
  - [Пример теста на JUnit 5](#пример-теста-на-junit-5)
- [Жизненный цикл теста](#жизненный-цикл-теста)
- [Тестовое окружение](#тестовое-окружение)
  - [Запуск ноды в Docker](#запуск-ноды-в-docker)
  - [Подключение к существующей ноде](#подключение-к-существующей-ноде)
  - [Методы объекта Node](#методы-объекта-node)
- [Аккаунт](#аккаунт)
  - [Получение информации об аккаунте](#получение-информации-об-аккаунте)
  - [Подпись данных и отправка транзакций](#подпись-данных-и-отправка-транзакций)
  - [Создание dApp и смарт токена](#создание-dapp-и-смарт-токена)
- [Проверки](#проверки)
  - [Транзакция](#транзакция)
  - [Результат InvokeScript транзакции](#результат-invokescript-транзакции)
  - [Отмена транзакции](#отмена-транзакции)
- [Другие возможности](#другие-возможности)
  - [Ожидания](#ожидания)
  - [Асинхронные операции](#асинхронные-операции)
  - [RSA шифрование](#rsa-шифрование)
  - [Дерево Меркла](#дерево-меркла)
- [Что дальше?](#что-дальше)

## С чего начать

### Требования

- Java 8 и новее;
- Docker 17.03.1 и новее, если нужно использовать ноду Waves в Docker.\
  Для Windows установите последний Docker for Windows.

### Установка

Самый простой способ начать новый проект - создать его из GitHub-шаблона [paddle-example](https://github.com/msmolyakov/paddle-example).

Или, если уже есть существующий проект, добавьте Paddle как зависимость.

#### Maven

```xml
<dependency>
    <groupId>im.mak</groupId>
    <artifactId>paddle</artifactId>
    <version>0.4.1</version>
</dependency>
```

#### Gradle

Groovy DSL:
```groovy
implementation 'im.mak:paddle:0.4.1'
```

Kotlin DSL:
```kotlin
compile("im.mak:paddle:0.4.1")
```

### Пример использования

```java
import im.mak.paddle.Account;
import im.mak.paddle.DockerNode;

public class Main {

    public static void main(String[] args) {

        // Скачать Docker образ и запустить ноду Waves
        DockerNode node = new DockerNode();

        // Создать два аккаунта
        Account alice = new Account(node, 10_00000000); // аккаунт с балансом 10 Waves
        Account bob = new Account(node);                // аккаунт с пустым балансом

        // Отправить 3 Waves Бобу и подождать, пока Transfer транзакция попадет в блокчейн
        alice.transfers(t -> t.to(bob).amount(3_00000000));

        System.out.println( bob.balance() ); // 300000000
        
        // Выключить Docker контейнер с нодой
        node.shutdown();
    }

}
```

### Пример теста на JUnit 5

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

## Жизненный цикл теста

Paddle не зависит от тестового фреймворка. Вы можете использовать Paddle совместно с JUnit, TestNG или любым другим фреймворком.

В любом случае, тест будет состоять из следующих шагов:
1. запустить ноду или подключиться к существующей;
2. создать тестовые аккаунты;
3. отправить транзакции в блокчейн;
4. выполнить проверки;
5. выключить ноду, если была запущена на шаге 1.

## Тестовое окружение

Для выполнения тестовых сценариев необходима нода Waves.
Paddle может автоматически запустить ноду в Docker или подключиться к заданной работающей ноде.

### Запуск ноды в Docker

#### Официальный Docker образ

По умолчанию Paddle предлагает выполнять тесты локально в автоматически создаваемом окружении.

Чтобы запустить ноду в Docker, нужно выполнить:

```java
DockerNode node = new DockerNode();
```

И всё! Paddle использует [официальный образ приватной ноды Waves](https://hub.docker.com/r/wavesplatform/waves-private-node) с чистым блокчейном и более быстрой генерацией блоков, чем в мейннет или тестнет. Это позволяет выполнять тесты гораздо быстрее.

Если на машине установлен Docker, но нет образа ноды, то Paddle скачает его автоматически!

На момент старта ноды все токены Waves лежат на специальном аккаунте `rich`.\
К этому аккаунту можно обратиться через поле `node.rich` объекта `Node`. Аккаунт `rich` используется как "банк" для других тестовых аккаунтов.\
Когда в тесте создается новый аккаунт, его стартовый баланс пополняется переводом токенов Waves именно с `rich` аккаунта.

**Важно!** Не забывайте выключать контейнер ноды после выполнения тестов, иначе это помешает их следующему запуску:

```java
node.shutdown();
```

#### Произвольный Docker образ ноды

Если необходимо, Paddle позволяет запускать ноду из произвольного образа Docker:

```java
DockerNode node = new DockerNode(image, tag, apiPort, chainId, richSeedPhrase);
```

**Важно!** На данный момент требуется, чтобы образ открывал порт `6869` для доступа к REST API ноды.

### Подключение к существующей ноде

Paddle также позволяет подключиться к ноде в любой сети Waves: мейннет, тестнет или какой-то собственной.

Для подключения необходимо сообщить URL ноды, букву сети и seed фразу для `rich` аккаунта:

```java
Node node = new Node("testnodes.wavesnodes.com", 'T', "your rich seed phrase");
```

**Важно!** Будьте осторожны при работе в мейннет! Ваши тесты могут потратить реальные деньги.

### Методы объекта Node

- `node.chainId()` - буква сети ноды;
- `node.height()` - текущая высота блокчейна;
- `node.compileScript()` - скомпилировать скрипт RIDE;
- `node.isSmart(assetOrAddress)` - определить, является ли токен или аккаунт скриптованным;
- `node.send(...)` - отправить транзакцию;
- `node.api.assetDetails(assetId)` - информация о выпущенном токене;
- `node.api.nft(address)` - список NFT на счету у аккаунта;
- `node.api.stateChanges(invokeTxId)` - результат выполнения указанной InvokeScript транзакции.

## Аккаунт

В Paddle объект `Account` это действующее лицо в тестовом сценарии. В нем есть информация о Waves аккаунте и он может отправлять транзакции.

Чтобы создать аккаунт:

```java
Account alice = new Account(node, 10_00000000L);
```

В конструктор аккаунта необходимо передать объект ноды, на которую аккаунт будет отправлять транзакции и другие запросы.

Опционально можно задать seed фразу для аккаунта, иначе она будет сгенерирована автоматически.

Также опционально можно задать начальный баланс Waves, иначе аккаунт начнет работу с пустым кошельком.

Технически, чтобы у аккаунта появился начальный баланс, `rich` аккаунт делает перевод токенов на этот аккаунт Transfer транзакцией.

### Получение информации об аккаунте

У объекта `Account` есть методы для получения его сид фразы, приватного и публичного ключей, адреса. Аккаунт может подсказать, является ли он скриптованным:

```java
alice.seed();
alice.privateKey();
alice.publicKey();
alice.address();

alice.isSmart();
```

`Account` может сообщить свой баланс Waves или в любом токене, а также извлекать записи из своего хранилища данных:

```java
alice.balance();        // баланс Waves
alice.balance(assetId); // баланс в указанном токене
alice.nft();            // список non-fungible токенов на аккаунте

alice.data();           // все записи из хранилища данных этого аккаунта
alice.dataByKey(key);   // запись неопределенного типа по указанному ключу

alice.dataBin(key);     // запись типа byte[] по указанному ключу
alice.dataBool(key);    // запис типа boolean по указанному ключу
alice.dataInt(key);     // запись типа long по указанному ключу
alice.dataStr(key);     // запись типа String по указанному ключу
```

### Подпись данных и отправка транзакций

Аккаунт может подписывать данные и отправлять транзакции:

```java
alice.sign(tx.getBodyBytes());

alice.issues(...);
alice.setsScript(...);
alice.invokes(...);
// и все остальные типы транзакций...
```

Для создания транзакции можно указывать только те поля, которые важны для текущего сценария - в большинстве случаев остальные поля примут значения по умолчанию или будут рассчитаны автоматически.\
Например, для выпуска токена необязательно заполнять его имя и описание:

```java
alice.issues(a -> a.quantity(1000).decimals(0));
// здесь явно указаны только количество выпускаемого токена и количество знаков после запятой
```

Также _комиссия транзакции тоже определяется автоматически_! Единственное, на данный момент не рассчитываются комиссии в спонсорских токенах или если в результате InvokeScript транзакции передаются смарт токены.

Вам не нужно подписывать транзакции в коде - Paddle делает это автоматически.\
Вам не нужно явно ждать, пока транзакция попадет в блокчейн - Paddle делает это автоматически.

### Создание dApp и смарт токена

Чтобы создать dApp или смарт токен, скрипт для них можно хранить в отдельном файле, а в тесте просто указывать путь до него:

```java
alice.setsScript(s -> s.script(Script.fromFile("wallet.ride")));
alice.issues(a -> a.script(Script.fromFile("fixed-price.ride")));
```

Или, если необходимо, код контракта можно задать и напрямую в коде теста:

```java
alice.setsScript(s -> s.script("sigVerify(tx.bodyBytes, tx.proofs[0], tx.senderPublicKey)"));
alice.issues(a -> a.script("true"));
```

В обоих случаях не нужно компилировать скрипт - Paddle делает это автоматически.

## Проверки

### Транзакция

Любое действие по отправке транзакции возвращает объект этой транзакции из блокчейна. Таким образом, можно использовать свойства транзакции для проверок:

```java
String assetId = alice.issues(a -> a.quantity(1000)).getId().toString();

assertEquals(1000, alice.balance(assetId));
```

### Результат InvokeScript транзакции

Если используемая нода хранит результаты InvokeScript транзакций (зависит от конфигурации ноды), то их можно извлекать для проверок:

```java
String txId = bob.invokes(i -> i.dApp(alice)).getId().toString();

StateChanges changes = node.api.stateChanges(txId);

assertAll(
    () -> assertEquals(1, changes.data.size()),
    () -> assertEquals(bob.address(), changes.data.get(0).key),
    () -> assertEquals(100500, changes.data.get(0).asInteger())
);
```

### Отмена транзакции

Если по сценарию нода должна отклонить конкретную транзакцию и вернуть ошибку, т.е. транзакция не должна попасть в блокчейн, то это можно проверить, перехватив исключение `NodeError`.

Например, как это может выглядеть с JUnit 5:

```java
NodeError error = assertThrows(NodeError.class, () ->
        bob.invokes(i -> i.dApp(alice).function("deposit").payment(500, assetId))
);

assertTrue(error.getMessage().contains("can accept payment in waves tokens only!"));
```

## Другие возможности

### Ожидания

#### Высота

Paddle позволяет ждать, пока высота блокчейна вырастет на заданное количество блоков:
```java
node.waitNBlocks(2);
```
или пока блокчейн достигнет конкретной высоты:
```java
node.waitForHeight(100);
```

Оба метода используют "мягкие" ожидания. Это означает, что они продолжают ждать, пока высота растет с ожидаемой частотой. Ожидаемая частота задается свойством ноды `node.blockWaitingInSeconds`, но может быть изменена или переопределена вторым аргументом:
```java
node.waitNBlocks(2, waitingInSeconds);
node.waitForHeight(100, waitingInSeconds);
```

#### Транзакция в блокчейне

Также Paddle позволяет ждать, пока транзакция с заданным id не попадет в блокчейн:
```java
node.waitForTransaction(txId);
```

По умолчанию время ожидания транзакции задано в свойстве ноды `node.transactionWaitingInSeconds`, но может быть изменено или переопределено вторым аргументом:
```java
node.waitForTransaction(txId, waitingInSeconds);
```

### Асинхронные операции

Чтобы сократить время выполнения теста (или из других соображений для специфических кейсов) иногда необходимо выполнять какие-то действия асинхронно.

Например, по сценарию нужно создать несколько тестовых аккаунтов, и каждый из них должен выпустить свой токен:
```java
Account alice = new Account(node, 1_00000000);
Account bob = new Account(node, 1_00000000);
Account carol = new Account(node, 1_00000000);
alice.issues(a -> a.name("Asset 1"));
bob.issues(a -> a.name("Asset 2"));
carol.issues(a -> a.name("Asset 3"));
```
Эти 6 транзакций мало зависят друг от друга, но всё равно будут выполнены последовательно и с ожиданием попадания очередной транзакции в блокчейн. Но с методом `Async.async()` это можно выполнить асинхронно и в три раза быстрее:
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
Теперь операции будут выполняться в три потока, а последовательно будут отправляться только зависимые транзакции.

`Async` метод завершает работу, только когда все потоки завершены.

### RSA шифрование

Функция `rsaVerify()` в Ride проверяет, что RSA подпись валидна, т.е. была создана владельцем публичного ключа.

Paddle позволяет создавать такую подпись:
```java
Rsa rsa = new Rsa(); // сгенерированная пара приватного и публичного ключей
byte[] prKey = rsa.privateKey();
byte[] pubKey = rsa.publicKey();
// подпись, созданная этим приватным ключом. Данные хешируются по алгоритму SHA256
byte[] signature = rsa.sign(HashAlg.SHA256, "Hello!".getBytes());
```

`HashAlg` содержит все алгоритмы хеширования, поддерживаемые в Ride:
* NOALG
* MD5
* SHA1
* SHA224
* SHA256
* SHA384
* SHA512
* SHA3_224
* SHA3_256
* SHA3_384
* SHA3_512

### Дерево Меркла

Функция `checkMerkleProof()` в Ride проверяет, что данные являются частью дерева Меркла.

Paddle позволяет создать дерево Меркла по алгоритму, поддерживаемому в Ride, и получить пруфы для данных в этом дереве:
```java
List<byte[]> leafs = asList("one".getBytes(), "two".getBytes(), "three".getBytes());
MerkleTree tree = new MerkleTree(leafs);
byte[] rootHash = tree.rootHash();
byte[] proof = tree.proofByLeaf("two".getBytes()).get();
```

## Что дальше?

Ознакомьтесь с [тестами](https://github.com/msmolyakov/paddle/tree/master/src/test/java/im/mak/paddle) в этом репозитории. В них есть примеры, как можно использовать Paddle.

А также в GitHub-шаблоне [paddle-example](https://github.com/msmolyakov/paddle-example) есть пример dApp, покрытый тестами на Paddle.
