package org.inhahackers.optmo_user_be.config;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import org.springframework.cloud.function.adapter.azure.FunctionInvoker;

import java.util.Optional;

public class FunctionHandler extends FunctionInvoker<HttpRequestMessage<Optional<String>>, HttpResponseMessage> {

    @FunctionName("userFunction")
    public HttpResponseMessage userFunction(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET, HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request,
            ExecutionContext context) {

        return handleRequest(request, context);  // ✅ request 자체 전달
    }

    @FunctionName("oauthUserFunction")
    public HttpResponseMessage oauthUserFunction(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET, HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request,
            ExecutionContext context) {
        return handleRequest(request, context);
    }

    @FunctionName("jwtUserFunction")
    public HttpResponseMessage jwtUserFunction(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET, HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request,
            ExecutionContext context) {
        return handleRequest(request, context);
    }

    @FunctionName("increaseElecFunction")
    public HttpResponseMessage increaseElecFunction(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request,
            ExecutionContext context) {
        return handleRequest(request, context);
    }
}