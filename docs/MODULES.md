# Modules

The whole game lives in one file, `PokemonCardDuel.java`. It is about 1,660 lines long, so it is split into 8 sections with comments. This page describes what each section does.

Line numbers below are approximate. They may shift a little if the file is edited.

## Game words

A few words come up again and again. Here is what they mean in this game.

- **Dice:** the game's money. Each player rolls 8 dice at the start of every round and spends them to attack, play cards or switch Pokémon.
- **Element:** the type of a Pokémon or die (Fire, Water, Grass, Electric, Psychic). "Omni" dice work as any element.
- **Active Pokémon:** the one that is fighting right now. The other two wait on the "bench".
- **Energy:** a small meter on each Pokémon. It goes up by 1 with every attack. The strongest attack needs it full.
- **Aura:** when a Pokémon is hit, it gets the attacker's element on it. If it is then hit by a different element, a "reaction" happens, which means extra damage.
- **Support cards:** cards like Potion that a player plays from their hand by spending dice.

The full rules are in `HOW_TO_PLAY.md`.

## The sections at a glance

| Section | Lines | What it does |
|---|---|---|
| Data | 45-147 | Describes the things in the game: elements, cards, Pokémon, players |
| State | 148-223 | Keeps track of the game right now, and listens to the mouse |
| Game rules | 224-484 | All the game logic, including the computer opponent |
| Effects | 485-562 | Sparkles, floating numbers and the update timer |
| Drawing helpers | 563-799 | Small tools for drawing text, shapes and dice |
| Pokémon art | 800-960 | Draws each Pokémon using simple shapes |
| Cards and buttons | 961-1236 | Draws the cards and buttons on screen |
| Screens | 1237-1661 | The title, team select and battle screens, plus `main` |

A simple way to remember it: the first three sections are the game itself, and the rest is how it looks.

## 1. Data (lines 45-147)

This section only describes things. It has no game logic.

- `Elem` lists the six elements. Each one has a name and two colours.
- `Card` lists the four support cards: Potion, Rare Candy, Choice Band and Poké Ball.
- `Cost` is the price of a move: how many dice, and how much energy.
- `Hit` is a clickable spot on the screen, plus what should happen when it is clicked.
- `NAMES`, `ELEMS`, `SKILLS` and `DMG` are lists that hold the five Pokémon, their elements, their moves and the damage of each move.
- `Mon` is one Pokémon. It stores HP, energy and aura.
- `Side` is one player. It stores their team, dice and hand of cards.
- `Floater` and `Part` are for effects: a floating number and a single spark.
- `Screen` says which screen is showing: title, select or battle.

## 2. State (lines 148-223)

Everything that changes while the game is played is stored here: the current screen, both players, the battle log, whose turn it is, and the round number.

The constructor (the code that runs once at startup) also does the setup. It makes the stars in the background, sets up the mouse, and starts a timer that ticks every 16 milliseconds.

The `loc()` method turns a mouse position into a position on the game board. This keeps clicks working when the window is resized.

## 3. Game rules (lines 224-484)

This is the heart of the game. There is no drawing code in this section, so it can be read on its own to understand how the game works.

| Method | What it does |
|---|---|
| `cost` | Gives the price of a move |
| `payment` | Works out which dice to spend, or says it can't be paid |
| `spend` | Removes the dice that were paid |
| `reaction` | Names the reaction when two elements meet |
| `roll` | Rolls 8 dice for a player |
| `draw` | Draws support cards (a hand holds 7 at most) |
| `say` | Adds a line to the battle log |
| `startBattle` | Builds both teams and starts round 1 |
| `endRound` | Starts a new round: rolls dice, draws cards, decides who goes first |
| `after` | Decides whose turn comes next, or ends the round |
| `pass` | A player stops for this round |
| `act` | Uses a move: pays for it, adds energy, checks for a reaction, deals damage |
| `hurt` | Takes off HP, handles fainting, switching and winning |
| `play` | Plays a support card |
| `swap` | Switches the active Pokémon (costs 1 die) |
| `denied` | Shows "Not enough dice!" |
| `ai` | The computer opponent |

The computer opponent picks its move like this, from top to bottom:

1. Heal with a Potion if its HP is 5 or less.
2. Play Rare Candy if its energy is at 2.
3. Sometimes play Choice Band when its energy is full.
4. Sometimes switch Pokémon when its HP is 3 or less.
5. Use the strongest move it can pay for.
6. If it can't do anything, pass.

## 4. Effects (lines 485-562)

These are the small animations that don't affect the rules.

- `burst` makes a spray of sparks when something is hit.
- `floatTxt` makes numbers like "-3" float up and fade.
- `center` finds the middle of a card so the effects land in the right place.
- `ha` gives a smooth hover value for buttons, so they glow softly instead of snapping.
- `tick` runs every 16 milliseconds. It moves the sparks, updates the HP bars and lets the computer take its turn.

## 5. Drawing helpers (lines 563-799)

Small tools that the other drawing code uses again and again.

- Shapes: `rr` (rounded box), `hex` (hexagon), `star`.
- Colours and fonts: `a` (makes a colour see-through), `serif`, `sans`.
- Text: `cs` (centred text), `shadow`, `wrap` (breaks long text into lines), `goldText`.
- Decoration: `frame` draws the gold border.
- Game pieces: `sym` (element symbols), `gem` (one die), `pokeball`, `back` (the back of a card).

## 6. Pokémon art (lines 800-960)

There are no image files in this project. Each Pokémon is drawn with ovals and polygons, put together like a paper cut-out.

- `fe` draws an oval and `poly` draws a polygon.
- `eyes` draws a pair of eyes.
- `mon` draws a whole Pokémon from its number, and makes it float up and down a little.

## 7. Cards and buttons (lines 961-1236)

This section builds the pieces that the screens are made of.

- `monCard` draws a Pokémon card: the art, HP, aura, name, HP bar and energy dots.
- `icon` draws the small picture on a support card.
- `actCard` draws a support card in the player's hand. It looks dim when the player can't afford it.
- `btn` draws a move button along with its dice cost.
- `bigButton` draws the large gold buttons like BEGIN.

`btn` and `bigButton` also register the clickable area for the button.

## 8. Screens (lines 1237-1661)

This is where everything comes together on screen.

- `paintComponent` runs about 60 times a second. It fits the board to the window, adds screen shake, then draws the background, the current screen and the effects.
- `background` draws the night sky, the rings and the stars.
- `title` draws the title screen.
- `select` draws the team select screen, where the player picks 3 Pokémon.
- `battle` draws the main screen: both teams, the player's dice and hand, the move buttons, the battle log and the round panel. It also shows VICTORY or DEFEAT at the end.
- `hovCost` checks what the mouse is pointing at and lights up the dice it would cost.
- `effects` draws the sparks, floating numbers and the round banner on top of everything.
- `main` opens the window.

## How the sections connect

- The screens read the current state and draw it, using the card and button code.
- The game rules change the state.
- When something happens in the rules, like a hit, they ask the effects section for sparks and numbers. So the rules don't draw anything themselves, but they do trigger effects.
