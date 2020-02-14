package abscon.instance.components;


import csp.old.Variable;

public class PVariable {
	private String name;

	private PDomain domain;

	public String getName() {
		return name;
	}

	public PDomain getDomain() {
		return domain;
	}

	public PVariable(String name, PDomain domain) {
		this.name = name;
		this.domain = domain;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) { return false; }
		if (this == o) { return true; }

		// instanceof Check and actual value check
		if ((o instanceof PVariable) && (((PVariable) o).name.equals(this.name))) {
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.name.hashCode();
	}

	public String toString() {
		return "  variable " + name + " with associated domain " + domain.getName();
	}
}
