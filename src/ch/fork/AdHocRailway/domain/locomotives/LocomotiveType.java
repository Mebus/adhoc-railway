package ch.fork.AdHocRailway.domain.locomotives;

// Generated 08-Aug-2007 18:10:44 by Hibernate Tools 3.2.0.beta8

import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;

/**
 * LocomotiveType generated by hbm2java
 */
@Entity
@Table(name = "locomotive_type", catalog = "adhocrailway", uniqueConstraints = {})
public class LocomotiveType implements java.io.Serializable,
		Comparable<LocomotiveType> {

	// Fields

	@Id @GeneratedValue
	private int id;

	private String typeName;

	private int drivingSteps;

	private int stepping;

	private int functionCount;

	public static final int PROTOCOL_VERSION = 2;

	public static final String PROTOCOL = "M";

	@Sort(type = SortType.NATURAL)
	private SortedSet<Locomotive> locomotives = new TreeSet<Locomotive>();

	public int compareTo(LocomotiveType o) {
		if (this == o)
			return 0;
		if (o == null)
			return -1;
		return typeName.compareTo(o.getTypeName());
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		LocomotiveType t = (LocomotiveType) o;

		if (!typeName.equals(t.getTypeName()))
			return false;
		if (drivingSteps != t.getDrivingSteps())
			return false;
		if (functionCount != t.getFunctionCount())
			return false;
		return true;
	}

	public int hashCode() {
		return this.getTypeName().hashCode() + this.getDrivingSteps()
				+ this.getFunctionCount();
	}

	public String toString() {
		return this.getTypeName();
	}

	// Constructors

	/** default constructor */
	public LocomotiveType() {
	}

	/** minimal constructor */
	public LocomotiveType(int id, String typeName) {
		this.id = id;
		this.typeName = typeName;
	}

	/** full constructor */
	public LocomotiveType(int id, String typeName,
			SortedSet<Locomotive> locomotives) {
		this.id = id;
		this.typeName = typeName;
		this.locomotives = locomotives;
	}

	// Property accessors

	@Id @GeneratedValue
	@Column(name = "id", unique = true, nullable = false, insertable = true, updatable = true)
	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Column(name = "type_name", unique = false, nullable = false, insertable = true, updatable = true)
	public String getTypeName() {
		return this.typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	@Column(name = "drivingSteps", unique = false, nullable = false, insertable = true, updatable = true)
	public int getDrivingSteps() {
		return this.drivingSteps;
	}

	public void setDrivingSteps(int drivingSteps) {
		this.drivingSteps = drivingSteps;
	}

	@Column(name = "stepping", unique = false, nullable = false, insertable = true, updatable = true)
	public int getStepping() {
		return this.stepping;
	}

	public void setStepping(int stepping) {
		this.stepping = stepping;
	}

	@Column(name = "functionCount", unique = false, nullable = false, insertable = true, updatable = true)
	public int getFunctionCount() {
		return this.functionCount;
	}

	public void setFunctionCount(int functionCount) {
		this.functionCount = functionCount;
	}

	@Sort(type = SortType.NATURAL)
	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER, mappedBy = "locomotiveType")
	public SortedSet<Locomotive> getLocomotives() {
		return this.locomotives;
	}

	@Sort(type = SortType.NATURAL)
	public void setLocomotives(SortedSet<Locomotive> locomotives) {
		this.locomotives = locomotives;
	}

}
