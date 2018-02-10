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
    
2.  Here we start to demonstrate how the java 
    serializer is not ideal for production purposes.
    This step requires a persistence repository that 
    is separate from the codebase, so install a local
    copy of cassandra@2.1 with default configuration.
    
    Note that we've added a "truthMatrix" field 
    to the class that gets persisted - a tuple of
    Doubles. Perhaps our actor calculates both
    the accuracy and popularity rankings of our
    Statement, and stores the values with the Statement.
    
    Now run the app itself. The Evolution app will 
    instantiate a Fact actor and send a command to 
    it, which will in turn persist to cassandra.
    
    Quit and restart the app while keeping cassandra 
    running. You'll see in the logs that the Fact actor's
    recovery process has recovered the previous Statement, 
    and added another Statement to the history.
    
    Keep cassandra running.  Quit the app and proceed 
    to the third commit.
    
3.  In this commit we have upgraded to scala 2.12,
    the next "major version" of scala. Run the app.
    
    You will see errors. This is because Akka Persistence
    is unable to replay the previous event from 
    cassandra.
    
    It couldn't replay because the default java 
    serializer treats scala 2.12 objects differently
    than scala 2.11 objects - in this case, it had
    trouble with the tuple of Doubles.
    
    Generally, one shouldn't use Tuples for 
    persistence events anyway - case classes are
    better. But the point is that java default 
    serialization is not guaranteed to work across 
    major scala versions, and other class structures
    may have trouble as well.
    
    Quit the app and delete your cassandra keyspace:
    
    `cqlsh> drop keyspace akka`
    
    A better serializer will help us transition across
    major scala versions. Proceed to the fourth commit.
    
4.  We have rolled back to scala 2.11, and represented
    the Statement class with an Avro schema. Notice
    that the Statement case class is no longer defined 
    in the Fact object. In fact, your IDE is probably
    complaining about the nonexistent class.
    
    This is because the class will be auto-generated
    as part of your compile step, from the schema 
    definition file in `src/main/resources/avro`. 
    
    Unfortunately, this may not work with your 
    default IntelliJ integration at this time of 
    writing. Instead, invoke `sbt compile` from the 
    shell to generate the Statement class. You should 
    then to be able to navigate to the class definition
    from the Fact actor.
    
    Note that we have also replaced the truthMatrix
    tuple with a case class, as this is better
    practice. Avro4s documents which types it 
    supports in its
    [README file](https://github.com/sksamuel/avro4s).
        
    We have also configured and created a serializer
    that will use Avro when writing to and reading
    from Cassandra. By default, Avro includes the
    schema with each message, because Avro needs
    to know which schema to use when de-serializing.
    
    Run the app by invoking `sbt run` from the shell. 
    It will save the Avro-serialized Statement to 
    Cassandra. You can also quit and restart the app
    (while leaving Cassandra running) to see that it
    successfully replays, as well. The logging 
    statements indicate that each message is 417 bytes.
    
    Proceed to the fifth commit.
    
5.  We've upgraded to scala 2.12. Run the app. It should
    run normally. This means that using Avro, the app was 
    able to replay and deserialize the previous 
    Statement(s) across major scala versions. Feel free
    to quit and run the app multiple times so that multiple
    messages are written to cassandra.
    
    This was accomplished using avro's default settings
    of sending the schema with every message. But what 
    if we don't want to send the schema with every 
    message?
    
    You should have at least one event stored in 
    cassandra. Quit the app. Empty out your 
    cassandra keyspace again:
    
    `cqlsh> drop keyspace akka`
    
    Proceed to the sixth commit.
    
6.  Since our schema definitions are part of our 
    codebase, we should be able to just send 
    along a schema identifier with our messages, 
    rather than the entire schema.
    
    We can accomplish this using two techniques.
    First, Avro4s allows us to serialize a message
    with a "binary" output type instead of a "data"
    output type. The difference is that "binary"
    does not include the schema.
    
    The second technique is to use Akka's 
    SerializerWithStringManifest. Each serialized
    message will include a String "manifest"; a 
    String description of how the class was serialized.
    
    For our purposes, the manifest should be a schema 
    identifier. The schema identifier can be a manually 
    specified number, or it can be generated from the
    schema itself using 
    [fingerprinting](http://avro.apache.org/docs/1.7.2/spec.html#Schema+Fingerprints)
    such as Rabin (for less than a million schema
    versions), MD5, or SHA-256.

    For our example, we are using MD5 since libraries
    are easily available.
    
    Run the app. Quit the app. Run the app 
    again. The app was able to serialize and 
    deserialize the "schema-less" messages. You'll
    see that the messages are now only 47 bytes 
    (plus 32 bytes for the identifier) instead of 
    417 bytes, a significant savings.
    
    This was all done without using schema evolution.
    But what if we want to change the definition of the
    Statement itself?  Quit the app and proceed to the 
    seventh commit.
    
7.  We have added a field to the Statement and 
    created a new version of the schema document.
    The schema requires a default value for the
    new field to avoid errors when reading old
    values from cassandra.
    
    We put the active schema in `src/main/avro`
    because that is the schema that will be used
    to generate the class at compile time.
    
    The old schema is put in `src/main/avroHistory`
    because we don't want to generate the class, 
    but still want the schema to be able to read
    older versions of messages.
    
    Since the class is altered, we also altered
    our usage of it in our Fact actor and test.
    We also added some logging to be able to 
    verify the class structure.
    
    Finally, we have updated the serializer so that 
    it supports schema evolution.
    
    The serializer will always write to cassandra
    using the most recent schema, but when reading
    from cassandra, it needs to know the different
    possible schemas.
    
    A map from fingerprint to schema supplies the
    different schemas, and allows the `fromBinary`
    method to recognize what schema a message
    was encoded with.
    
    Avro works by specifying both a writer and
    a reader schema, which is what it allows it
    to translate between any combination of versions.
    In our case, we always want to translate to the
    most recent "active" version of the Statement
    class.
    
    Stop and restart the app at least once more,
    to add more "second version" facts to 
    cassandra.
    
    Let's add in a third version, just to test
    it further. Stop the app, keep cassandra 
    running, and proceed to the eighth commit.
    
