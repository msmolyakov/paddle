MVP:
- [x] alice.exchanges()
- [x] correctly calc all fees
- [x] wrap wavesJ IOExceptions in my own
- [x] `alice.invokes(i -> i.dApp(dApp).func(""))`
- [x] fix: REST API key
- [ ] save test logs in target/datetime subdir + waves.log + test.log; verbose mode
- [x] AssertJ
- [x] move to own project
- [x] node.waitForHeight(long)
- [x] release WavesJ 0.15.3
- [ ] tutorial article to Habr (ru, en), Medium + Travis howto

Release:
- [x] `account.issuesNft()`
- [ ] alias in InvokeScript's dApp
- [ ] NFT api
- [ ] plugin for setScript/setAssetScript (deploy, test/main values, etc)
- [ ] OrderV3 (+ WavesJ)
- [ ] im.mak.paddle.Node extends wavesJ im.mak.paddle.Node; unify node connection and creation
- [ ] centralized error handling
- [ ] im.mak.paddle.api `account.data(regex)`
- [x] im.mak.paddle.api `account.scriptInfo()`
- [ ] im.mak.paddle.api `account.transactions(limit, after)` and `node.transactions(account, limit, after)`
- [ ] `.withProofs(...)` and don't sign if `[0]` is specified
- [ ] `node.send(invokeScript(alice).dApp("dApp").func(""))`
- [ ] `git describe` for version

IDEAS:
* ${var} in contracts. Access from Env instance with specified profile
* imports in contracts + imports hub
* unit testing
* is volume needed?
* alice.placesOrder(); alice.cancelsOrder(); add matcher + matcher.log into target. Fix: remove deprecated matcher settings from waves.conf
* create maven archetype
* status badges for Github
* support for Waves master branch
* support in IntelliJ plugin
* is local.conf works and needed?
* multi-stage build
* Scala-friendly API
