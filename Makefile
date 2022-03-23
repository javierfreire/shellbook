hello:
	@echo 'ShellBook'

clean:
	@./gradlew clean

jar:
	@./gradlew build

test: jar
	@java -jar build/libs/shellbook.jar src/test/resources/test.md

native:
	@./gradlew nativeCompile

dist:
	@./gradlew nativeDistTar

