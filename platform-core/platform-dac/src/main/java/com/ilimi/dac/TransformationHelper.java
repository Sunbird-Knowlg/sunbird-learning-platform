package com.ilimi.dac;

import org.modelmapper.ModelMapper;

import com.ilimi.common.util.ILogger;
import com.ilimi.common.util.PlatformLogger;

/**
 * Helper to wrap commonly used utility functions in model mapping and
 * transformations.
 *
 * @author Feroz
 */
public class TransformationHelper {

    /** The Constant logger. */
    private static ILogger LOGGER = new PlatformLogger(TransformationHelper.class.getName());

    /**
     * Registers a type map between a given pair of classes(bi-directional) with
     * the specified model mapper instance. This allows the model mapper to
     * initialize its bindings before actually the requests are processed.
     *
     * @param <S>
     *            the generic type
     * @param <D>
     *            the generic type
     * @param modelMapper
     *            Model mapper to use
     * @param source
     *            Source class
     * @param destination
     *            Destination class
     */
    public static <S, D> void createTypeMap(ModelMapper modelMapper,
            Class<S> source, Class<D> destination) {
        createTypeMapSafe(modelMapper, source, destination);
        createTypeMapSafe(modelMapper, destination, source);
    }

    /**
     * Creates the type map between the specified pair. In doing so, handles the
     * IllegalStateException that model mapper may throw if the registration
     * between the pair already exists. This utility was needed to provide a
     * safe registration without causing program failures.
     *
     * @param <S>
     *            the generic type
     * @param <D>
     *            the generic type
     * @param modelMapper
     *            Model mapper to use
     * @param source
     *            Source class
     * @param destination
     *            Destination class
     */
    public static <S, D> void createTypeMapSafe(ModelMapper modelMapper,
            Class<S> source, Class<D> destination) {
        try {
            modelMapper.createTypeMap(source, destination);
        } catch (Exception ex) {
            LOGGER.log("Failed to register type map", ex);
        }
    }
}

