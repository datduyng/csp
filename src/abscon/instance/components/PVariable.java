package abscon.instance.components;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;

public class PVariable implements Comparator<PVariable> {
	private String name;

	private PDomain domain;

	public PDomain currentDomain;

	public Set<PVariable> neighbors;

	public Set<PConstraint> constraints;

	public String getName() {
		return name;
	}

	public PDomain getDomain() {
		return domain;
	}

	public PVariable() {
		this.neighbors = new LinkedHashSet<>();
	}

	public PVariable(String name, PDomain domain) {
		this();
		this.name = name;
		this.domain = domain;
		this.currentDomain = PDomain.deepCopy(domain);
	}

	public void resetCurrentDomain() {
		this.currentDomain = PDomain.deepCopy(this.domain);
	}


	@Override
	public boolean equals(Object o) {
		if (o == null) { return false; }
		if (this == o) { return true; }
		if(o == null || o.getClass() != this.getClass())
			return false;
		// instanceof Check and actual value check
		return (((PVariable) o).name.equals(this.name));
	}

	@Override
	public int hashCode() {
		return this.name.hashCode();
	}

	public String toString() {
		return "  variable " + name + " with modified domain " + currentDomain.toString();
	}
}
