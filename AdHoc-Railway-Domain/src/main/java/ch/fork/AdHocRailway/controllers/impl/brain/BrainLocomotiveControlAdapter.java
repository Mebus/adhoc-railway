package ch.fork.AdHocRailway.controllers.impl.brain;

import ch.fork.AdHocRailway.controllers.LockingException;
import ch.fork.AdHocRailway.controllers.LocomotiveController;
import ch.fork.AdHocRailway.controllers.SimulatedMFXLocomotivesHelper;
import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveType;
import ch.fork.AdHocRailway.manager.locomotives.LocomotiveException;
import ch.fork.AdHocRailway.manager.locomotives.LocomotiveHelper;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

public class BrainLocomotiveControlAdapter extends LocomotiveController {

    private final BrainController brain;

    private final Set<Locomotive> activeLocomotives = Sets.newHashSet();
    private final BrainLocomotiveCommandBuilder brainLocomotiveCommandBuilder;

    public BrainLocomotiveControlAdapter(final BrainController brain) {
        this.brain = brain;
        brainLocomotiveCommandBuilder = new BrainLocomotiveCommandBuilder();
    }

    @Override
    public void toggleDirection(final Locomotive locomotive)
            throws LocomotiveException {
        LocomotiveHelper.toggleDirection(locomotive);
        setSpeed(locomotive, locomotive.getCurrentSpeed(),
                locomotive.getCurrentFunctions());
        informListeners(locomotive);
    }

    @Override
    public void setSpeed(final Locomotive locomotive, final int speed,
                         final boolean[] functions) throws LocomotiveException {

        initLocomotive(locomotive);

        try {
            final String command = brainLocomotiveCommandBuilder.getLocomotiveCommand(locomotive, speed, functions);

            brain.write(command);
            locomotive.setCurrentSpeed(speed);
            locomotive.setCurrentFunctions(functions);
            informListeners(locomotive);
        } catch (final BrainException e) {
            throw new LocomotiveException("error setting speed", e);
        }

    }

    private void initLocomotive(final Locomotive locomotive)
            throws LocomotiveException {
        try {
            if (!activeLocomotives.contains(locomotive)) {
                if (locomotive.getType().equals(LocomotiveType.SIMULATED_MFX)) {
                    final String initCommand1 = getInitCommand(locomotive,
                            locomotive.getAddress1());
                    final String initCommand2 = getInitCommand(locomotive,
                            locomotive.getAddress2());
                    brain.write(initCommand1);
                    brain.write(initCommand2);
                } else {
                    final String initCommand = getInitCommand(locomotive,
                            locomotive.getAddress1());
                    brain.write(initCommand);
                }
                activeLocomotives.add(locomotive);
            }
        } catch (final BrainException e) {
            throw new LocomotiveException("error initializing locomotive", e);
        }
    }

    @Override
    public void setFunction(final Locomotive locomotive,
                            final int functionNumber, final boolean state,
                            final int deactivationDelay) throws LocomotiveException {
        final boolean[] functions = locomotive.getCurrentFunctions();

        if (functionNumber >= functions.length) {
            return;
        }

        setFunctions(locomotive, functionNumber, state);
        locomotive.setCurrentFunctions(functions);

        informListeners(locomotive);

        if (deactivationDelay > 0) {
            startFunctionDeactivationThread(locomotive, functionNumber,
                    deactivationDelay);
        }
    }

    private void setFunctions(Locomotive locomotive, int functionNumber, boolean state) {
        List<String> functionsCommands = brainLocomotiveCommandBuilder.getFunctionsCommand(locomotive, functionNumber, state);

        for (String functionsCommand : functionsCommands) {
            brain.write(functionsCommand);
        }
    }

  /*  private void setFunctions(final Locomotive locomotive,
                              final boolean[] newFunctions) {
        List<String> functionsCommands = brainLocomotiveCommandBuilder.getFunctionsCommand(locomotive, newFunctions);

        for (String functionsCommand : functionsCommands) {
            brain.write(functionsCommand);
        }
        locomotive.setCurrentFunctions(newFunctions);
    }*/

    @Override
    public void emergencyStop(final Locomotive myLocomotive)
            throws LocomotiveException {
        setFunction(myLocomotive, myLocomotive.getEmergencyStopFunction(),
                true, 0);
        setSpeed(myLocomotive, 0, myLocomotive.getCurrentFunctions());

    }

    /**
     * Locking is not supported for BrainLocomotives
     */
    @Override
    public boolean isLocked(final Locomotive object) throws LockingException {
        return false;
    }

    /**
     * Locking is not supported for BrainLocomotives
     */
    @Override
    public boolean isLockedByMe(final Locomotive object)
            throws LockingException {
        return true;
    }

    /**
     * Locking is not supported for BrainLocomotives
     */
    @Override
    public boolean acquireLock(final Locomotive object) throws LockingException {
        return true;
    }

    /**
     * Locking is not supported for BrainLocomotives
     */
    @Override
    public boolean releaseLock(final Locomotive object) throws LockingException {
        return true;
    }


    private String getInitCommand(final Locomotive locomotive, final int address) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("XLS ");
        stringBuilder.append(address);
        stringBuilder.append(" ");
        if (locomotive.getType().equals(LocomotiveType.DELTA)) {
            stringBuilder.append("mm");
        } else {
            stringBuilder.append("mm2");
        }

        final String initCommand = stringBuilder.toString().trim();
        return initCommand;
    }
}
