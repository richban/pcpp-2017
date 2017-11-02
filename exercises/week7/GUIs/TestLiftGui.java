// For week 7
// sestoft@itu.dk * 2014-10-18

// From JSR 305 jar file jsr305-3.0.0.jar:
// import javax.annotation.concurrent.GuardedBy;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TestLiftGui {
    public static final long delay = 100000L; // 100 ms == 100000 us
    public static final long period = 62500L; // 1 / 16 s == 62.5 ms == 62500 us

    public static void main(String[] args) {
        final Lift[] lifts = new Lift[] {
            new Lift("Lift 1", new LiftShaft(-2, 10)),
            new Lift("Lift 2", new LiftShaft(-2, 10)),
            new Lift("Lift 3", new LiftShaft(-2, 10)),
            new Lift("Lift 4", new LiftShaft(-2, 10))
        };

        LiftController controller = new LiftController(
                lifts[0],
                lifts[1],
                lifts[2],
                lifts[3]
        );

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(lifts.length);
        for (Lift lift : lifts) {
            executor.scheduleAtFixedRate(lift, delay, period, TimeUnit.MICROSECONDS);
        }

        // The graphical presentation
        final JFrame frame = new JFrame("TestLiftGui");
        final JPanel panel = new JPanel();

        frame.add(panel);
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        int k = 0;
        for (; k < lifts.length / 2; ++k) {
            panel.add(new LiftDisplay(lifts[k], true));
        }
        panel.add(new OutsideLiftButtons(controller));
        for (; k < lifts.length; ++k) {
            panel.add(new LiftDisplay(lifts[k], false));
        }

        frame.pack(); frame.setVisible(true);
    }
}

class LiftDisplay extends JPanel {
  public LiftDisplay(Lift lift, boolean buttonsLeft) {
    setLayout(new BorderLayout());
    InsideLiftButtons buttons = new InsideLiftButtons(lift);
    add(buttons, buttonsLeft ? BorderLayout.WEST : BorderLayout.EAST);
    add(lift.shaft, buttonsLeft ? BorderLayout.EAST : BorderLayout.WEST);
    lift.setInsideButtons(buttons);
  }
}

class LiftShaft extends Canvas {
  public final int lowFloor, highFloor;
  private double atFloor = 0.0,         // in [lowFloor, highFloor]
    doorOpen = 0.0;                     // in [0, 1]

  public LiftShaft() {
      this(-1, 5);
  }

  public LiftShaft(int lowFloor, int highFloor) {
      this.lowFloor = lowFloor;
      this.highFloor = highFloor;
    setPreferredSize(new Dimension(50, 280));
  }

  public void moveTo(double atFloor, double doorOpen) {
    this.atFloor = atFloor;
    this.doorOpen = doorOpen;
    repaint();
  }

  public void paint(Graphics g) {
    super.paint(g);
    final int floorCount = highFloor - lowFloor + 1;
    final int h = getHeight(), w = getWidth(), perFloor = (h-1) / floorCount,
      yOffset = highFloor * perFloor;
    g.setColor(Color.BLACK);
    g.drawRect(0, 0, w-1, floorCount * perFloor);
    // Draw lift cage and doors in red
    g.setColor(Color.RED);
    final int liftTop = yOffset + 2 - (int)(atFloor * perFloor + 0.5);
    g.drawRect(2, liftTop, w-5, perFloor-4);
    final int doorWidth = (int)(0.5 + (w-7)/2.0 * (1.0 - doorOpen));
    g.drawRect(3, liftTop+1, doorWidth, perFloor-6); // left door
    g.drawRect(w-4-doorWidth, liftTop+1, doorWidth, perFloor-6); // right door
    // Draw floor lines in grey at bottom of floors
    g.setColor(Color.GRAY);
    for (int floor=highFloor-1; lowFloor<=floor; floor--) {
      final int y = yOffset - floor * perFloor;
      g.drawLine(1, y, w-2, y);
    }
  }
}

class InsideLiftButtons extends JPanel {
    private JButton[] buttons;

  public InsideLiftButtons(final Lift lift) {
    setLayout(new GridBagLayout()); // To center button panel
    JPanel panel = new JPanel();
    panel.setBackground(Color.LIGHT_GRAY);
    add(panel);
    final int floorCount = lift.highFloor - lift.lowFloor + 1;
    panel.setLayout(new GridLayout(floorCount, 1));

    buttons = new JButton[floorCount];

    for (int floor=lift.highFloor; lift.lowFloor<=floor; floor--) {
      final int myFloor = floor;
      int index = floor - lift.lowFloor;
      buttons[index] = new JButton(Integer.toString(myFloor));
      panel.add(buttons[index]);
      buttons[index].addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            lift.goTo(myFloor);
          }});
    }
  }

  public void setButtonTextColor(int floor, Color textColor) {
      // FILL THIS
  }
}

class OutsideLiftButtons extends JPanel {
  public OutsideLiftButtons(LiftController controller) {
    final int floorCount = controller.highFloor - controller.lowFloor + 1;
    setLayout(new GridLayout(floorCount, 1));
    for (int floor=controller.highFloor; controller.lowFloor<=floor; floor--)
      add(new UpDownButtons(floor, controller));
  }
}

class UpDownButtons extends JPanel {
  public UpDownButtons(final int atFloor, final LiftController controller) {
    JPanel panel = new JPanel();
    panel.setBackground(Color.LIGHT_GRAY);
    add(panel);
    panel.setLayout(new GridLayout(2, 1));
    JButton up = new JButton("u"), down = new JButton("d");
    panel.add(up);
    panel.add(down);
    up.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          controller.someLiftTo(atFloor, Direction.Up);
        }});
    down.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          controller.someLiftTo(atFloor, Direction.Down);
        }});
    up.setVisible(atFloor < controller.highFloor);
    down.setVisible(atFloor > controller.lowFloor);
  }
}

// The lift model --------------------------------------------------

class LiftController {
  private final Lift[] lifts;
  public final int lowFloor, highFloor;

  public LiftController(Lift... lifts) {
    this.lifts = lifts;
    int lowFloor = Integer.MAX_VALUE, highFloor = Integer.MIN_VALUE;
    for (int i=0; i<lifts.length; i++) {
      lowFloor = Math.min(lowFloor, lifts[i].lowFloor);
      highFloor = Math.max(highFloor, lifts[i].highFloor);
    }
    this.lowFloor = lowFloor;
    this.highFloor = highFloor;
  }

  public void someLiftTo(int floor, Direction dir) {
    double bestTime = Double.POSITIVE_INFINITY;
    int bestLift = -1;
    for (int i=0; i<lifts.length; i++) {
      double thisLiftTime = lifts[i].timeToServe(floor, dir);
      if (thisLiftTime < bestTime) {
        bestTime = thisLiftTime;
        bestLift = i;
      }
    }
    lifts[bestLift].customerAt(floor, dir);
  }
}

class Lift implements Runnable {
    private static final double wakeUpPerSecond = 1 / (TestLiftGui.period / 1000_000.0);

    // Used in foor animation
    private double doorShift = 0.0;

    private InsideLiftButtons buttons;

  public final int lowFloor, highFloor;
  public final String name;
  public final LiftShaft shaft;
  // These two fields are accessed only on the Lift's own thread:
  private double floor;         // In interval [lowFloor...highFloor]
  private Direction direction;  // None or Up or Down
  // @GuardedBy("this")
  private final Direction[] stops;

  public Lift(String name, LiftShaft shaft) {
    // Topmost floor this elevator can reach
    this.lowFloor = shaft.lowFloor;

    // Lowermost floor this elevator can reach
    this.highFloor = shaft.highFloor;

    this.name = name;
    this.shaft = shaft;

    // Prograss to a floor. When integer,
    // it means that such floor is actually reached.
    this.floor = 0.0;

    // Current direction
    this.direction = Direction.None;

    // Every floor will tell wether the elevator
    // will stop there, and if it will in an upward
    // sweep, downwards or both.
    // Only floors for at which the elevator
    // will stop are not null.
    this.stops = new Direction[highFloor-lowFloor+1];
  }

  public void setInsideButtons(InsideLiftButtons buttons) {
      this.buttons = buttons;
  }

  // All these private methods are used in the lift's
  // up-sweep/down-sweep operation:

  private synchronized Direction getStop(int floor) {
      // This takes in account negative floors
      // which can not be array indexes
    return stops[floor-lowFloor];
  }

  private synchronized void setStop(int floor, Direction dir) {
      floor = floor - lowFloor;
      // This takes in account negative floors
      // which can not be array indexes
      stops[floor] = dir;

      final InsideLiftButtons buttons = this.buttons;

      if (dir != null) {
          /* Something like this */
          // FILL THIS
          //SwingUtil.invokeLater(() => {
              //buttons.setButtonTextColor(floor, Color.GREEN);
          //})
      }
      else {
          /* Something like this */
          // FILL THIS
          //SwingUtil.invokeLater(() => {
              //buttons.setButtonTextColor(floor, Color.BLACK);
          //})
      }
  }

  // Updates the floor 'stop' status, 'subtracting'
  // the passed direction from the current floor.
  //
  // Called every time the lift wakes up from sleeping
  // to update its own status.
  private synchronized void subtractFromStop(int floor, Direction dir) {
    switch (dir) {
        case Down:
          if (floor == lowestStop())

            // reached destination, clearing stop
            setStop(floor, null);
          else

            // Updating floor, setting to null as appropriate
            setStop(floor, dir.subtractFrom(getStop(floor)));
          break;
        case Up:
          if (floor == highestStop())

            // reached destination, clearing stop
            setStop(floor, null);
          else

            // Updating floor, setting to null as appropriate
            setStop(floor, dir.subtractFrom(getStop(floor)));
          break;
        default:
          throw new RuntimeException("impossible Lift.subtractFromStop");
    }
  }

  /**
   * Returns the topmost floor in the current sweep.
   *
   * This method returns the topmost floor in the
   * current sweep, that is the highest index for
   * which the stop is not null.
   *
   * @return The index of the topmost floor in the current sweep.
   */
  private synchronized int highestStop() {
    for (int floor=highFloor; lowFloor<=floor; floor--)
      if (getStop(floor) != null)
        return floor;
    return Integer.MIN_VALUE;
  }

  /**
   * Returns the lowermost floor in the current sweep.
   *
   * This method returns the lowermost floor in the
   * current sweep, that is the smallest index for
   * which the stop is not null.
   *
   * @return The index of the lowermost floor in the current sweep.
   */
  private synchronized int lowestStop() {
    for (int floor=lowFloor; floor<=highFloor; floor++)
      if (getStop(floor) != null)
        return floor;
    return Integer.MAX_VALUE;
  }

  /**
   * Returns the number of floors within a range for which
   * the elevator would stop at if sweeping in a given direction.
   *
   * @param from The lower floor of the interval.
   * @param to The higher floor of the interval.
   * @param dir The direction which should be contained the stops.
   * @return The number of stops within the range which contain dir.
   */
  private synchronized int stopsBetween(double from, double to, Direction dir) { // from <= to
    final int fromFloor = (int)(Math.floor(from)), toFloor = (int)(Math.ceil(to));
    int count = 0;
    for (int floor=fromFloor; floor<=toFloor; floor++)
      if (dir.isContainedBy(getStop(floor)))
        return count++;
    return count;
  }

  /**
   * Returns a number representing the time to move between
   * two floors in a specified direction.
   *
   * This methods returns a number which represents the time it
   * will take to go from one floor to another, moving in the
   * specified direction.
   * It takes the current stops in account. In particular, if
   * the elevator is to stop at a floor within the range, going
   * in the specified direction, the floor will count three timeToServe
   * than normal.
   *
   * @param from The floor to move from.
   * @param to The floor to move to.
   * @param dir The direction to move in.
   * @return A number representig the time.
   */
  private synchronized double timeToGo(double from, double to, Direction dir) {
      return
            // Every floor counts once
            Math.abs(to - from)
            // Floors included in the directino count twice more, hence three times
            + 2 * stopsBetween(from, to, dir);
  }

  // Estimate the lift's time to serve toFloor in the desired
  // direction; called by lift controller on the event thread
  public synchronized double timeToServe(int requestedFloor, Direction requestedDir) {
    switch (this.direction) {
    case Down:
      final int lowestStop = this.lowestStop();
      // The elevator is going down. The requested floor is
      // below the current one. The requested direction includes
      // going down or the requested floor is below the
      // lowest stop of the current swep.
      //
      // This means that the request can be served by continuing
      // going down in the current sweep.
      if (this.floor > requestedFloor + 0.5
            && (
                requestedDir != Direction.Up
                || requestedFloor <= lowestStop
                )
        )
        return this.timeToGo(floor, requestedFloor, direction);
      else
          // The elevator can not serve the request by continuing
          // the downwards sweep, because one or more of the necessary
          // conditions are not met.
          //
          // This means that the request must be served by continuing
          // going down in the current sweep and the going up.
        return this.timeToGo(floor, lowestStop, Direction.Down)
          + this.timeToGo(lowestStop, requestedFloor, Direction.Up);
    case Up:
        final int highestStop = highestStop();
        // The elevator is going up. The requested floor is
        // above the current one. The requested direction includes
        // going up or the requested floor is above the
        // highest stop of the current swep.
        //
        // This means that the request can be served by continuing
        // going up in the current sweep.
        if (this.floor < requestedFloor - 0.5
                && (
                    requestedDir != Direction.Down
                    || requestedFloor >= highestStop
                )
            )
            return this.timeToGo(floor, requestedFloor, direction);
        else
            // The elevator can not serve the request by continuing
            // the upwards sweep, because one or more of the necessary
            // conditions are not met.
            //
            // This means that the request must be served by continuing
            // going up in the current sweep and the going down.
            return this.timeToGo(floor, highestStop, Direction.Up)
                + this.timeToGo(highestStop, requestedFloor, Direction.Down);
      case None:
        return Math.abs(floor - requestedFloor);
    default:
      throw new RuntimeException("impossible timeToServe");
    }
  }

  // External request from lift controller (on event thread):
  public synchronized void customerAt(int floor, Direction thenDir /* not null */) {

      // 'Adds' the passed direction to the passed floor
    setStop(floor, thenDir.add(getStop(floor)));
  }

  // Internal request from the lift's own buttons (on event thread):
  public synchronized void goTo(int floor) {

      // Sets the direction for the passed floor
      // to None if it is null, leaves it untouched
      // otherwise
    setStop(floor, Direction.None.add(getStop(floor)));
  }

  public void run() {
      // Animating doors
      if (doorShift != 0.0) {
          openAndCloseDoors();
          return;
      }

      // The direction is changed in the previous iterations
      // of the loop. Such modification is made basing on
      // the status of the stops, which are changed by the
      // lift controller (when the u/d buttons are pressed),
      // or by the ui directly, when the internal buttons of
      // the elevator are clicked.
      switch (this.direction) {
          case Up:

            // At a floor, maybe stop here
            if ((int)floor == floor) {
                Direction afterStop = getStop((int)floor);

                // The floor is a stop. Such stop is not for
                // going down (the opposite direction than the
                // current, we are in the Up case), or the floor
                // is the highest stop
                //
                // Displaying door animation and updating stop.
                if (afterStop != null
                        && (
                            afterStop != Direction.Down
                            || (int)floor == highestStop()
                    )
                ) {
                    openAndCloseDoors();
                    subtractFromStop((int)floor, direction);
                }
            }

            // Progressing to a floor, but not arrived yet
            if (floor < highestStop()) {
                floor += direction.delta / wakeUpPerSecond;
                shaft.moveTo(floor, 0.0);
            }

            // Already at or above the highest floor, no longer moving
            else {
                direction = Direction.None;
            }
        break;


      case Down:

        // At a floor, maybe stop here
        if ((int)floor == floor) {
          Direction afterStop = getStop((int)floor);

          // The floor is a stop. Such stop is not for
          // going up (the opposite direction than the
          // current, we are in the Down case), or the floor
          // is the lowest stop
          //
          // Displaying door animation and updating stop.
          if (afterStop != null
                && (
                    afterStop != Direction.Up
                    || (int)floor == lowestStop()
                )
          ) {
            openAndCloseDoors();
            subtractFromStop((int)floor, direction);
          }
        }

        // Progressing to a floor, but not arrived yet
        if (floor > lowestStop()) {
          floor += direction.delta / wakeUpPerSecond;
          shaft.moveTo(floor, 0.0);
        }

        // Already at or below the lowest floor, no longer moving
        else {
            direction = Direction.None;
        }
        break;

      case None:
        final int lowestStop = lowestStop(), highestStop = highestStop();

        // Above lowest stop => going down
        if (floor >= lowestStop)
          direction = Direction.Down;

        // Below highest stop => going up
        else if (floor <= highestStop)
          direction = Direction.Up;
        break;

      default:
        throw new RuntimeException("impossible Lift.move");
      }
  }

  private void openAndCloseDoors() {
      // This will not achieve linear speed in the
      // animation, but it is not important for us
      shaft.moveTo(floor, Math.sin(doorShift * Math.PI / 2));

      // doorShift will be 2 at the end of the animation
      if ((int) doorShift == 2) {
         doorShift = 0.0;
      }
      else {
          doorShift += 1.0 / wakeUpPerSecond;
      }
  }
}

enum Direction {
  Down(-1), None(0), Up(+1), Both(2);
  public final int delta;
  private Direction(int delta) {
    this.delta = delta;
  }

  // Returns whether the direction is contained in this instance.
  //
  // Both contains anything.
  // Up, Down and None contains themselves only.
  public boolean isContainedBy(Direction dirAfter) { // may be null
    return dirAfter != null && (dirAfter == Both || dirAfter == this);
  }

  // Performs 'addition', which means
  //
  // Up + Down == Both
  // Down + Up == Both
  // Both as the second operand is the wildcard, as it counts both as Down and Up.
  //
  // Both + * == Both
  // None + * == *
  //
  // Used when updating the elevator stops when a request is received,
  // which happens both from the up and down button from outside the
  // elevator and from the buttons inside the elevator itself.
  public Direction add(Direction dir) {
    switch (this) {
    case Up:
      return (dir==Down || dir==Both) ? Both : this;
    case Down:
      return (dir==Up   || dir==Both) ? Both : this;
    case None:
      return dir != null ? dir : this;
    case Both:
      return this;
    default: throw new RuntimeException("impossible Direction.add");
    }
  }

  // Performs 'subtraction', which means
  //
  // Up - Down == Down
  // Down - Up == Up
  // Both as the second operand is the wildcard, as it counts both as Down and Up.
  //
  // In any other case, returns null.
  //
  // Used when updating the elevator stops after a floor is reached.
  public Direction subtractFrom(Direction dirAfter) {  // may be null
    switch (this) {
    case Up:
      return (dirAfter==Down || dirAfter==Both) ? Down : null;
    case Down:
      return (dirAfter==Up   || dirAfter==Both) ? Up : null;
    default: throw new RuntimeException("impossible Direction.subtractFrom");
    }
  }
}

class MoveTo {
  public final int floor;

  public MoveTo(int floor) {
    this.floor = floor;
  }
}
