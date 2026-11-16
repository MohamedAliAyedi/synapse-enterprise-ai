package com.example.aiorchestrator.tool;

import java.util.Map;

/**
 * Simple abstraction for a tool that can be invoked during an AI conversation.
 * Implementations should provide a unique name and a description that will be
 * surfaced to the LLM. The {@link #run(Map)} method contains the business logic
 * that should be executed when the tool is called.
 */
public interface Tool {
    /**
     * @return the unique name of the tool
     */
    String getName();

    /**
     * @return a short description of what the tool does
     */
    String getDescription();

    /**
     * Execute the tool with the supplied arguments.
     *
     * @param arguments a map of argument names to values
     * @return the result of the tool execution
     */
    Map<String, Object> run(Map<String, Object> arguments);
}
