##To run the application, go to the project directory from terminal/command prompt and execute the following commands.

* Run with one commandline argument
./gradlew run --args="/Users/danukaaluthge/IdeaProjects/flo-energy/src/test/resources/meter-entries.csv"

* Run with two commmandline arguments
./gradlew run --args="/Users/danukaaluthge/IdeaProjects/flo-energy/src/test/resources/meter-entries.csv /Users/danukaaluthge/IdeaProjects/flo-energy/sql-st.sql"

### First argument is mandatory which is the path to meter records file
### Second argument is optional whichi is the file that sql inserts have to be written out. Default file will be sql-inserts.sql created in the current directory (project home).


