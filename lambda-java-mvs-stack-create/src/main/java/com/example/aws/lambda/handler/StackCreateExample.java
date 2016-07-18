package com.example.aws.lambda.handler;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.CreateStackResult;
import com.amazonaws.services.cloudformation.model.Parameter;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
/**
 * Created by Ashwini on 7/13/16.
 */
public class StackCreateExample implements RequestHandler<Object, Object> {

    // Initialize the Log4j logger.
    static final Logger logger = Logger.getLogger(StackCreateExample.class);
    //List of parameters used in CFT
    private static final String PARAM_NAME_TAG = "ParamNameTag";
    private static final String PARAM_DEPLOYMENT_TARGET = "ParamDeploymentTarget";
    private static final String DATE_FORMATTER_VALUE = "yyyyMMdd";
    private static final String S3_TEMPLATE_URL = "s3TemplateUrl";
    private static final String STACK_NAME ="stackName";
    private Properties prop;

    private static final String CFT_STACK_NAME_VALUE_DEFAULT ="LambdaCFTExample";
    private static final String EC2_STACK_TAG_NAME_VALUE_DEFAULT = "LambdaCFTStack";
    private static final String DEPLOYMENT_ENVIRONMENT_DEFAULT = "DEV";
    private static final String CFT_S3_BUCKET_LOCATION_DEFAULT_VALUE = "https://s3.amazonaws.com/lambda-example/create/cft_create_script.json";

    /**
     * Handler method
     */
    @Override
    public Object handleRequest(Object input, Context context) {
        logger.info("Input: " + input);
        loadEnvProperties();//call to load the property files
        
        //Date 
        SimpleDateFormat ft = new SimpleDateFormat (DATE_FORMATTER_VALUE);
        String date = ft.format(new Date());
        String environment = context.getFunctionName();

        return createMVSStack(prop.getProperty(STACK_NAME,CFT_STACK_NAME_VALUE_DEFAULT)+"-"+date, environment);
    }

    /**
     * Method to create the MVS stacks
     * @param stackName
     * @return stack creation result
     */
    private String createMVSStack(String stackName, String environment) {
        logger.info("Creating the stack - " + stackName);
        AmazonCloudFormationClient amazonCloudFormationClient = new AmazonCloudFormationClient();
        CreateStackRequest createStackRequest = new CreateStackRequest();
      //Adding the stack name
        createStackRequest.setStackName(stackName);
        createStackRequest.setTemplateURL(prop.getProperty(S3_TEMPLATE_URL,CFT_S3_BUCKET_LOCATION_DEFAULT_VALUE));
      //Setting up the parameters
        createStackRequest.setParameters(createParametersList());
        CreateStackResult createStackResult = amazonCloudFormationClient.createStack(createStackRequest);

        logger.info("Create Stack Result - " + createStackResult.toString());
        return createStackResult.toString();
    }

    /**
     * Method to create the list of parameters used while creating the stack
     * @return List of parameter
     */
    private List<Parameter> createParametersList() {
        List<Parameter> parameterList =	new ArrayList<Parameter>();

        Parameter parameter1 = new Parameter();
        parameter1.setParameterKey(PARAM_DEPLOYMENT_TARGET);
        parameter1.setParameterValue(prop.getProperty(PARAM_DEPLOYMENT_TARGET,DEPLOYMENT_ENVIRONMENT_DEFAULT));
        parameter1.setUsePreviousValue(false);

        Parameter parameter2 = new Parameter();
        parameter2.setParameterKey(PARAM_NAME_TAG);
        parameter2.setParameterValue(prop.getProperty(PARAM_NAME_TAG,EC2_STACK_TAG_NAME_VALUE_DEFAULT));
        parameter2.setUsePreviousValue(false);

        parameterList.add(parameter1);
        parameterList.add(parameter2);
        logger.info("Parameter List for the stack creation - " + parameterList);
        return parameterList;
    }

    /**
     * Method to load all the properties based on the environment
     */
    private void loadEnvProperties() {
        prop = new Properties();
        InputStream envProperties = null;
        try {
            envProperties = new FileInputStream("env.properties"); //Loading the property file
            prop.load(envProperties);
            logger.info("Property file is loaded for " + prop.getProperty("envValue") + " environment");
        } catch (IOException e) {
            logger.error("Exception occurred while loading the property file : ", e.fillInStackTrace());
            logger.error("Redirecting to default values");
        } finally {
            if (envProperties != null) {
                try {
                    envProperties.close();
                } catch (IOException e) {
                    logger.error("Exception while closing input stream : ", e.fillInStackTrace());
                }
            }
        }
    }
}
