import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

/**
 * POKEMON - CARD INVOCATION
 *
 * <p>A card duel in the style of Genshin Impact's Genius Invokation TCG, drawn entirely with Java2D
 * (Swing). No images and no libraries are used. Run it with: java PokemonCardDuel.java (JDK 17+)
 *
 * <p>HOW THE GAME WORKS
 *
 * <ul>
 *   <li>Each side fields 3 Pokemon (10 HP each). One is "active" and can attack.
 *   <li>Every round both sides roll 8 elemental dice and draw 2 support cards.
 *   <li>Skills cost dice: Basic = 1 matching + 2 any, Skill = 3 matching, Ultimate = 3 matching and
 *       3 energy. Omni dice count as any element.
 *   <li>Hitting a Pokemon puts its element "aura" on it. Hitting it again with a DIFFERENT element
 *       triggers an elemental reaction: +2 damage.
 *   <li>Rounds end when both sides pass. Defeat all 3 enemy Pokemon to win.
 * </ul>
 *
 * <p>HOW THE CODE IS ORGANISED (top to bottom)
 *
 * <ol>
 *   <li>data - enums/records/classes describing elements, cards, Pokemon and players
 *   <li>state - the fields that hold the current game
 *   <li>game rules - paying costs, attacking, damage, cards, switching, rival AI (no drawing here)
 *   <li>fx helpers - particles, floating numbers and the per-frame update loop (tick)
 *   <li>painting primitives - small reusable drawing helpers (text, frames, symbols, dice)
 *   <li>pokemon art - each Pokemon is drawn from ellipses and polygons
 *   <li>cards - how a Pokemon card, a support card and the buttons look
 *   <li>screens - title, team select and battle screens, plus the main method
 * </ol>
 */
public class PokemonCardDuel extends JPanel {

  /** Width of the fixed logical board in pixels. */
  static final int W = 1360;
  /** Height of the fixed logical board in pixels. */
  static final int H = 860;
  /** Width of one Pokemon card in pixels. */
  static final int CW = 186;
  /** Height of one Pokemon card in pixels. */
  static final int CH = 248;
  /** Horizontal gap between adjacent Pokemon cards in pixels. */
  static final int GAP = 34;
  /** X coordinate of the left edge of the first Pokemon card column. */
  static final int CX0 = 277;

  /** Primary gold colour used for borders, text and highlights. */
  static final Color GOLD = new Color(232, 190, 96);
  /** Dark gold used for gradients and shadows. */
  static final Color GOLD_D = new Color(140, 98, 36);
  /** Near-black ink colour used for text on gold backgrounds. */
  static final Color INK = new Color(14, 16, 44);

  // ------------------------------------------------------------------ data

  /**
   * The six elemental types in the game.
   *
   * <p>Each constant carries a human-readable label and two colours: a bright fill ({@code c}) and
   * a darker shade ({@code d}) used for gradients and die faces. {@code OMNI} is a wildcard that
   * satisfies any element requirement in a {@link Cost}.
   */
  enum Elem {
    /** Yellow electric type; satisfies Electric cost slots. */
    ELECTRIC("Electric", new Color(255, 214, 64), new Color(168, 110, 0)),
    /** Orange-red fire type; satisfies Fire cost slots. */
    FIRE("Fire", new Color(255, 118, 70), new Color(150, 32, 12)),
    /** Blue water type; satisfies Water cost slots. */
    WATER("Water", new Color(78, 172, 255), new Color(12, 62, 150)),
    /** Green grass type; satisfies Grass cost slots. */
    GRASS("Grass", new Color(116, 220, 100), new Color(18, 100, 42)),
    /** Purple psychic type; satisfies Psychic cost slots. */
    PSYCHIC("Psychic", new Color(214, 124, 255), new Color(92, 32, 142)),
    /** White wildcard type; satisfies any cost slot regardless of element. */
    OMNI("Omni", new Color(238, 242, 255), new Color(118, 130, 172));

    /** Display name shown in the UI. */
    final String label;
    /** Bright fill colour for dice faces, symbols and highlights. */
    final Color c;
    /** Dark shade used for the lower half of gradient fills. */
    final Color d;

    Elem(String l, Color c, Color d) {
      label = l;
      this.c = c;
      this.d = d;
    }
  }

  /**
   * Support cards that players draw into their hand and play by spending dice.
   *
   * <p>Each constant stores a display title, a one-line description shown on the card face, and an
   * integer die cost (all slots are "any element").
   */
  enum Card {
    /** Restores up to 3 HP to the active Pokemon. */
    POTION("Potion", "Heal your active Pok\u00e9mon for 3 HP.", 1),
    /** Grants the active Pokemon 1 energy. */
    CANDY("Rare Candy", "Your active Pok\u00e9mon gains 1 Energy.", 1),
    /** Adds +2 damage to the player's next attack. */
    BAND("Choice Band", "Your next attack deals +2 damage.", 2),
    /** Draws 2 more cards into the hand. */
    BALL("Pok\u00e9 Ball", "Draw 2 cards from your deck.", 1);

    /** Short display name printed on the card ribbon. */
    final String title;
    /** One-line flavour / rules text printed in the card body. */
    final String text;
    /** Number of dice of any element required to play this card. */
    final int cost;

    Card(String t, String x, int c) {
      title = t;
      text = x;
      cost = c;
    }
  }

  /**
   * Immutable description of what a skill costs to use.
   *
   * @param need the element that must be matched ({@code null} means any element for all slots)
   * @param m    number of dice of the {@code need} element required
   * @param any  number of additional dice of any element required
   * @param en   minimum energy the active Pokemon must have accumulated
   */
  record Cost(Elem need, int m, int any, int en) {}

  /**
   * A clickable region registered during each paint pass.
   *
   * <p>The {@link #hits} list is cleared at the start of every {@code paintComponent} call and
   * repopulated as interactive elements are drawn, so hit-testing is always in sync with what is
   * visible on screen.
   *
   * @param id  unique string key used to look up the hover-animation value in {@link #anim}
   * @param r   bounding rectangle in logical board coordinates
   * @param act callback fired when the user clicks inside {@code r}
   */
  record Hit(String id, Rectangle r, Runnable act) {}

  /** Display names of the five playable species, indexed by species number 0–4. */
  static final String[] NAMES = {"Pikachu", "Charizard", "Blastoise", "Venusaur", "Mewtwo"};

  /** Elements of the five playable species, indexed by species number 0–4. */
  static final Elem[] ELEMS = {Elem.ELECTRIC, Elem.FIRE, Elem.WATER, Elem.GRASS, Elem.PSYCHIC};

  /**
   * Skill names for every species, indexed as {@code SKILLS[species][skill]}.
   * Skill index 0 = basic attack, 1 = skill, 2 = ultimate.
   */
  static final String[][] SKILLS = {
    {"Quick Attack", "Thunderbolt", "Volt Tackle"},
    {"Ember", "Flamethrower", "Blast Burn"},
    {"Water Gun", "Hydro Pump", "Hydro Cannon"},
    {"Vine Whip", "Solar Beam", "Frenzy Plant"},
    {"Confusion", "Psystrike", "Psycho Boost"}
  };

  /**
   * Base damage dealt by each skill tier before reactions or bonuses are applied.
   * Index 0 = basic (2), 1 = skill (3), 2 = ultimate (5).
   */
  static final int[] DMG = {2, 3, 5};


  /**
   * Mutable state for one Pokemon on the field.
   *
   * <p>Instances are created once per battle (via {@link #startBattle}) and mutated in place as
   * the game progresses. Animation fields ({@code hpShown}, {@code lunge}, {@code flash}) are
   * updated every tick by {@link #tick} and read during painting.
   */
  static class Mon {
    /** Species index (0–4); used to look up name, element, skills and to select the art branch. */
    final int sp;
    /** Display name, copied from {@link #NAMES} at construction. */
    final String name;
    /** Element type, copied from {@link #ELEMS} at construction. */
    final Elem e;
    /** Current HP. Starts at 10; the Pokemon faints when this reaches 0. */
    int hp = 10;
    /** Accumulated energy (0–3). Charged by basic/skill attacks; consumed by ultimates. */
    int en = 0;
    /**
     * Elemental aura currently applied to this Pokemon.
     * Set to the attacker's element on a hit; cleared when a reaction fires. {@code null} = no aura.
     */
    Elem aura;
    /** Smoothly interpolated HP value used for the animated HP bar; tracks {@code hp} at 10% per frame. */
    float hpShown = 10;
    /** Attack-lunge animation progress (0–1). Set to 1 on attack; decays to 0 each tick. */
    float lunge;
    /** Hit-flash brightness (0–1). Set to 1 on damage received; decays to 0 each tick. */
    float flash;

    Mon(int sp) {
      this.sp = sp;
      name = NAMES[sp];
      e = ELEMS[sp];
    }

    /** Returns {@code true} while the Pokemon has at least 1 HP remaining. */
    boolean alive() {
      return hp > 0;
    }
  }

  /**
   * Mutable state for one player (human or AI rival).
   *
   * <p>{@code sd[0]} is always the human player; {@code sd[1]} is always the AI rival.
   */
  static class Side {
    /** The three Pokemon on this side's team. */
    Mon[] team = new Mon[3];
    /** Index into {@code team} of the currently active (frontline) Pokemon. */
    int active;
    /** {@code true} once this side has ended the current round by calling {@link #pass}. */
    boolean passed;
    /**
     * Pending attack bonus in damage points, granted by playing a Choice Band card.
     * Consumed (and reset to 0) on the next attack.
     */
    int bonus;
    /** Current pool of elemental dice available for spending this round. */
    List<Elem> dice = new ArrayList<>();
    /** Cards currently held in hand (maximum 7). */
    List<Card> hand = new ArrayList<>();
  }

  /**
   * A transient floating-text effect (damage numbers, reaction names, card effects).
   *
   * <p>Floaters drift upward and fade out over their {@code life} duration. They are stored in
   * {@link #floats} and removed by {@link #tick} once {@code life} reaches zero.
   */
  static class Floater {
    /** Text string to render. */
    String t;
    /** Current X position in board coordinates. */
    float x;
    /** Current Y position in board coordinates; decremented each tick. */
    float y;
    /** Remaining lifetime in seconds; starts at 1.4 and counts down to 0. */
    float life = 1.4f;
    /** Font size in points. */
    float size;
    /** Colour of the text. */
    Color c;
  }

  /**
   * One particle belonging to a burst explosion effect.
   *
   * <p>Particles are stored in {@link #parts} and removed by {@link #tick} once {@code life}
   * reaches zero. Each particle has a velocity, gravity-like acceleration on {@code vy}, and
   * slight horizontal damping on {@code vx}.
   */
  static class Part {
    /** Current X position. */
    float x;
    /** Current Y position. */
    float y;
    /** Horizontal velocity (pixels per tick). */
    float vx;
    /** Vertical velocity (pixels per tick); increases by 0.16 each tick (gravity). */
    float vy;
    /** Remaining lifetime in seconds. */
    float life;
    /** Diameter of this particle in pixels. */
    float size;
    /** Colour; occasionally white for sparkle variety. */
    Color c;
  }

  /** The three top-level screens the game can display. */
  enum Screen {
    /** Animated title screen with the five demo cards fanned out. */
    TITLE,
    /** Team-selection screen where the player picks three Pokemon. */
    SELECT,
    /** Main battle screen showing both fields, dice, hand and skill buttons. */
    BATTLE
  }

  // ------------------------------------------------------------------ state

  /** The screen currently being rendered and receiving input. */
  Screen scr = Screen.TITLE;

  /**
   * The two players. Index 0 is the human; index 1 is the AI rival.
   * Both entries are {@code null} until {@link #startBattle} is called.
   */
  final Side[] sd = new Side[2];

  /** Shared random-number generator used for dice rolls, card draws and AI decisions. */
  final Random rnd = new Random();

  /**
   * Species indices chosen on the team-select screen, in pick order.
   * The first entry becomes the initial active Pokemon.
   */
  final List<Integer> picks = new ArrayList<>();

  /** Battle log lines shown in the centre of the board. Only the most recent 5 are kept. */
  final List<String> log = new ArrayList<>();

  /** Active floating-text effects (damage numbers, reaction names). */
  final List<Floater> floats = new ArrayList<>();

  /** Active burst particles. */
  final List<Part> parts = new ArrayList<>();

  /**
   * Clickable regions for the current frame, rebuilt at the start of every {@code paintComponent}
   * call. The mouse handler iterates this list in reverse to respect paint order (topmost first).
   */
  final List<Hit> hits = new ArrayList<>();

  /**
   * Hover-animation values keyed by {@link Hit} id. Each value is a float in [0, 1] that
   * approaches 1 while the cursor is over the matching hit, and 0 otherwise, at 20% per tick.
   */
  final Map<String, Float> anim = new HashMap<>();

  /**
   * Star-field data for the background: 170 stars, each stored as
   * {@code [x, y, twinkle_phase, size]}.
   */
  final float[][] stars = new float[170][4];

  /**
   * Five persistent {@link Mon} instances (one per species) used on the title and select screens.
   * They never take damage; they exist only for display purposes.
   */
  final Mon[] demo = new Mon[5];

  /** Id of the {@link Hit} the cursor is currently resting over, or {@code ""} if none. */
  String hov = "";

  /** Primary text of the round-start banner (e.g. "ROUND 2"). */
  String popText = "";

  /** Secondary text of the round-start banner (e.g. "You act first"). */
  String popSub = "";

  /**
   * Whose turn it is: 0 = human, 1 = AI rival.
   * Advanced by {@link #after} after each action.
   */
  int turn;

  /** Current round number, starting at 1. Incremented by {@link #endRound}. */
  int round;

  /**
   * Index of the side that ended the current round first (0 or 1).
   * That side acts first in the following round. {@code -1} until someone passes.
   */
  int firstPasser = -1;

  /**
   * Index of the winning side (0 = human won, 1 = rival won), or {@code -1} while the game is
   * still in progress.
   */
  int winner = -1;

  /**
   * Timestamp (from {@link System#currentTimeMillis}) at or after which the AI is allowed to take
   * its next action. Used to introduce a realistic-feeling delay between AI moves.
   */
  long aiAt;

  /** Monotonically increasing animation clock in seconds, incremented by 0.016 each tick. */
  float time;

  /**
   * Current screen-shake magnitude in pixels. Added to on hits and attacks; multiplied by 0.88
   * each tick until it falls below 0.3, at which point it is clamped to 0.
   */
  float shake;

  /**
   * Remaining display time for the round-start banner in seconds.
   * Set to 2.4 at the start of each round; decremented to 0 each tick.
   */
  float popT;

  /**
   * Uniform scale factor applied to map the fixed 1360×860 board into the actual window size.
   * Computed each frame in {@code paintComponent}.
   */
  double sc = 1;

  /** Horizontal pixel offset to centre the scaled board in the window. */
  double ox;

  /** Vertical pixel offset to centre the scaled board in the window. */
  double oy;

