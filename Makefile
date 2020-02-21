
all:
	mkdir -p bin
	javac -encoding ISO-8859-1 -J-Xmx256m -d bin \
	       -sourcepath src src/csp/main/CSPSolver.java \
			src/abscon/instance/intension/*/*.java
	jar -J-Xmx256m cfm csp.jar src/META-INF/MANIFEST.MF -C bin .

	zip -r csp.zip --exclude=tests/* --exclude=results/* --exclude=libs/* --exclude=bin --exclude=out *

clean: 
	rm -rf bin/* csp.jar *~ csp.zip