package com.objectbrains.sti.db.entity.superentity;

/**
 *
 * @author chris
 */
public abstract class AbstractSuperEntity implements SuperEntityInterface {
    
    /**
     * Use PK for HashCode when possible.
     */
    @Override
    public int hashCode() {
        long myPK = getPk();
        if (myPK == 0) return super.hashCode();
        return (int) myPK;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object anotherObject) {
        // Quickest test
        if (this == anotherObject) return true;
        
        // Ensure that other object is a SuperEntity as well
        if (anotherObject == null) return false;
        if (!(anotherObject instanceof SuperEntity)) return false;
        
        // Ensure that PK is the same
        SuperEntity anotherSE = (SuperEntity) anotherObject;
        if (getPk() != anotherSE.getPk()) return false;
        
        // Ensure that the two are assignable
        Class<?> myClass = getClass();
        Class<?> otherClass = anotherSE.getClass();
        return ((myClass.isAssignableFrom(otherClass)) || (otherClass.isAssignableFrom(myClass)));
    }

    
}
