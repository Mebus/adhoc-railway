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
import org.hibernate.dialect.FirebirdDialect;

import com.jgoodies.binding.beans.Model;

/**
 * TurnoutType generated by hbm2java
 */
@Entity
@Table(name = "turnout_type", catalog = "adhocrailway", uniqueConstraints = {})
public class TurnoutType extends Model implements java.io.Serializable,
		Comparable<TurnoutType> {

	// Fields
	@Id
	@GeneratedValue
	private int id;

	private String typeName;

	@Sort(type = SortType.NATURAL)
	private SortedSet<Turnout> turnouts = new TreeSet<Turnout>();

	private static final String PROPERTYNAME_ID = "id";
	private static final String PROPERTYNAME_TYPENAME = "typeName";
	private static final String PROPERTYNAME_TURNOUTS = "turnouts";

	public enum TurnoutTypes {
		DEFAULT, DOUBLECROSS, THREEWAY, UNKNOWN
	};

	public int compareTo(TurnoutType o) {
		return typeName.compareTo(o.getTypeName());
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result
				+ ((typeName == null) ? 0 : typeName.hashCode());
		return result;
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final TurnoutType other = (TurnoutType) obj;
		if (id != other.id)
			return false;
		if (typeName == null) {
			if (other.typeName != null)
				return false;
		} else if (!typeName.equals(other.typeName))
			return false;
		return true;
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
	@Id
	@GeneratedValue
	@Column(name = "id", unique = true, nullable = false, insertable = true, updatable = true)
	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		int old = this.id;
		this.id = id;
		firePropertyChange(PROPERTYNAME_ID, old, id);
	}

	@Column(name = "type_name", unique = false, nullable = false, insertable = true, updatable = true)
	public String getTypeName() {
		return this.typeName;
	}

	public void setTypeName(String typeName) {
		String old = this.typeName;
		this.typeName = typeName;
		firePropertyChange(PROPERTYNAME_TYPENAME, old, typeName);
	}

	@Sort(type = SortType.NATURAL)
	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER, mappedBy = "turnoutType")
	public SortedSet<Turnout> getTurnouts() {
		return this.turnouts;
	}

	@Sort(type = SortType.NATURAL)
	public void setTurnouts(SortedSet<Turnout> turnouts) {
		SortedSet<Turnout> old = this.turnouts;
		this.turnouts = turnouts;
		firePropertyChange(PROPERTYNAME_TURNOUTS, old, turnouts);
	}

}
