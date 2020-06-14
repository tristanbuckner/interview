# Offer Service
_Note_: I had to upgrade sbt as the original version in the repo seems to be permanently broken on my machine (build fails bringing in dependencies)

Sbt run works on my machine, as does running in Intelli (using OpenJDK-14)

## Organization
I kept to the original repo's flat package structure since there are only a few files.
- Web server start up, routes, etc. are in [OfferService.scala](src/main/scala/OfferService.scala)
- Domain objects, validation, jsonformats, and a helper function for loading the experiments file are in [Experiments.scala](src/main/scala/Experiments.scala)
- The logic for finding the active experiment and filtering offers is in [OfferLogic.scala](src/main/scala/OfferLogic.scala)
