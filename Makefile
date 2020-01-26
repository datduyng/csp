
all:
	mkdir -p bin
	javac -J-Xmx256m -d bin -sourcepath src src/csp/MyParser.java \
																src/abscon/instance/intension/*/*.java
	jar -J-Xmx256m cfm csp.jar src/META-INF/MANIFEST.MF -C bin .

	zip -r csp.zip *

clean: 
	rm -rf bin/* csp.jar *~ csp.zip