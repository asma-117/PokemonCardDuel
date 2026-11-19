# Technical Overview

This page lists the technologies and Java features used in the project. Each item has a short description and a pointer to where it appears in the code. Line numbers are approximate.

## Technologies

| Technology | Description | How it is used |
|---|---|---|
| Java 17 or newer | A general-purpose, object-oriented programming language | The whole game is written in it |
| Swing | Java's built-in toolkit for building desktop windows | Creates the window and runs the timer |
| Java2D | Java's built-in API for drawing 2D graphics | Draws every card, die, Pokémon and effect |
| Java standard library | The set of ready-made classes that come with Java | Lists, maps, random numbers and math |

No images, sounds or outside libraries are used. The whole project is one `.java` file and runs with `java PokemonCardDuel.java`.

## Java features used

### Classes and objects

| Feature | Description | Where |
|---|---|---|
| Class | A template that defines the data and behaviour of objects | `PokemonCardDuel` (line 39) |
| Inheritance | A class taking on the fields and methods of another class, using `extends` | `PokemonCardDuel extends JPanel` |
| Nested class | A class declared inside another class | `Mon`, `Side`, `Floater`, `Part` (lines 99-138) |
| Overriding | Giving a new version of a method that a parent class already defines | `paintComponent` (line 1240) and the mouse methods (lines 190-212) |
| `static` | A member that belongs to the class itself, not to one object | Helper methods like `cost` and `payment`, and the constants |
| `final` | A value that cannot be changed after it is set | Board size and gold colours (lines 40-43) |

### Enums and records

| Feature | Description | Where |
|---|---|---|
| Enum | A special type that holds a fixed set of named constants | `Elem`, `Card`, `Screen` |
| Enum with fields | An enum whose constants each carry their own data, set by a constructor | `Elem` stores a name and two colours; `Card` stores a title, text and cost |
| Record | A compact class for holding data, where Java creates the constructor and getters automatically | `Cost` (line 80) and `Hit` (line 83) |

### Storing data

| Feature | Description | Where |
|---|---|---|
| Array | A fixed-size container of values of the same type | `NAMES`, `ELEMS`, `DMG` (lines 86-96) |
| 2D array | An array whose items are themselves arrays | `SKILLS` (three moves per Pokémon) and `stars` |
| `List` / `ArrayList` | An ordered collection that can grow and shrink | Dice, hand, battle log, particles |
| `Map` / `HashMap` | A collection of key and value pairs, where each key leads to one value | `anim`, the hover amount for each button (line 162) |
| `Set` / `HashSet` | A collection that holds no duplicates | Dice marked for spending (line 1414) |
| `EnumSet` | A set built specifically for enum values | `reaction` (lines 273-282) |
| Generics | Type parameters that fix what a collection may hold, such as `List<Card>` | All lists and maps |

### Writing less code

| Feature | Description | Where |
|---|---|---|
| Lambda expression | A short unnamed function that can be passed around as a value | Button actions (for example line 1332) and the timer (line 216) |
| Method reference | A shorthand for a lambda that only calls an existing method, written `Class::method` | `Enum::ordinal`, used to sort dice (line 289) |
| Anonymous class | A class that is declared and created in one expression, without a name | The mouse listener (lines 188-213) |
| Switch expression | A `switch` that produces a value, written with `->` and no fall-through | Card effects (line 421), element symbols (line 695), screens (line 1258) |
| Ternary operator | A one-line conditional written `condition ? a : b` | `cost` (lines 226-228) |
| `var` | Lets the compiler work out the type of a local variable | The loop in `tick` (line 525) |
| Varargs | A parameter that accepts any number of values of one type | `poly` (line 807) |

### Loops and control

| Feature | Description | Where |
|---|---|---|
| Iterator | An object that walks through a collection and can remove items safely along the way | Removing finished sparks and floating text in `tick` (lines 540-554) |
| Enhanced `for` loop | A loop that visits each item of an array or collection in turn | Many places, such as drawing the stars (line 1293) |
| Boolean flag | A true or false variable that records a state | `passed` on each player |

### Other standard tools

| Feature | Description | Where |
|---|---|---|
| `Random` | A class that produces pseudo-random numbers | Dice rolls, card draws, particles, computer choices |
| `Collections.shuffle` | Puts the items of a list in random order | The computer's random team (line 309) |
| `StringBuilder` | A changeable sequence of characters used to build text | Word wrapping and the spaced-out title text |
| `System.currentTimeMillis` | Returns the current time in milliseconds | The computer's waiting time |
| `Math` | A class of standard maths functions such as sine and cosine | Floating motion, hexagon and star shapes |
| Unicode escape | A `\uXXXX` code that stands for one character | `\u00e9` for the é in Pokémon |
| Javadoc comment | A comment written between `/**` and `*/` that describes the code | Above almost every method |

## Swing features used

| Feature | Description | Where |
|---|---|---|
| `JFrame` | A top-level window | `main` (line 1653) |
| `JPanel` | A container that can be used as a drawing area | The game class extends it |
| `javax.swing.Timer` | A timer that runs an action at a fixed interval | Calls `tick` every 16 ms (line 216) |
| `SwingUtilities.invokeLater` | Runs code on the thread that handles the window | `main` (line 1651) |
| `MouseAdapter` | A helper class for handling mouse events | Lines 188-215 |

The imports at the top include `import java.util.List;`. Both `java.awt` and `java.util` contain a class named `List`, so this line tells Java which one to use.

## Java2D features used

| Feature | Description | Where |
|---|---|---|
| `Graphics2D` | The class that provides the drawing operations for shapes, text and images | Every painting method |
| Shape classes | Classes that describe forms to draw or fill | `Ellipse2D`, `RoundRectangle2D`, `Arc2D`, `Line2D`, `Path2D` |
| `GradientPaint` | Fills a shape with a smooth blend between two colours | Cards, buttons, gold frames |
| `RadialGradientPaint` | Fills a shape with a colour blend that spreads out from a centre point | Card glow and background light |
| `AffineTransform` | A change of position, rotation or size applied to what is drawn | Rotated title cards, scaled Pokémon, fitting the board to the window |
| Clipping | Limiting drawing to a chosen area | Keeps the Pokémon art inside its card |
| `AlphaComposite` | Controls how see-through the drawing is | Fading round banner |
| Rendering hints | Settings that control drawing quality, such as smooth edges | Start of `paintComponent` |
| Text outline | Text converted to a shape, so it can be filled like any other shape | `goldText` (line 615) |

## Run requirements

- JDK 17 or newer
- No other downloads needed

Run with `java PokemonCardDuel.java`, or compile first with `javac PokemonCardDuel.java` and then run `java PokemonCardDuel`.

### Alternative: run in the browser

If you don't have Java installed, you can run the game directly in a browser via Gitpod:

```
https://gitpod.io/#https://github.com/cse-asma/PokemonCardDuel
```

Gitpod provides a browser-based environment with Java pre-installed. The game starts automatically when the workspace loads. A free Gitpod account (sign up with GitHub) is required.
