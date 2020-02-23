# Generic Data Structures for Storing CSPs

-----

# Hw1
## File structure

```
abscon.instance
csp
    - CFunction.java
    - abstract Constraint
        - ExtensionConstraint
        - IntensionConstraint
    - Domain
    - MyParser
    - ProblemInstance
    - Relation
    - Variable
```

## Modification made from abscon
- Information like relations and domains and constraints were not exposed as public. Thus, I created getter function so that `MyParser` class can access.
- field `presentationName` were also not being stored in the `InstanceParser` provided by abscon. I created and set the field in the function `parsePresentation()` using xml query command as follow: 

```
presentationElement.getAttribute("name");
```

## Data structure indepth


- Constraint is an abstract class with following fields
    - name: name of constraint
    - arity: size of scope
    - scope: variable related to constraint
- ExtensionConstraint extends Constraint with following field
    - relation: a list of array of size 2. represent list of tuple or values that support a relation
- IntensionConstraint extends Constraint with following field
    - function
        - functionalExpression
        
- Variable class represent CSP variables. have following fields:
    - name
    - initial-domain: value in the domain
    - current-domain: value in the domain that are still alive
    - constraints: pointers to the constraints that apply to the variable (i.e., constraints in the scope of which that variable appears).
    - neighbors: pointers to the other variables that share a constraint with this variable
    
- MyParser: take care most of the logic when parsing the XML. MyParser build these first build these following from xml
    - Map of domains
    - Map of variables
    - Map of Relation
    - Map of constraint
- I ultilized a map for efficient look given a name of a field. this help mapping related neighbor and domain to a variable easier.


-----
## Hw2

For parsing argument, I decided to use `CliArgs` class. A generic class that would parse command line token.

Helper function such as:

- `public boolean check(Variable vi, int vali , Variable vj, int valj)`
- `public boolean supported(Variable vi, int vali, Variable vj)`
- `public boolean revise(Variable vi, Variable vj)`
- `public void checkNodeConsistency()`

is implemented.


-----
## Hw3

Helper function below has been implemented

- `private int btLabel(int varIndex)`
- `public boolean is2VariableConsistent(PVariable vi, PVariable vj)`
- `public boolean supported(PVariable vi, Integer viVal, PVariable vj)`
- `public boolean check(PVariable vari, Integer vali,
                            PVariable varj, Integer valj)`
- `public List<PConstraint> findConstraintByScope(List<PVariable> scope)`



