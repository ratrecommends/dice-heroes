# Dice heroes

Dice heroes is a turn based rpg-strategy game where characters are dice.

### Running game locally
This project is built using gradle, so to run it you have to:

1. Clone this repository
2. Make sure you have jdk installed
3. In project's root, run in command line following command: `gradlew desktop:run` (on windows) or `./gradlew desktop:run` (on mac and linux)

Alternatively, you can import cloned repository in Intellij IDEA and run `desktop:run` gradle task from IDE.

### Project overview
1. `main` directory contains platform-independent code, "meat" of the game
2. `desktop` and `android` directories contain platform-dependent code, mostly about play services integration and in-app purchases (which are fake on desktop). To compile android project you will have to install [Android SDK](https://developer.android.com/sdk/index.html#downloads).
3. `editor` directory has internal editor for levels, you can run it with `editor:run` gradle task which is similar to `desktop:run`. It's a bit tricky, and it does not save anything, so to test levels in actual game you have to copy-paste it's output to `android/assets/levels.yml` and create new level on a map.
4. `generator/gfx` and `generator/world-map` directories contain images that are compiled into atlas before running a game.

### Contributing
1. Use [issues](https://github.com/ratrecommends/dice-heroes/issues) to submit bugs and feature requests. **Do not** use issues for personal support (use community, [english](https://www.facebook.com/rrg.dice.heroes/) or [russian](https://vk.com/dice.heroes)). Please note that all collaboration is in english language, so everyone can understand and take part in development.
2. Feel free to submit [pull requests](https://github.com/ratrecommends/dice-heroes/pulls).

### Licenses
1. Code is under [GNU GPLv3](https://gnu.org/licenses/gpl.html) ([tl;dr](https://tldrlegal.com/license/gnu-general-public-license-v3-%28gpl-3%29)).
2. Images are under [Creative Commons Attribution 4.0 License](https://creativecommons.org/licenses/by/4.0/legalcode) ([tl;dr](https://tldrlegal.com/license/creative-commons-attribution-4.0-international-%28cc-by-4%29)).
3. Sound licenses:
  - `map.mp3` and `ambient-battle.mp3` have `All Rights Reserved` license: do not distribute these sounds. Please contact [Sagamor](mailto:a.sukhotin@gmail.com)  (he is author) for further details.
  - all other sounds are taken from [freesound.org](http://freesound.org/) and modified in audacity, please help me find original authors to give them proper credits.
