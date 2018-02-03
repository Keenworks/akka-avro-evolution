## Akka and Avro Serial Evolution

This is a demonstration of using Avro for schema evolution
against Akka Persistence.

This repo is in tutorial form. That means to check it out and 
start with the first commit, and always refer to the README.

1.  The first commit demonstrates how akka persistence works
    against a journal. The `FactPersistenceSpec` works by 
    sending a command to an actor, killing the actor, and
    then querying the actor for its event history.

    If the actor worked correctly, it received the command
    and persisted an event to the persistence journal, and
    then replayed that event from the journal upon restart.
    
    Run the test and proceed to the second commit.
