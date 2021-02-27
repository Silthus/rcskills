# Admin Dokumentation

* [Befehle](#befehle)
* [Configuration](#configuration)
  * [config.yml](#configyml)
  * [Skills konfigurieren](#skills-konfigurieren)
* [Skill Types](#skill-types)
  * [permission](#permission)
  * [command](#command)

## Befehle

Alle [RCSkills](../Readme.md) Admin Befehle fangen mit `/rcsa` oder `/rcs:admin` an.

## Configuration

Die primäre Konfiguration des Plugins findet in der `config.yml` statt. Skills werden als einzelne Dateien im `skills/` Ordner konfiguriert. Die Module der Skills, also die `.jar` Dateien werden im `modules/` Ordner abgelegt.

> Wird ein Skill Modul nicht mehr gefunden wird der Skill automatisch deaktiviert. Deaktivierte Skills werden automatisch aus den Skill Slots der Spieler entfernt. Der Skill Slot kann dann für andere Skills verwendet werden. Wenn der Skill wieder aktiviert wird kann er wie gewohnt wieder neu einem Skill Slot zugewiesen werden.

### config.yml

```yaml
# The relative path where your skill configs are located.
skills_path: skills
# The relative path where your skill and effect modules (jar files) are located.
module_path: modules
# Set to true to automatically load skill classes and factories from other plugins.
load_classes_from_plugins: false
# Set to false if you want to disable broadcasting players leveling up to everyone.
broadcast_levelup: true
# The time in ticks until the /rcs buy command confirmation times out.
buy_command_timeout: 600
# The time in ticks how long the progress bar should be displayed.
exp_progress_bar_duration: 120
database:
  username: ${CFG_DB_USER}
  password: ${CFG_DB_PASSWORD}
  driver: ${CFG_DB_DRIVER}
  url: ${CFG_DB_URL}
# Define the expression that calculates the required exp for each level here.
level_config:
  # currently has no effect
  max_level: 100
  # a valid java expression is required to calculate the exp to the next level
  # the current level is used as the level variable
  exp_to_next_level: 100 + ((-0.4 * Math.pow(level, 2)) + (x * Math.pow(level, 2)))
  # either use the following variables in the expression or hard code them
  x: 10.4
  y: 0.0
  z: 0.0
# Define what a player automatically gets when he levels up.
level_up_config:
  # the number of skill points a player should get with each level up
  skill_points_per_level: 1
  # the number of skill slots a player gets with each level up
  slots_per_level: 0
  # a list of levels where the player should get extra skillpoints or slots
  levels:
    5:
      slots: 1
      skillpoints: 5
    10:
      slots: 1
# Define the expression that calculates the cost for buying new skill slots.
slot_config:
  # you can use the level, number of slots, number of skills and the reset count as variables in the expression
  slot_price: (Math.pow(2, slots) * x) + (Math.pow(level, 2) + Math.pow(skills, 2)) * y
  reset_price: Math.pow(2, resets) * a
  x: 1000.0
  y: 100.0
  z: 0.0
  a: 10000.0
  b: 0.0
  c: 0.0
```

### Skills konfigurieren

Alle Skills befinden sich als `.yml` Dateien im `skills/` Ordner und werden beim Start in die Datenbank synchronisiert. Dabei hat jeder Skill folgende Config Optionen plus die seines Types.

```yaml
# the id of the skill will be generated on first load
#
# !!! make sure to delete it if you copy the skill !!!
#
# id: 4929215e-0c3a-46a1-864c-6ec68e99c8ed
name: Example
description: Example Skill
# The alias is what is used in commands and permissions
alias: example
# The type is provided by the different skill implementations
# each type brings its own config that is provided in the "with" section
type: permission
# disabled skills will be removed from player slots automatically
enabled: true
# the skill points this skill needs when bought
skillpoints: 0
# the minimum level required to obtain the skill
level: 1
# the money this skill costs
money: 0.0
# set to true to not require a skill slot upon activation of the skill
no-skill-slot: false
# set to true to hide the skill from players
hidden: false
# set to true to require the rcskills.skill.<alias> permission for this skill
restricted: false
# set to true to auto unlock the skill if all requirements are met
auto-unlock: false
# define the categories of the skill here
categories:
  - test

# these values fine tune the execution of the skill
execution:
  # only applicable to targeted skills
  # defines the range of the skill, e.g. when shooting fireballs, or teleporting at the looked block
  range: 30
  # all of the following values can be defined with this time pattern:
  # 1y2mo3w4d5h6m10s5
  # 1 year 2 months 3 weeks 4 days 5 hours 6 minutes 10 seconds 5 ticks
  # the delay before executing the skill
  delay: 0
  # the warmup differs from the delay as it requires the player to not move and aborts on damage
  warmup: 0
  # the cooldown of the skill
  cooldown: 0

task:
  # the interval of the task timer in ticks
  # is only applicable to periodic skills
  interval: 10

# see the documentation of each skill for these values
with:
  permissions:
    - foobar

# the skills section allows you to define an infinite amount ob sub skills
# each sub skill automatically inherits all properties from its parent skill
# simply set the properties you want to override.
# a parent: UUID property will be set with the parent id
# child skills can be configured recursively
skills:
  foobar:
    type: permission
    with:
      permissions:
        - bar
```

## Skill Types

RCSkills comes with two built-in skill types: `permission` and `command`. The first one allows you to give a player a special permission when he has the skill active. The second one can execute commands when the skill is activated, removed or executed.

### permission

Gives the player a permission while he has the skill.

| Option | Default | Beschreibung |
| ------ | ------- | ----------- |
| `permissions` | `[]` | A list of permissions the player gets while he has the skill. Use the `command` skill if you are using LuckPerms and want to use context based permissions. |

### command

Executes a command as the player or server when the skill is applied, removed or executed.

| Option | Default | Beschreibung |
| ------ | ------- | ----------- |
| `apply` | `[]` | A list of commands that are executed when the skill is applied (e.g. the player joins or activates the skill). Playerholders are supported here. |
| `remove` | `[]` | A list of commands that are executed when the skill is removed (e.g. the player quits or resets his skills). Playerholders are supported here. |
| `execute` | `[]` | A list of commands that are executed when the skill is executed. Playerholders are supported here. |
| `server` | `false` | Set to `true` if the commands should be executed as the console. |
