package ch.fork.AdHocRailway.domain.turnouts;

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
import javax.persistence.Transient;

import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;

/**
 * TurnoutType generated by hbm2java
 */
@Entity
@Table(name = "turnout_type", catalog = "adhocrailway", uniqueConstraints = {})
public class TurnoutType implements java.io.Serializable,
		Comparable<TurnoutType> {

	// Fields
	@Id @GeneratedValue
	private int id;

	private String typeName;

	@Sort(type = SortType.NATURAL)
	private SortedSet<Turnout> turnouts = new TreeSet<Turnout>();

	public enum TurnoutTypes {
		DEFAULT, DOUBLECROSS, THREEWAY, UNKNOWN
	};

	public int compareTo(TurnoutType o) {
		return typeName.compareTo(o.getTypeName());
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		TurnoutType t = (TurnoutType) o;

		if (id != t.getId())
			return false;
		if (!typeName.equals(t.getTypeName()))
			return false;
		return true;
	}

	public int hashCode() {
		return id + typeName.hashCode();
	}

	public String toString() {
		return typeName;
	}

	@Transient
	public TurnoutTypes getTurnoutTypeEnum() {
		if (typeName.toUpperCase().equals("DEFAULT"))
			return TurnoutTypes.DEFAULT;
		else if (typeName.toUpperCase().equals("DOUBLECROSS"))
			return TurnoutTypes.DOUBLECROSS;
		else if (typeName.toUpperCase().equals("THREEWAY"))
			return TurnoutTypes.THREEWAY;
		else 
			return TurnoutTypes.UNKNOWN;

	}

	// Constructors

	/** default constructor */
	public TurnoutType() {
	}

	/** minimal constructor */
	public TurnoutType(int id, String typeName) {
		this.id = id;
		this.typeName = typeName;
	}

	/** full constructor */
	public TurnoutType(int id, String typeName, SortedSet<Turnout> turnouts) {
		this.id = id;
		this.typeName = typeName;
		this.turnouts = turnouts;
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

	@Sort(type = SortType.NATURAL)
	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "turnoutType")
	public SortedSet<Turnout> getTurnouts() {
		return this.turnouts;
	}

	@Sort(type = SortType.NATURAL)
	public void setTurnouts(SortedSet<Turnout> turnouts) {
		this.turnouts = turnouts;
	}

}
