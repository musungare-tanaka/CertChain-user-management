package com.certchain.blockchain;

import org.web3j.abi.TypeReference;
import java.util.List;

/**
 * Represents a custom error from a smart contract.
 * This class is used to define custom errors that can be emitted by smart contracts.
 */
public class CustomError {
    private final String name;
    private final List<TypeReference<?>> parameters;

    /**
     * Creates a new CustomError with the given name and parameters.
     *
     * @param name the name of the custom error
     * @param parameters the list of type references for the error parameters
     */
    public CustomError(String name, List<TypeReference<?>> parameters) {
        this.name = name;
        this.parameters = parameters;
    }

    /**
     * Gets the name of the custom error.
     *
     * @return the error name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the parameters of the custom error.
     *
     * @return the list of type references
     */
    public List<TypeReference<?>> getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return "CustomError{" +
                "name='" + name + '\'' +
                ", parameters=" + parameters +
                '}';
    }
}

