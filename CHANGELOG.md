## [1.24.3](https://github.com/raidcraft/rcskills/compare/v1.24.2...v1.24.3) (2021-01-11)


### Bug Fixes

* **msg:** display if skill slot is required ([8f4a8ec](https://github.com/raidcraft/rcskills/commit/8f4a8ec363c187da1973e02aa467e888a1c57812)), closes [#69](https://github.com/raidcraft/rcskills/issues/69)
* auto save when giving exp, level or skillpoints ([acb65bc](https://github.com/raidcraft/rcskills/commit/acb65bc9b25a583e1657a4361c86c1e5a3a3af93)), closes [#72](https://github.com/raidcraft/rcskills/issues/72)
* parent skill slot not replaced by child skill ([0c60219](https://github.com/raidcraft/rcskills/commit/0c602198eb69fc9b73a32746fcbb0406fe62816a)), closes [#68](https://github.com/raidcraft/rcskills/issues/68)

## [1.24.2](https://github.com/raidcraft/rcskills/compare/v1.24.1...v1.24.2) (2021-01-10)


### Bug Fixes

* **msg:** invalid newline break for single listed skills ([902220d](https://github.com/raidcraft/rcskills/commit/902220d70458ac791f15d3b7ea365c53663cce55))

## [1.24.1](https://github.com/raidcraft/rcskills/compare/v1.24.0...v1.24.1) (2021-01-10)


### Bug Fixes

* **msg:** clean up buttons in skill details ([9e2bafc](https://github.com/raidcraft/rcskills/commit/9e2bafc7a75808158d63597a7c7b70e644c87a19))

# [1.24.0](https://github.com/raidcraft/rcskills/compare/v1.23.0...v1.24.0) (2021-01-10)


### Bug Fixes

* child skills not spaced correctly ([f45d302](https://github.com/raidcraft/rcskills/commit/f45d30226f9afa2af40c235462a2166963d7a4a1))


### Features

* display cooldown and range in skill info ([77cfb28](https://github.com/raidcraft/rcskills/commit/77cfb28fcfd7c4cfe21010ea4bcf0372cf680436)), closes [#64](https://github.com/raidcraft/rcskills/issues/64)

# [1.23.0](https://github.com/raidcraft/rcskills/compare/v1.22.2...v1.23.0) (2021-01-09)


### Bug Fixes

* disabled worlds do not take precedence over enabled worlds ([148139b](https://github.com/raidcraft/rcskills/commit/148139bf83333471c1d5efed272f1c5f1c0d1f7e)), closes [#25](https://github.com/raidcraft/rcskills/issues/25)


### Features

* add option to globally disable worlds excluding certain skills ([13249bb](https://github.com/raidcraft/rcskills/commit/13249bbd646dd9abac419a82ee01cc8bfbbe8750)), closes [#25](https://github.com/raidcraft/rcskills/issues/25)
* add worldguard flag to deny skills ([c0e3d02](https://github.com/raidcraft/rcskills/commit/c0e3d02c258795572d2974137e4b2ad13d00648c)), closes [#25](https://github.com/raidcraft/rcskills/issues/25)

## [1.22.2](https://github.com/raidcraft/rcskills/compare/v1.22.1...v1.22.2) (2021-01-09)


### Bug Fixes

* npe when resolving skilled player without an input ([33298c4](https://github.com/raidcraft/rcskills/commit/33298c4e505da8d5dec013393934b1e8adc73cea))
* replace CommandUtils with inbuilt target selector matching ([3294a8d](https://github.com/raidcraft/rcskills/commit/3294a8da03d68942412a6adc275b12f7adab2a69))

## [1.22.1](https://github.com/raidcraft/rcskills/compare/v1.22.0...v1.22.1) (2021-01-09)


### Bug Fixes

* skills not disabled in disabled worlds ([0caeff4](https://github.com/raidcraft/rcskills/commit/0caeff401b600781fd33e7a5ca761743f98dfb5e))

# [1.22.0](https://github.com/raidcraft/rcskills/compare/v1.21.0...v1.22.0) (2021-01-09)


### Bug Fixes

* child skills not enabled when parent is replaced ([31d1bdb](https://github.com/raidcraft/rcskills/commit/31d1bdb4e57f347f77136d70eb395ab92dafb217))


### Features

* add support for disabling worlds for skills ([132cef4](https://github.com/raidcraft/rcskills/commit/132cef4e39fb6727409b3c51a0a396c9af906ce4)), closes [#65](https://github.com/raidcraft/rcskills/issues/65)
* **cmd:** add support for resolving players with minecraft target selectors ([188e36a](https://github.com/raidcraft/rcskills/commit/188e36a61805bea011787c586fd9aab744f66cec))

# [1.21.0](https://github.com/raidcraft/rcskills/compare/v1.20.3...v1.21.0) (2021-01-08)


### Bug Fixes

* add missing migrations for disabled property ([15eca5b](https://github.com/raidcraft/rcskills/commit/15eca5b88b1febd74053ab61a444ca5286ba1fe6))
* use ThreadLocalRandom seed to avoid multi threading issues ([17ff9be](https://github.com/raidcraft/rcskills/commit/17ff9be3df1c48940369ff225e1a22d467518de3))


### Features

* add option to enable skills only in specific worlds ([fbc1c7f](https://github.com/raidcraft/rcskills/commit/fbc1c7fe558fd754e900e04cd2a7eb385515db90)), closes [#65](https://github.com/raidcraft/rcskills/issues/65)

## [1.20.3](https://github.com/raidcraft/rcskills/compare/v1.20.2...v1.20.3) (2021-01-08)


### Bug Fixes

* **binding:** skills can be bound to air ([9e41aaa](https://github.com/raidcraft/rcskills/commit/9e41aaa549da86c4c1ca3f55207d37634b95defa))
* **cmd:** npe in /rcs info command ([c272fdc](https://github.com/raidcraft/rcskills/commit/c272fdc38d78c33120b8bb66572d0f71e140d585))

## [1.20.2](https://github.com/raidcraft/rcskills/compare/v1.20.1...v1.20.2) (2021-01-08)


### Bug Fixes

* cooldown not peristed in database ([073c3bd](https://github.com/raidcraft/rcskills/commit/073c3bd6ace4a373dab3941518fe24edd0d6a8e3))

## [1.20.1](https://github.com/raidcraft/rcskills/compare/v1.20.0...v1.20.1) (2021-01-08)


### Bug Fixes

* cooldown of 0 or less throws error ([6387ef0](https://github.com/raidcraft/rcskills/commit/6387ef03aa4dd99e07bc79a18bb42c7eec5c0329))

# [1.20.0](https://github.com/raidcraft/rcskills/compare/v1.19.0...v1.20.0) (2021-01-08)


### Bug Fixes

* **api:** add isOnCooldown method to Skill ([7571753](https://github.com/raidcraft/rcskills/commit/7571753014cc3a3989f3600eb12c5dfa2356b2a6))


### Features

* **api:** add alias, name and id delegates to Skill ([699a6e4](https://github.com/raidcraft/rcskills/commit/699a6e47ebe4589f206811d76a7e0f79eabe4329))

# [1.19.0](https://github.com/raidcraft/rcskills/compare/v1.18.2...v1.19.0) (2021-01-08)


### Features

* **api:** add PseudoRandomGenerator ([a0716bc](https://github.com/raidcraft/rcskills/commit/a0716bc15b880d2b1b9c499bc46bba3d063b7de6))

## [1.18.2](https://github.com/raidcraft/rcskills/compare/v1.18.1...v1.18.2) (2021-01-07)


### Bug Fixes

* auto activated skill is no skill slot assigned ([da15cd1](https://github.com/raidcraft/rcskills/commit/da15cd10983fd893c2df218e75d448af3e11d51a))
* free resets not assigned on levelup ([dc9161d](https://github.com/raidcraft/rcskills/commit/dc9161d284533dc38d683d2609107b59d42eb92e))

## [1.18.1](https://github.com/raidcraft/rcskills/compare/v1.18.0...v1.18.1) (2021-01-07)


### Bug Fixes

* new skill slots message not displayed on levelup ([67ce6d3](https://github.com/raidcraft/rcskills/commit/67ce6d3747254ddf97f1268f705cda76c11b111d))

# [1.18.0](https://github.com/raidcraft/rcskills/compare/v1.17.0...v1.18.0) (2021-01-07)


### Bug Fixes

* change order of /rcs info and /rcs list - take page first ([72e63fe](https://github.com/raidcraft/rcskills/commit/72e63fed1d38f6ffc9e028aa2fb85567c20707cb))
* skill slots do not show as resetted after /rcs reset ([d8f509e](https://github.com/raidcraft/rcskills/commit/d8f509e902eaab387a1842dc0c49e85f24298aa1))


### Features

* add /rcs skill info command ([30a49ef](https://github.com/raidcraft/rcskills/commit/30a49ef1816919f8e39e2256a98496fad9d4559f))
* add `auto-activate` option that auto assigns a skill to a free slot ([36bb95e](https://github.com/raidcraft/rcskills/commit/36bb95e5c1c6b4eb13eefed8d954df43dc92679e)), closes [#63](https://github.com/raidcraft/rcskills/issues/63)
* add free resets for player skills on level up ([f8f1fd3](https://github.com/raidcraft/rcskills/commit/f8f1fd3ea834406d15039f77162738516b173f53)), closes [#62](https://github.com/raidcraft/rcskills/issues/62)
* add levelup option to add free skill slots ([f191666](https://github.com/raidcraft/rcskills/commit/f1916669bef07a445db9a11ae251967114648aab))

# [1.17.0](https://github.com/raidcraft/rcskills/compare/v1.16.1...v1.17.0) (2021-01-07)


### Bug Fixes

* free slot count of 0 calculates 0 cost ([8024506](https://github.com/raidcraft/rcskills/commit/80245068dd83b5818ffe3a406ccfda2d89594078))


### Features

* allow free reset of skill slots under configured treshhold ([68f021b](https://github.com/raidcraft/rcskills/commit/68f021bf19625e20f3822cbb2b243a9b590be4a7)), closes [#52](https://github.com/raidcraft/rcskills/issues/52)

## [1.16.1](https://github.com/raidcraft/rcskills/compare/v1.16.0...v1.16.1) (2021-01-07)


### Bug Fixes

* **cmd:** skill list resets to page 1 after buying a skill ([626bf76](https://github.com/raidcraft/rcskills/commit/626bf763f1f3da56712dce9d8d295819caa26b23))

# [1.16.0](https://github.com/raidcraft/rcskills/compare/v1.15.1...v1.16.0) (2021-01-06)


### Bug Fixes

* activation of child skills where parent is not active ([183cd78](https://github.com/raidcraft/rcskills/commit/183cd78bc95d5418ac5df20513a2a2ad3bfb2539))
* calculate executable property dynamically and not from config ([1c8f5a7](https://github.com/raidcraft/rcskills/commit/1c8f5a71e57bc047188b9d9a74b03309a1271613)), closes [#60](https://github.com/raidcraft/rcskills/issues/60)
* check player id instead of offlineplayer for applicable check ([07e635b](https://github.com/raidcraft/rcskills/commit/07e635b6f68d57ff529c27bcd218023b5f78bd18))
* copy skill and execution configs into child skill ([40a2274](https://github.com/raidcraft/rcskills/commit/40a227440e2ed110d98250f7eaaa57c6088d5b1e))
* do not show activation notice for disabled skills ([777d788](https://github.com/raidcraft/rcskills/commit/777d788b8f253d0ecbe1120df6e70c855e66b207))
* execute nested child skills if parent is not executable ([7ddd619](https://github.com/raidcraft/rcskills/commit/7ddd619bc500197c04fbcd379ef1a43bd328d1da))
* only activate child skill if parent is activated ([86894fe](https://github.com/raidcraft/rcskills/commit/86894fee003754ed49b97cafbda264ee01ad9833))
* only auto unlock non-child skills ([4369a06](https://github.com/raidcraft/rcskills/commit/4369a067f7698f612afbe19a740a5b91107b80d1))
* only reload active skills ([646d50d](https://github.com/raidcraft/rcskills/commit/646d50da2551d5d52ccf256f93fc1d32b196f0d1))
* parent skill config overrides all values of child config ([932cb81](https://github.com/raidcraft/rcskills/commit/932cb811f73ef4a2b6060d0ef8c3634def53a4e2))
* remove permission before readding on reload ([780206c](https://github.com/raidcraft/rcskills/commit/780206c0fa7a360bcf685bcaec3d5253de7f9ff0))
* show complete info when buying slot or skill ([98a4380](https://github.com/raidcraft/rcskills/commit/98a438062da2fee03e51bfd3b3a53a617c098168))
* show current active skill in overview ([4e998e2](https://github.com/raidcraft/rcskills/commit/4e998e21b22b02fb4efc485a6428711598cc478a))
* show only 4 skills per page to make place for child skills ([5857388](https://github.com/raidcraft/rcskills/commit/58573885a1147458b3addea54f0d9e58b36ca65e))
* show unlocked child skills when leveling ([ef2ed8c](https://github.com/raidcraft/rcskills/commit/ef2ed8c445783b6332a9165ae368a2507332bc21))
* unlock deeply nested child skills when unlocking parent ([493e970](https://github.com/raidcraft/rcskills/commit/493e970386cd8e6a1899d2d7abf9b66b9b0336b1))
* update bindings cache after resetting skills ([ac5a4e3](https://github.com/raidcraft/rcskills/commit/ac5a4e38899d01e17d1e286dbcc5ee353d599088))
* **db:** cleanup slot -> skill foreign keys on player deletion ([b176625](https://github.com/raidcraft/rcskills/commit/b176625f03151bd7c6544c55d30e918012451001))


### Features

* activate child skills automatically when parent is activated ([66a8636](https://github.com/raidcraft/rcskills/commit/66a86364f1c32bba33c13b905e1d5b50352b80bd))
* add child skills to message display ([e862d2e](https://github.com/raidcraft/rcskills/commit/e862d2ed7ddd131c71b9f2f7e2e2708404c5cc5b))
* add parent -> child skills ([d2b209d](https://github.com/raidcraft/rcskills/commit/d2b209d18040250e9d8085438caf00a62302d89d))
* disable parent skill execution if child replaces it ([0f810fa](https://github.com/raidcraft/rcskills/commit/0f810fa3d38840f3cf65319eb4d851022d8cc5aa))
* show skills or player info after buy commands ([cca56dd](https://github.com/raidcraft/rcskills/commit/cca56dd18b078176db6f075b18783e8a226ac7d0))

## [1.15.1](https://github.com/raidcraft/rcskills/compare/v1.15.0...v1.15.1) (2021-01-04)


### Bug Fixes

* only show bindings for active skills ([50c023f](https://github.com/raidcraft/rcskills/commit/50c023fccf4b37486340483e5c31e82b31d635fa))
* remove bindings when skill is deactivated ([9c58fda](https://github.com/raidcraft/rcskills/commit/9c58fda4a0def0c875543c80287accaafc791afc))

# [1.15.0](https://github.com/raidcraft/rcskills/compare/v1.14.3...v1.15.0) (2021-01-04)


### Features

* improve binding messages and hint ([5e4e862](https://github.com/raidcraft/rcskills/commit/5e4e8621dc5eab00e4d081bfb3206a25c706c7db))

## [1.14.3](https://github.com/raidcraft/rcskills/compare/v1.14.2...v1.14.3) (2021-01-03)


### Bug Fixes

* accept skill id as command input for PlayerSkill ([e0b15de](https://github.com/raidcraft/rcskills/commit/e0b15de346d9415495af308fcd137826fec6c5c8))
* don't store categories in database ([0c19b29](https://github.com/raidcraft/rcskills/commit/0c19b29b561313019cf15c1373c28274fe950211))

## [1.14.2](https://github.com/raidcraft/rcskills/compare/v1.14.1...v1.14.2) (2021-01-03)


### Bug Fixes

* actively fetch the player skill when requested ([3e1aad1](https://github.com/raidcraft/rcskills/commit/3e1aad1b308553ec46d5276424dc9947a4074b66))

## [1.14.1](https://github.com/raidcraft/rcskills/compare/v1.14.0...v1.14.1) (2021-01-03)


### Bug Fixes

* load skill config after initializing config map ([1569480](https://github.com/raidcraft/rcskills/commit/1569480fefc99b0994ac0bc6c5ca69f69c21c627))

# [1.14.0](https://github.com/raidcraft/rcskills/compare/v1.13.0...v1.14.0) (2021-01-03)


### Features

* add /bind commands for active skills ([99e8c09](https://github.com/raidcraft/rcskills/commit/99e8c0988436b31c7eb34834e35ff1a6748c7366)), closes [#51](https://github.com/raidcraft/rcskills/issues/51)

# [1.13.0](https://github.com/raidcraft/rcskills/compare/v1.12.1...v1.13.0) (2021-01-02)


### Features

* display active/passive status in skill info ([9a5312c](https://github.com/raidcraft/rcskills/commit/9a5312c8f02b4bcda9ae1a61e20ef43a3cbc12fa)), closes [#41](https://github.com/raidcraft/rcskills/issues/41)

## [1.12.1](https://github.com/raidcraft/rcskills/compare/v1.12.0...v1.12.1) (2021-01-02)


### Bug Fixes

* award level specific skillpoints and slots when skipping level ([d521ef0](https://github.com/raidcraft/rcskills/commit/d521ef08ebc557fea31c09becc8bdbb13dcd21f4)), closes [#44](https://github.com/raidcraft/rcskills/issues/44)
* default skill description to N/A ([0457805](https://github.com/raidcraft/rcskills/commit/0457805b4b47ff31ffbaa0dc0cf09aa13f8b9087)), closes [#50](https://github.com/raidcraft/rcskills/issues/50)

# [1.12.0](https://github.com/raidcraft/rcskills/compare/v1.11.0...v1.12.0) (2021-01-02)


### Features

* **api:** add delegation methods for simpler access inside Skill ([f4d70c9](https://github.com/raidcraft/rcskills/commit/f4d70c9d6a346160cfab07493a61b304dad2ed74))

# [1.11.0](https://github.com/raidcraft/rcskills/compare/v1.10.0...v1.11.0) (2021-01-01)


### Bug Fixes

* set and update last used to calculate cooldown ([515fd36](https://github.com/raidcraft/rcskills/commit/515fd368a5ea3e476c81cd7fc920b1b8d9433c64))


### Features

* add Executable skills ([c9227c4](https://github.com/raidcraft/rcskills/commit/c9227c4e7b7f81b9dd12e9681ebf390bfbfef0d3))
* implemented delayed and cooldown skills ([df11bfc](https://github.com/raidcraft/rcskills/commit/df11bfc9dc3b296778fed8c372c441e81da49d41))

# [1.10.0](https://github.com/raidcraft/rcskills/compare/v1.9.1...v1.10.0) (2020-12-26)


### Features

* add active_skills counter to slot formula ([b4596c8](https://github.com/raidcraft/rcskills/commit/b4596c804a4addd82b534d09414729b7daffa817))

## [1.9.1](https://github.com/raidcraft/rcskills/compare/v1.9.0...v1.9.1) (2020-12-25)


### Bug Fixes

* register event listener of skills ([5ccb3c0](https://github.com/raidcraft/rcskills/commit/5ccb3c0d07987fee93f9490bfa54a760ebdec37e))

# [1.9.0](https://github.com/raidcraft/rcskills/compare/v1.8.2...v1.9.0) (2020-12-24)


### Bug Fixes

* skip serializing section root keys ([db9203e](https://github.com/raidcraft/rcskills/commit/db9203e28b923f14d14ff5d2b766b159cb31c339))
* use getter/setter everywhere to make use of ebean lazy loading ([3820dff](https://github.com/raidcraft/rcskills/commit/3820dffcf3f200a0037d105ca2e592fb6d35de62))


### Features

* add configurable sounds for levelup and skill unlock ([d93f16c](https://github.com/raidcraft/rcskills/commit/d93f16cf6c03694250c0eac3fe550288e37f3cae))

## [1.8.2](https://github.com/raidcraft/rcskills/compare/v1.8.1...v1.8.2) (2020-12-24)


### Bug Fixes

* load skill config from database before loading skill ([b93aab4](https://github.com/raidcraft/rcskills/commit/b93aab4229ccfd7d013caaee8704e71856bbdc2e))

## [1.8.1](https://github.com/raidcraft/rcskills/compare/v1.8.0...v1.8.1) (2020-12-24)


### Bug Fixes

* print information about loaded skills ([6b2c597](https://github.com/raidcraft/rcskills/commit/6b2c59795ca230f32588c4978f90b8b91844a03f))

# [1.8.0](https://github.com/raidcraft/rcskills/compare/v1.7.5...v1.8.0) (2020-12-23)


### Bug Fixes

* calculate exp for next level not current ([c26ef08](https://github.com/raidcraft/rcskills/commit/c26ef0812317b4f77eec1aaab35b83ca93a20c7c))
* send levelup messages in sync with bukkit api ([0fd8fb3](https://github.com/raidcraft/rcskills/commit/0fd8fb31dd69943b94032dee34c5cab614f61109))
* show correct exp to next level ([fdfa023](https://github.com/raidcraft/rcskills/commit/fdfa023b3f683bb9c27aa34bbbdfb62646873e15))


### Features

* add /rcsa reset command to purge player ([fad5e27](https://github.com/raidcraft/rcskills/commit/fad5e270f9207c92cb4e70252e607b3bd7973f59))
* add required exp for current level to display ([82e2570](https://github.com/raidcraft/rcskills/commit/82e2570632090287a052ce2605fafe10aaff8397))

## [1.7.5](https://github.com/raidcraft/rcskills/compare/v1.7.4...v1.7.5) (2020-12-23)


### Bug Fixes

* auto activate skills that are not unlocked by the player but unlockable ([0af4679](https://github.com/raidcraft/rcskills/commit/0af46792ce53a570a04a70bebf8e09a58dd6f975))
* near 0 exp progress bar throws error ([791f3c9](https://github.com/raidcraft/rcskills/commit/791f3c93c32105ee98ecfb658ce7a2c66077666c))
* npe when displaying skill with permission requirement ([b0d86c4](https://github.com/raidcraft/rcskills/commit/b0d86c4462e5886ea30d6bea2627c49eec57587c))

## [1.7.4](https://github.com/raidcraft/rcskills/compare/v1.7.3...v1.7.4) (2020-12-23)


### Bug Fixes

* show restricted skill requirement ([bf7e6f3](https://github.com/raidcraft/rcskills/commit/bf7e6f3d6425d3ea8244105beac64902f6e2d3df))

## [1.7.3](https://github.com/raidcraft/rcskills/compare/v1.7.2...v1.7.3) (2020-12-23)


### Bug Fixes

* create modules directory if not found ([6477d46](https://github.com/raidcraft/rcskills/commit/6477d46e609670f486a78fcde74a429a8e1cbed1))
* show skills on separate lines when leveling up ([5170ed3](https://github.com/raidcraft/rcskills/commit/5170ed3ffb6644c369f8f3b04cc2b15cfbca6789))

## [1.7.2](https://github.com/raidcraft/rcskills/compare/v1.7.1...v1.7.2) (2020-12-23)


### Bug Fixes

* **release:** release build artifacts ([413acea](https://github.com/raidcraft/rcskills/commit/413aceae82a78dbdaa000c79349da2e5807fbbc8))

## [1.7.1](https://github.com/raidcraft/rcskills/compare/v1.7.0...v1.7.1) (2020-12-23)


### Bug Fixes

* remove player from level up displays ([d44a6b1](https://github.com/raidcraft/rcskills/commit/d44a6b1b257f2c297012f75f85ed103385559d1a))

# [1.7.0](https://github.com/raidcraft/rcskills/compare/v1.6.0...v1.7.0) (2020-12-23)


### Bug Fixes

* order skill slots by status ([56fd755](https://github.com/raidcraft/rcskills/commit/56fd755c0de9a962644e9a6ffa97f29d24a2457c))
* skill activation cancelled when skill requires no slot ([c3987a7](https://github.com/raidcraft/rcskills/commit/c3987a76cf856f0c88141ad9b2a4d0c5ded44e95))


### Features

* improve slot help tooltip display next slot level and cost ([ac66cc9](https://github.com/raidcraft/rcskills/commit/ac66cc9c2ca6cddced719ff3970437692ac80b6f))

# [1.6.0](https://github.com/raidcraft/rcskills/compare/v1.5.0...v1.6.0) (2020-12-23)


### Bug Fixes

* convert skill uuid into string before saving in config ([75f690e](https://github.com/raidcraft/rcskills/commit/75f690ea88c190e9f88d5d30668bd55ef3138a42))
* correctly update skill config values in the database ([bf0ba6b](https://github.com/raidcraft/rcskills/commit/bf0ba6b8b3d43307deb978dd1efb3896a60b8394))
* deactivate disabled active skills ([896e642](https://github.com/raidcraft/rcskills/commit/896e64252389058e927f9c5f542019cef2aa2f11))
* disable skills that do not exist as config ([1968e7f](https://github.com/raidcraft/rcskills/commit/1968e7fb5d2d911a8106109c2ca2171843b34354))
* do not disable active and enabled skills when calling activate ([ec56c4c](https://github.com/raidcraft/rcskills/commit/ec56c4ca7ce072fff5977f69d2558da42ea3a0d0))
* do not succeed in testing skills when skill is disabled ([58d0521](https://github.com/raidcraft/rcskills/commit/58d052135e0ad2ff96d1f38e217c570e4f24e02d))
* hide disabled and hidden skills from list ([e5d966b](https://github.com/raidcraft/rcskills/commit/e5d966b1dfc5f6472696d831673827f4efc2a902))
* make /rcs buy skill the default for /rcs buy ([f70a923](https://github.com/raidcraft/rcskills/commit/f70a923bda8a1c547b9fb98efc062df238148f22))
* remove permissions before adding them to the server ([b57d899](https://github.com/raidcraft/rcskills/commit/b57d8994de22bb731db1a569017e7d8d8e118312))
* save skill id in configuration after initial load ([af000ff](https://github.com/raidcraft/rcskills/commit/af000ff90425add2c74548539897a98b7de314ec))


### Features

* activate auto unlockable skills on login ([23c452e](https://github.com/raidcraft/rcskills/commit/23c452e499eb965ee9ce6706cde84213cfcf1259))
* add /rcs buy slot command ([8475a5f](https://github.com/raidcraft/rcskills/commit/8475a5f37597e8ecd7b2803372e372bb8aed1a79))
* add confirm command for buying skill slots ([49f5cf8](https://github.com/raidcraft/rcskills/commit/49f5cf8524e8787afb36eb20b5e903c61b9e7094))
* add reset command to reset all skill slots ([ddf287e](https://github.com/raidcraft/rcskills/commit/ddf287e24a31a849389c33ecbd237c9d64672e1e))
* add restricted property that requires the permission to obtain a skill ([54412df](https://github.com/raidcraft/rcskills/commit/54412df3ff9f6e84bc8fce4e218bfb7f6c976323))
* add skill slot gui to player info ([5cd753c](https://github.com/raidcraft/rcskills/commit/5cd753c001b6b994a4c8e0ed9701bde70cde8b85))
* add skill specific skill slots ([30bc910](https://github.com/raidcraft/rcskills/commit/30bc91099fab55f89052609ee71172b5b4f3b181))
* auto unlock new auto unlockable skills at level up ([fdd8a35](https://github.com/raidcraft/rcskills/commit/fdd8a3505d44dcdd94627727fa4385d6032d3d38))
* greatly improve displaying of skills and their status ([e76f4b9](https://github.com/raidcraft/rcskills/commit/e76f4b9efd0815b791e8f511851571c28de324d2))
* show unlocked skills at level up ([00908dc](https://github.com/raidcraft/rcskills/commit/00908dc335a971e9d6692fc60f7fc374df6be023))

# [1.5.0](https://github.com/raidcraft/rcskills/compare/v1.4.1...v1.5.0) (2020-12-21)


### Features

* add very basic skills gui ([3493762](https://github.com/raidcraft/rcskills/commit/349376283d2e51b3ce8fb34cae4fc7c31b1adcad))

## [1.4.1](https://github.com/raidcraft/rcskills/compare/v1.4.0...v1.4.1) (2020-12-21)


### Bug Fixes

* **api:** return PlayerSkill in activate() and deactivate() methods ([6bc7486](https://github.com/raidcraft/rcskills/commit/6bc74867e0b1013e3126f3aeb5d7f1136f2c8abe))

# [1.4.0](https://github.com/raidcraft/rcskills/compare/v1.3.0...v1.4.0) (2020-12-21)


### Features

* **api:** add additional methods to create and load skills from a class ([e0e256d](https://github.com/raidcraft/rcskills/commit/e0e256d3a17fd3bfa79624193ef558a886811368))

# [1.3.0](https://github.com/raidcraft/rcskills/compare/v1.2.0...v1.3.0) (2020-12-20)


### Bug Fixes

* reload skills when /rcsa reload is called ([725b09f](https://github.com/raidcraft/rcskills/commit/725b09f1adef8d3d14190847b2d4ee95f82fdd8d))
* unload player skills when server shuts down ([d8e999b](https://github.com/raidcraft/rcskills/commit/d8e999b9fbcf2c81cd47986406ea092706e76154))


### Features

* skills can now depend on plugins ([a78db31](https://github.com/raidcraft/rcskills/commit/a78db313731bcd03f753cb337a5b043ca3bd82f1))

# [1.2.0](https://github.com/raidcraft/rcskills/compare/v1.1.1...v1.2.0) (2020-12-20)


### Bug Fixes

* refresh playerskill from database when fetched from skill context ([086b558](https://github.com/raidcraft/rcskills/commit/086b558fc139ad9eed76f4c50911b3f2b06de5a9))


### Features

* activate skill directly if it needs no skill slots ([e731f78](https://github.com/raidcraft/rcskills/commit/e731f78f0f45d124ae33c6820848e765b176501d))
* display details about exp gain ([135836c](https://github.com/raidcraft/rcskills/commit/135836c90c960d3b281c4a7ecf126a386bef0e2b))
* load skill modules from sub directory ([dbd4b08](https://github.com/raidcraft/rcskills/commit/dbd4b083422c350063dc356922c4a5df9cf02110))

## [1.1.1](https://github.com/raidcraft/rcskills/compare/v1.1.0...v1.1.1) (2020-12-20)


### Bug Fixes

* expose ebeanwrapper as api ([f5af42d](https://github.com/raidcraft/rcskills/commit/f5af42dd148f0262527763f4a8d609c9dc3c5f35))

# [1.1.0](https://github.com/raidcraft/rcskills/compare/v1.0.0...v1.1.0) (2020-12-20)


### Bug Fixes

* save skill state before activating it ([ff48f83](https://github.com/raidcraft/rcskills/commit/ff48f839ffed6e932d20ed4c7c509025f49b943a))


### Features

* add data store to PlayerSkill ([1622bdb](https://github.com/raidcraft/rcskills/commit/1622bdb073c03e339769a7f92878f2203ea4c4c3))
* add exp online time skill ([7b997d7](https://github.com/raidcraft/rcskills/commit/7b997d76a71f0491a06acf5ab21e212eef5fa832))

# 1.0.0 (2020-12-18)


### Bug Fixes

* **build:** do not fail on javadocs errors ([858ad1b](https://github.com/raidcraft/skills/commit/858ad1bdddcbe393cb83a544b169ec42b51223c0))
* **release:** use jdk11 when building with jitpack ([ffc6c31](https://github.com/raidcraft/skills/commit/ffc6c3118fed05074c2264cd6b155760fbff84f5))
* buying of skills now uses the economy wrapper ([5d6e975](https://github.com/raidcraft/skills/commit/5d6e975dc31abd99708d7894ee715445f13c616a))
* calculate free skillslots based on their configured value instead of pure count ([b11cb3e](https://github.com/raidcraft/skills/commit/b11cb3ebd4a2cb4a6de54dd445d2ec62d784b3cf))
* create new database migrations ([efbea28](https://github.com/raidcraft/skills/commit/efbea28b7b05325c79f340ae4eb3f996e34fcd11))
* delay loading of other skill types by 1 tick ([e59c492](https://github.com/raidcraft/skills/commit/e59c4929621fc9c5e557bf2be58fb60f738b995f))
* hide level progress bar after timeout ([bffeac2](https://github.com/raidcraft/skills/commit/bffeac23c982d92914e1ae6fbfdc36b8b8485d7f))
* set default skill status to NOT_PRESENT ([341a01c](https://github.com/raidcraft/skills/commit/341a01c3d44846976d3494431dd7ba0b14318324))
* **cmd:** add extra permission to show player info of others ([22ad811](https://github.com/raidcraft/skills/commit/22ad81187bd73aa05592e3d6923d68d25152c937))
* store ConfiguredSkill data in database ([fea0a36](https://github.com/raidcraft/skills/commit/fea0a36c2a09f0ce044dbe7fc4907fc13ce9f1c2))
* unlock skill directly when added and requirement checks pass ([21f59c7](https://github.com/raidcraft/skills/commit/21f59c72472baf1e54892693211a66ff6b95c4ff))


### Features

* add /rcs buy skill command and fix messages ([89d7b5e](https://github.com/raidcraft/skills/commit/89d7b5e6cfc73e2ee8f22b4f72dc4fa3df381330))
* add admin commands to add and remove skills from players ([4eac45c](https://github.com/raidcraft/skills/commit/4eac45c91c2ee2caddd61f9d712c12506fa75906))
* add buy confirm option when buying skills - improve a lot of the messages ([53579ad](https://github.com/raidcraft/skills/commit/53579ad41a280f3b4df09739be1164e85f0706b2))
* add config to let players gain skillpoints and slots on levelup ([c3c26dc](https://github.com/raidcraft/skills/commit/c3c26dcccb2cf53cdcce034df4c3d5f6ea6dbc7f))
* add database models and basic api ([aa0ca68](https://github.com/raidcraft/skills/commit/aa0ca6853ddffc576e7ccbe1a5fa8f11b7ac1833))
* add default required skill permission based on alias ([bc55156](https://github.com/raidcraft/skills/commit/bc55156b000dcd06f6018fc49ba955e93d690d4e))
* add detailed /rcs command for player information ([edcecc5](https://github.com/raidcraft/skills/commit/edcecc5bf7d8e3fbb29ce3804c3a396a87412be8))
* add first command ([67b7b49](https://github.com/raidcraft/skills/commit/67b7b4936a12892c113e369c57be7bde22d233e8))
* add individual player level table ([8dde16e](https://github.com/raidcraft/skills/commit/8dde16e96f00656f15a3cc78d32ff4c340b77a5f))
* add level requirement type ([a6927e8](https://github.com/raidcraft/skills/commit/a6927e863652c1bf1947d3d874055063f772fa6a))
* add money and skillpoint requirements ([d5a2549](https://github.com/raidcraft/skills/commit/d5a2549949889d7fca39b440d67c3c845d70c597))
* add option to customize requirement name and description per skill ([7e69894](https://github.com/raidcraft/skills/commit/7e6989486b45c5f09428ae21072ee9eb9772b070))
* add required level to skill list ([d1bf066](https://github.com/raidcraft/skills/commit/d1bf06614092987779c8f086f2b14abe18a55bd5))
* add skill activation command ([e0825f8](https://github.com/raidcraft/skills/commit/e0825f84b2b9fe6f5740393df2a299b1e535a33a))
* add skill and requirement types with annotations ([ac3798f](https://github.com/raidcraft/skills/commit/ac3798f4c861c9d6516554e1abe5c51944b7a2d2))
* add skill categories ([bd3d871](https://github.com/raidcraft/skills/commit/bd3d871f04aa77c1e54a91d23996f1a51b41401f))
* add skill money and skillpoint cost ([545a911](https://github.com/raidcraft/skills/commit/545a911392a0aaa537285e1bc76718504151d594))
* add SkillFactory and automatic scanning of all plugins for registered skills ([4fb4596](https://github.com/raidcraft/skills/commit/4fb4596fd4c56f3b141c18425e6e91efc7e04992))
* add skillslots to limit the number of active skills ([137067c](https://github.com/raidcraft/skills/commit/137067c172a195c98330e2199221f7b38b703e52))
* add sound and visual effect when a skill is unlocked ([68829b7](https://github.com/raidcraft/skills/commit/68829b7a2587161818165e38968401e46dc4e126))
* added player history ([ca19e83](https://github.com/raidcraft/skills/commit/ca19e8307daab77b01863d8a873eddaa87cb0d52))
* added skillslots and slot requirements ([267505a](https://github.com/raidcraft/skills/commit/267505ad1fefe97066664bf79d739598fb95d5a6))
* adjust total exp based on player level ([314b5e5](https://github.com/raidcraft/skills/commit/314b5e55273a320fe5e71e4c36df90b9b54dd36e))
* apply skills on player login and remove them on logout ([54e44a9](https://github.com/raidcraft/skills/commit/54e44a933c00c0330ef2bf2ca585eea6ee0903bf))
* broadcast levelup to all players ([51cb0c0](https://github.com/raidcraft/skills/commit/51cb0c0e529b5217c2676d2c197057bf7dd6f7c6))
* finish player skill saving into the database ([c232643](https://github.com/raidcraft/skills/commit/c23264310c97aa01a853a1664271a14d5e4cc628))
* implement leveling and level formula customization ([579ade6](https://github.com/raidcraft/skills/commit/579ade6f699174ecb067be87ed12163f6428ebd7))
* implement skill loading and the PermissionSkill ([79acda5](https://github.com/raidcraft/skills/commit/79acda543b75737d8f08ba34653157f2ccd72927))
* load skill instance per player ([9417381](https://github.com/raidcraft/skills/commit/9417381e63881b612d1af83a605adbc9d2cc4c3e))
* show exp and level progress bar ([036f0bc](https://github.com/raidcraft/skills/commit/036f0bc1a9a18aae90667acf7bbe40dc9ff71b5e))
* show level up title message ([e72bd5f](https://github.com/raidcraft/skills/commit/e72bd5f6002c3944af399e0415c9f398dffdfdb1))
* show skill unlock action title ([85c17bd](https://github.com/raidcraft/skills/commit/85c17bdcea654c403b1f776cbfab16b3b0e60a3b))
* **api:** add events for skill unlock and activate ([dc08ab7](https://github.com/raidcraft/skills/commit/dc08ab70a20abd8d3a377d98f00b81a779c9d9ce))
* **cmd:** add /rcs help command ([c4c1aa1](https://github.com/raidcraft/skills/commit/c4c1aa151196ab0d2a426a3c2dd5acf5bea60e8a))
* **cmd:** add commands for adding exp, level and skillpoints ([27147fd](https://github.com/raidcraft/skills/commit/27147fde2a999ca014a53448f06317c0e3b02231))
* replace active/unlocked with skill status enum for more flexibility ([2e88dc7](https://github.com/raidcraft/skills/commit/2e88dc77aa927f7458e9da67bf68301e66b24ea3))
* store skill configuration in the database ([e0d66b3](https://github.com/raidcraft/skills/commit/e0d66b34242acefb10fc6136da5fe966f9a11a22))
* **cmd:** add add and remove skill command ([b3e6d7e](https://github.com/raidcraft/skills/commit/b3e6d7e6c9a503f533c951204f6353fa0681d4d1))


### BREAKING CHANGES

* implementations of skill now require to extend AbstractSkill or provide an option to the PlayerSkill
