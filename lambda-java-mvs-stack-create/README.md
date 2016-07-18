This is an example to create the stack using aws lambda java apis. Based one the values from property file and the location of the CFT, the stack is created

1) build command : mvn clean -Dtarget.env=dev package shade:shade


Value of ${target.env} can be dev or prod, based on this parameter the properties file will be loaded.

maven shade plugin is used to create the jar will all the dependencies included.

While registering the function to AWS lambda use - com.example.aws.lambda.handler.StackCreateExample::handleRequest , as the handler method description.