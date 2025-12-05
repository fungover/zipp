## Development Notes
Here we can add notes to enhance and document our learning process.

JaCoCo = How much of the code is tested - 
we only need to add the plugin in pom.xml and run 'mvn test'


PIT = How well our tests actually work - we need to add PIT plugin as a targetclass, 
run 'mvn pitest:mutationCoverage' , open PIT reports and doubleclick the index.html
to see the report in the web browser.

SonarQube ties it all together and provides us with the bigger-quality view.
It requires a little more configuration, but I believe it is absolutely manageable. 
Especially since we are 2 on this issue.
