serve:
	./mvnw exec:java
connect:
	mvn compile exec:java -Dexec.mainClass=za.co.wethinkcode.robots.client.RobotClient -Dexec.args="guysa 2146"
compile:
	./mvnw compile
test:
	./mvnw test
clean:
	./mvnw clean
serve_arged:
	./mvnw exec:java -Dexec.args="127.0.0.1 2146"