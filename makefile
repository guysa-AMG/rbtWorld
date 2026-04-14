serve:
	./mvnw exec:java
compile:
	./mvnw compile
test:
	./mvnw test
clean:
	./mvnw clean
serve_arged:
	./mvnw exec:java -Dexec.args="127.0.0.1 2146"