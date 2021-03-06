package ch.fork.AdHocRailway.railway.brain.brain.brain;

import ch.fork.AdHocRailway.controllers.LocomotiveChangeListener;
import ch.fork.AdHocRailway.model.locomotives.Locomotive;
import ch.fork.AdHocRailway.railway.brain.brain.BrainController;
import ch.fork.AdHocRailway.railway.brain.brain.BrainLocomotiveControlAdapter;
import ch.fork.AdHocRailway.railway.brain.brain.BrainTestSupport;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

public class BrainLocomotiveControlAdapterTest extends BrainTestSupport {

    @Mock
    private BrainController brainController;
    @Mock
    private LocomotiveChangeListener listener;

    private BrainLocomotiveControlAdapter testee;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void set_speed_digital_locomotive() throws
            IOException {
        final Locomotive locomotive = createDigitalLocomotive();

        givenTestee();

        final int speed = 10;
        whenSettingSpeed(locomotive, speed, new boolean[]{false, false,
                false, false, false});

        assertBrainInitLocoCall(locomotive);
        assertBrainSetSpeedCalled(locomotive, speed, "1", "0", "0 0 0 0");
    }

    @Test
    public void increase_speed_digital_locomotive() throws
            IOException {
        final Locomotive locomotive = createDigitalLocomotive();
        locomotive.setCurrentSpeed(10);

        givenTestee();

        final int speed = 11;
        whenIncreasingSpeed(locomotive);

        assertBrainInitLocoCall(locomotive);
        assertBrainSetSpeedCalled(locomotive, speed, "1", "0", "0 0 0 0");
    }

    @Test
    public void increase_speed_digital_locomotive_speed_14()
            throws IOException {
        final Locomotive locomotive = createDigitalLocomotive();
        locomotive.setCurrentSpeed(14);

        givenTestee();

        whenIncreasingSpeed(locomotive);
        assertNoBrainCall();
    }

    @Test
    public void decrease_speed_digital_locomotive() throws
            IOException {
        final Locomotive locomotive = createDigitalLocomotive();
        locomotive.setCurrentSpeed(10);

        givenTestee();

        final int speed = 9;
        whenDecreasingSpeed(locomotive);

        assertBrainInitLocoCall(locomotive);
        assertBrainSetSpeedCalled(locomotive, speed, "1", "0", "0 0 0 0");
    }

    @Test
    public void decrease_speed_digital_locomotive_speed_0()
            throws IOException {
        final Locomotive locomotive = createDigitalLocomotive();
        locomotive.setCurrentSpeed(0);

        givenTestee();
        whenDecreasingSpeed(locomotive);

        assertNoBrainCall();
    }

    @Test
    public void set_functions_digital_locomotive()
            throws IOException {
        final Locomotive locomotive = createDigitalLocomotive();
        locomotive.setCurrentSpeed(0);

        givenTestee();
        whenSettingFunction(locomotive, 0, true);

        assertBrainSetSpeedCalled(locomotive, 0, "1", "1", "0 0 0 0");
    }

    @Test
    public void set_function_1_digital_locomotive()
            throws IOException {
        final Locomotive locomotive = createDigitalLocomotive();
        locomotive.setCurrentSpeed(0);

        givenTestee();
        whenSettingFunction(locomotive, 4, true);

        assertBrainSetSpeedCalled(locomotive, 0, "1", "0", "0 0 0 1");
    }

    private void whenSettingFunction(Locomotive locomotive, int i, boolean state) {
        testee.setFunction(locomotive, i, state, -1);
    }

    private void assertNoBrainCall() {
        Mockito.verifyZeroInteractions(brainController);
    }

    private void whenIncreasingSpeed(final Locomotive locomotive) {
        testee.increaseSpeed(locomotive);
    }

    private void whenDecreasingSpeed(final Locomotive locomotive) {
        testee.decreaseSpeed(locomotive);
    }

    private void assertBrainSetSpeedCalled(final Locomotive locomotive,
                                           final int speed, final String direction, final String light,
                                           final String functions) throws IOException {
        String brainLocomotiveCommand = createBrainLocomotiveCommand(locomotive, speed, direction, light, functions);
        Mockito.verify(brainController).write(brainLocomotiveCommand);
    }

    private void assertBrainInitLocoCall(final Locomotive locomotive)
            throws IOException {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("XLS ");
        stringBuilder.append(locomotive.getAddress1());
        stringBuilder.append(" mm2");
        Mockito.verify(brainController).write(stringBuilder.toString());
    }

    private void whenSettingSpeed(final Locomotive locomotive, final int speed,
                                  final boolean[] functions) {
        testee.setSpeed(locomotive, speed, functions);
    }

    private void givenTestee() {
        testee = new BrainLocomotiveControlAdapter(null, brainController);
    }

}
