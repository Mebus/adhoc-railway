package ch.fork.AdHocRailway.domain.locking;


public interface LockControlIface<E> {

	public boolean isLocked(E object) throws LockingException;
	
	public boolean isLockedByMe(E object) throws LockingException;
	
	public boolean acquireLock(E object) throws LockingException;

	public boolean releaseLock(E object) throws LockingException;

}