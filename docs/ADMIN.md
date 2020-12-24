# Admin Dokumentation

* [Befehle](#befehle)
* [Configuration](#configuration)
  * [config.yml](#configyml)

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
